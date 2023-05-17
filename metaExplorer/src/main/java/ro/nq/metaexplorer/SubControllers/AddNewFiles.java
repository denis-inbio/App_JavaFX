package ro.nq.metaexplorer.SubControllers;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.AutonomousViews.GenerateFilteredQueue;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.ItemViews.FileMetadataView;
import ro.nq.metaexplorer.ItemViews.PageView;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class AddNewFiles implements Initializable {
    // ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
            AddNewFiles.class,
            "AddNewFiles",
            "AddNewFiles.fxml",
            "AddNewFiles.css",
            "ro.nq.metaexplorer.SubControllers.AddNewFiles"
    );
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public AddNewFiles(Terminal application) {
        assert application != null;
            this.application = application;

        URL stylesheet = this.getClass().getResource(classConstants.stylesheetPathName);
        System.out.println("Stylesheet `" + classConstants.stylesheetPathName + "` is: " + stylesheet);
        assert stylesheet != null;

        this.fxmlLoader = new FXMLLoader();
            this.fxmlLoader.setController(this);
            this.fxmlLoader.setLocation(this.getClass().getResource(classConstants.fxmlPathName));
        try {
            this.parent = this.fxmlLoader.load();
            parent.getStylesheets().add(stylesheet.toExternalForm());
            System.out.println("{AddNewFiles} [] (FXMLLoader.load()) - Parent: " + this.parent);
        } catch (IOException exception) {
            System.out.println("{AddNewFiles} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }

        this.generateFilteredQueueAutonomousView = new GenerateFilteredQueue(this.application);
            this.container_filter.getChildren().add(this.generateFilteredQueueAutonomousView.parent);
    }

// ---- [controllers]
public GenerateFilteredQueue generateFilteredQueueAutonomousView;
// ---- [FXML model]
public @FXML VBox base;
@FXML HBox container_filter;
@FXML Label label_itemsPerPage;
@FXML TextField textField_itemsPerPage;
@FXML Button button_updateItemsPerPage;
public @FXML ListView<FileMetadataView> listView_paginatedQueuedFileViewCells;
public @FXML ListView<PageView> listView_pageViewCells;
@FXML Label label_statusMessagePrefix;
@FXML Label label_statusMessageCountFilteredFiles;
@FXML Label label_statusMessageMiddle;
@FXML Label label_statusMessageCountAllFiles;
@FXML Label label_statusMessageSuffix;
@FXML Button button_clearAllQueuedFiles;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.container_filter != null;
        assert this.label_itemsPerPage != null;
        assert this.textField_itemsPerPage != null;
            this.textField_itemsPerPage.setText(String.valueOf(Terminal.initialItemsPerPage));
        assert this.button_updateItemsPerPage != null;
            this.button_updateItemsPerPage.setOnAction(this::handleUpdateItemsPerPage);
        assert this.listView_paginatedQueuedFileViewCells != null;
            this.listView_paginatedQueuedFileViewCells.setItems(this.application.observablePaginatedFilteredQueuedFileViews);
            this.listView_paginatedQueuedFileViewCells.setCellFactory(items -> new FileMetadataView.FileMetadataViewCell(this.application));
            this.listView_paginatedQueuedFileViewCells.setOnDragOver(this::handleDragOver);
            this.listView_paginatedQueuedFileViewCells.setOnDragDropped(this::handleDragDropped);
        assert this.listView_pageViewCells != null;
            this.listView_pageViewCells.setItems(this.application.observablePageViews);
            this.listView_pageViewCells.setCellFactory(items -> new PageView.PageViewCell(this.application));
        assert this.label_statusMessagePrefix != null;
        assert this.label_statusMessageCountFilteredFiles != null;
            this.synchronizeFilteredListCount();
                this.addFilteredListCountListener();
        assert this.label_statusMessageMiddle != null;
        assert this.label_statusMessageCountAllFiles != null;
            this.synchronizeTotalListCount();
                this.addTotalListCountListener();
        assert this.label_statusMessageSuffix != null;
        assert this.button_clearAllQueuedFiles != null;
            this.button_clearAllQueuedFiles.setOnAction(this::handleClearAllQueuedItems);

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::AddNewFiles");
            updateLanguageDependentContent();
        }
    };

    public void addLanguageListener() {
        this.application.observableLanguage.addListener(this.languageListener);
    }

    public void removeLanguageListener() {
        this.application.observableLanguage.removeListener(this.languageListener);
    }

    InvalidationListener filteredListCountListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Filtered list count invalidation in ::AddNewFiles");
            synchronizeFilteredListCount();
        }
    };

    public void addFilteredListCountListener() {
        this.application.observableFilteredQueuedFileViews.addListener(this.filteredListCountListener);
    }

    public void removeFilteredListCountListener() {
        this.application.observableFilteredQueuedFileViews.removeListener(this.filteredListCountListener);
    }

    InvalidationListener totalListCountListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Total list count invalidation in ::AddNewFiles");
            synchronizeTotalListCount();
        }
    };

    public void addTotalListCountListener() {
        this.application.observableQueuedFiles.addListener(totalListCountListener);
    }

    public void removeTotalListCountListener() {
        this.application.observableQueuedFiles.removeListener(totalListCountListener);
    }

    public void addAllListeners () {
        addLanguageListener();
        addFilteredListCountListener();
        addTotalListCountListener();
    }
    public void removeAllListeners() {
        removeLanguageListener();
        removeFilteredListCountListener();
        removeTotalListCountListener();
    }
// ---- [event handlers]
    public void handleClearAllQueuedItems(ActionEvent event) {
        this.application.clearObservableQueuedFiles();
    }
    public void handleUpdateItemsPerPage (ActionEvent event) {
        Integer itemsPerPage = null;
        try {
            itemsPerPage = Integer.parseInt(this.textField_itemsPerPage.getText());
            this.application.setItemsPerPage(itemsPerPage);
        }
        catch (NumberFormatException exception) {
            System.out.println("{AddNewFiles.handleUpdateItemsPerPage()} [NumberFormatException] (Integer.parseInt()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
    }
    public void handleDragOver (DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }
    public void handleDragDropped (DragEvent event) {
        this.application.addObservableQueuedFiles(event.getDragboard().getFiles());
    }





    public void handleInsertSelected (ActionEvent event) {
        ObservableList<FileMetadataView> selectedViews = this.listView_paginatedQueuedFileViewCells.getSelectionModel().getSelectedItems();
        // <TODO>
    }
    public void handleShareSelectedWith (ActionEvent event) {
        ObservableList<FileMetadataView> selectedViews = this.listView_paginatedQueuedFileViewCells.getSelectionModel().getSelectedItems();
        // <TODO>
    }
    public void handleRemoveSelected (ActionEvent event) {
        ObservableList<FileMetadataView> selectedViews = this.listView_paginatedQueuedFileViewCells.getSelectionModel().getSelectedItems();
        for (FileMetadataView selectedView : selectedViews) {
            this.application.observableQueuedFiles.remove(selectedView.fileMetadata);
        }
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_itemsPerPage.setText(bundle.getString("label_itemsPerPage"));
        this.button_updateItemsPerPage.setText(bundle.getString("button_updateItemsPerPage"));
        this.label_statusMessagePrefix.setText(bundle.getString("label_statusMessagePrefix"));
        // <TODO> listener
//        this.label_statusMessageCountFilteredFiles.setText(bundle.getString("label_statusMessageCountFilteredFiles"));
        this.label_statusMessageMiddle.setText(bundle.getString("label_statusMessageMiddle"));
        // <TODO> listener
//        this.label_statusMessageCountAllFiles.setText(bundle.getString("label_statusMessageCountAllFiles"));
        this.label_statusMessageSuffix.setText(bundle.getString("label_statusMessageSuffix"));
        this.button_clearAllQueuedFiles.setText(bundle.getString("button_clearAllQueuedFiles"));
    }
    public void synchronizeFilteredListCount () {
        int filteredListSize = this.application.observableFilteredQueuedFileViews.size();
            this.label_statusMessageCountFilteredFiles.setText(String.valueOf(filteredListSize));
    }
    public void synchronizeTotalListCount () {
        int totalListSize = this.application.observableQueuedFiles.size();
            this.label_statusMessageCountAllFiles.setText(String.valueOf(totalListSize));
    }
}
