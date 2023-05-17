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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.AutonomousViews.GenerateDatabaseQuery;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.ItemViews.FileMetadataView;
import ro.nq.metaexplorer.ItemViews.PageView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

public class QueryDatabase implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
            QueryDatabase.class,
            "QueryDatabase",
            "QueryDatabase.fxml",
            "QueryDatabase.css",
            "ro.nq.metaexplorer.SubControllers.QueryDatabase"
    );
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public QueryDatabase (Terminal application) {
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
            System.out.println("{QueryDatabase} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{QueryDatabase} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }

        this.generateDatabaseQueryAutonomousView = new GenerateDatabaseQuery(this.application);
            this.container_query.getChildren().add(this.generateDatabaseQueryAutonomousView.parent);
    }
// ---- [controllers]
public GenerateDatabaseQuery generateDatabaseQueryAutonomousView;
// ---- [FXML model]
public @FXML VBox base;
@FXML HBox container_query;
@FXML Label label_itemsPerPage;
@FXML TextField textField_itemsPerPage;
@FXML Button button_updateItemsPerPage;
public @FXML ListView<FileMetadataView> listview_selectedQueryFiles;
@FXML Button button_saveSelectedTo;
@FXML Button button_shareSelectedWith;
@FXML Button button_removeSelected;
public @FXML ListView<PageView> listView_pageViewCells;
@FXML Label label_statusMessagePrefix;
@FXML Label label_statusMessageCountSelectedFiles;
@FXML Label label_statusMessageSuffix;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.container_query != null;

        assert this.label_itemsPerPage != null;
        assert this.textField_itemsPerPage != null;
            this.textField_itemsPerPage.setText(String.valueOf(Terminal.initialItemsPerPage));
        assert this.button_updateItemsPerPage != null;
            this.button_updateItemsPerPage.setOnAction(this::handleUpdateItemsPerPage);

        // <TODO> not implemented
            this.label_itemsPerPage.setVisible(false);
            this.textField_itemsPerPage.setVisible(false);
            this.button_updateItemsPerPage.setVisible(false);
            this.label_itemsPerPage.setManaged(false);
            this.textField_itemsPerPage.setManaged(false);
            this.button_updateItemsPerPage.setManaged(false);

        assert this.listview_selectedQueryFiles != null;
            this.listview_selectedQueryFiles.setItems(this.application.observableSelectedFileViews);
            this.listview_selectedQueryFiles.setCellFactory(items -> new FileMetadataView.FileMetadataViewCell(this.application));
        assert this.button_saveSelectedTo != null;
            this.button_saveSelectedTo.setOnAction(this::handleSaveSelectedTo);
        assert this.button_shareSelectedWith != null;
            this.button_shareSelectedWith.setOnAction(this::handleShareSelectedWith);
        assert this.button_removeSelected != null;
            this.button_removeSelected.setOnAction(this::handleRemoveSelected);

        // <TODO> not implemented
        assert this.listView_pageViewCells != null;
            this.listView_pageViewCells.setVisible(false);
            this.listView_pageViewCells.setManaged(false);

        assert this.label_statusMessagePrefix != null;
        assert this.label_statusMessageCountSelectedFiles != null;
        assert this.label_statusMessageSuffix != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();

        this.synchronizeCountSelectedFiles();
            this.addCountSelectedFilesListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::QueryDatabase");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }

    InvalidationListener countSelectedFilesListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Count selected files invalidation in ::QueryDatabase");
            synchronizeCountSelectedFiles();
        }
    };
    public void addCountSelectedFilesListener () {
        this.application.observableSelectedFiles.addListener(this.countSelectedFilesListener);
    }
    public void removeCountSelectedFilesListener () {
        this.application.observableSelectedFiles.removeListener(this.countSelectedFilesListener);
    }

    public void addAllListeners () {
        addLanguageListener();
        addCountSelectedFilesListener();
    }
    public void removeAllListeners() {
        this.removeLanguageListener();
        this.removeCountSelectedFilesListener();
    }
// ---- [event handlers]
    public void handleSaveSelectedTo(ActionEvent event) {
        ObservableList<FileMetadataView> selectedFileMetadataViews = this.listview_selectedQueryFiles.getSelectionModel().getSelectedItems();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directoryFile = directoryChooser.showDialog(this.application.window);
            if (directoryFile != null) {
                for (FileMetadataView fileMetadataView : selectedFileMetadataViews) {
                    File file = Paths.get(directoryFile.getAbsolutePath(), fileMetadataView.fileMetadata.fileName + "." + fileMetadataView.fileMetadata.fileType).toFile();
                    boolean statusReadContent = fileMetadataView.fileMetadata.readContentFromDatabase();
                        if (statusReadContent) {
                            try {
                                Files.write(file.toPath(), fileMetadataView.fileMetadata.content);
                            }
                            catch (IOException exception) {
                                System.out.println("{QueryDatabase.handleSaveSelectedTo()} [IOException] (Files.write()) - " + exception.getMessage() + "\n" + exception.getCause());
                                // <TODO> Ui error message, status message
                            }
                        }
                        else {
                            // <TODO> Ui error message, status message; "how many files succeeded, how many failed ?"
                            ;
                        }
                }
            }
    }
    public void handleShareSelectedWith (ActionEvent event) {
        // <TODO>
    }
    public void handleRemoveSelected (ActionEvent event) {
        // <TODO>
    }

    public void handleUpdateItemsPerPage (ActionEvent event) {

    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_itemsPerPage.setText(bundle.getString("label_itemsPerPage"));
        this.button_updateItemsPerPage.setText(bundle.getString("button_updateItemsPerPage"));
        this.button_saveSelectedTo.setText(bundle.getString("button_saveSelectedTo"));
        this.button_shareSelectedWith.setText(bundle.getString("button_shareSelectedWith"));
        this.button_removeSelected.setText(bundle.getString("button_removeSelected"));
        this.label_statusMessagePrefix.setText(bundle.getString("label_statusMessagePrefix"));
        this.label_statusMessageSuffix.setText(bundle.getString("label_statusMessageSuffix"));
    }
    public void synchronizeCountSelectedFiles () {
        this.label_statusMessageCountSelectedFiles.setText(String.valueOf(this.application.observableSelectedFiles.size()));
    }

}