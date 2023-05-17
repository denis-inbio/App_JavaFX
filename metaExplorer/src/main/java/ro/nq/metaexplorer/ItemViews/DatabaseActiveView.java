package ro.nq.metaexplorer.ItemViews;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.BorderControllers.BorderedExplorer;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.DatabaseEntry;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.Locale;
import java.util.ResourceBundle;

public class DatabaseActiveView implements Initializable {
// ---- [ListCell variant]
    public static class DatabaseActiveViewCell extends ListCell<DatabaseActiveView> {
        public Terminal application;
        public DatabaseActiveView databaseActiveView;
        public DatabaseActiveViewCell (Terminal application) {
            this.application = application;
            this.databaseActiveView = null;
        }

        public void updateItem (DatabaseActiveView item, boolean empty) {
            super.updateItem(item, empty);
            this.databaseActiveView = item;

            if (item != null) {
                setGraphic(this.databaseActiveView.parent);
            }
            else {
                setGraphic(null);
            }
        }
    };
    public static class DatabaseActiveViewComboCell extends ComboBoxListCell<DatabaseActiveView> {
        public Terminal application;
        public DatabaseActiveView databaseActiveView;
        public DatabaseActiveViewComboCell (Terminal application) {
            this.application = application;
            this.databaseActiveView = null;
        }
        public void updateItem (DatabaseActiveView item, boolean empty) {
            super.updateItem(item, empty);
            this.databaseActiveView = item;

            if (item != null) {
                String customConnectionName = this.databaseActiveView.databaseEntry.customName.getValue();
                    if (customConnectionName.isBlank()) {
                        customConnectionName = this.databaseActiveView.databaseEntry.file.getValue().getName();
                    }
                setText(customConnectionName);
                    System.out.println("Selected active database: " + customConnectionName);
            }
            else {
                setText("");
            }
        }
    }
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        DatabaseActiveView.class,
        "DatabaseActiveView",
        "DatabaseActiveView.fxml",
        "DatabaseActiveView.css",
        "ro.nq.metaexplorer.ItemViews.DatabaseActiveView"
    );
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
    public static final String editIconPath = "icons/edit.png";
    public static final String checkIconPath = "icons/check.png";
// ---- [state, ctor]
public DatabaseEntry databaseEntry;
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public DatabaseActiveView (Terminal application, DatabaseEntry databaseEntry) {
        assert databaseEntry != null;
            this.databaseEntry = databaseEntry;
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
            System.out.println("{DatabaseActiveView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{DatabaseActiveView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_minimizeOrMaximize;
@FXML Label label_connectionName;
@FXML TextField textField_connectionName;
@FXML Button button_editConnectionName;
@FXML Button button_disconnectFromDatabase;
@FXML VBox minimizable;
@FXML Label label_fileCanonicalPath;
@FXML TextField textField_fileCanonicalPath;
@FXML Label label_databaseFilesCount;
@FXML TextField textField_databaseFilesCount;
@FXML Label label_lastQueryStatus;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
            this.base.setPrefWidth(BorderedExplorer.rightBorderActiveListViewsItemWidth);
            this.base.setMaxWidth(BorderedExplorer.rightBorderActiveListViewsItemWidth);
        assert this.button_minimizeOrMaximize != null;
            Image minimizeIcon = Filesystem.loadImageResource(application, DatabaseActiveView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);

        assert this.label_connectionName != null;

        assert this.textField_connectionName != null;
            this.synchronizeConnectionCustomName();
            this.addCustomConnectionNameListener();

        assert this.button_editConnectionName != null;
            Image editConnectionNameIcon = Filesystem.loadImageResource(application, DatabaseActiveView.editIconPath);
            ImageView editConnectionNameImageView = new ImageView(editConnectionNameIcon);
                this.button_editConnectionName.setGraphic(editConnectionNameImageView);
            this.button_editConnectionName.setOnAction(this::handleEnableEditConnectionName);

        assert this.button_disconnectFromDatabase != null;
            this.button_disconnectFromDatabase.setOnAction(this::handleTryToDisconnectFromDatabase);

        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(true);

        assert this.label_fileCanonicalPath != null;
        assert this.textField_fileCanonicalPath != null;
            this.synchronizeFileCanonicalPath();
            this.addFileCanonicalPathListener();
        assert this.label_databaseFilesCount != null;
        assert this.textField_databaseFilesCount != null;
            this.synchronizeDatabaseFileCount();
                this.addDatabaseFileCountListener();
        assert this.label_lastQueryStatus != null;
            // <TODO> ?

        this.updateLanguageDependentContent();
            this.addLanguageListener();
        this.synchronizeConnectionStatus();
            this.addConnectionStatusListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::DatabaseActiveView");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }
    InvalidationListener customConnectionNameListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Custom connection name invalidation in ::DatabaseActiveView");
            synchronizeConnectionCustomName();
        }
    };
    public void addCustomConnectionNameListener () {
        this.databaseEntry.customName.addListener(this.customConnectionNameListener);
    }
    public void removeCustomConnectionNameListener () {
        this.databaseEntry.customName.removeListener(this.customConnectionNameListener);
    }
    InvalidationListener connectionStatusListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Connection status invalidation in ::DatabaseActiveView: " + invalidation);
            synchronizeConnectionStatus();
        }
    };
    public void addConnectionStatusListener () {
        this.databaseEntry.connection.addListener(this.connectionStatusListener);
    }
    public void removeConnectionStatusListener () {
        this.databaseEntry.connection.removeListener(this.connectionStatusListener);
    }
    InvalidationListener fileCanonicalPathListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("File canonical path invalidation in ::DatabaseActiveView: " + invalidation);
            synchronizeFileCanonicalPath();
        }
    };
    public void addFileCanonicalPathListener () {
        this.databaseEntry.file.addListener(this.fileCanonicalPathListener);
    }
    public void removeFileCanonicalPathListener () {
        this.databaseEntry.file.removeListener(this.fileCanonicalPathListener);
    }
    InvalidationListener databaseFileCountListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Database file counts invalidation in ::DatabaseActiveView: " + invalidation);
            synchronizeDatabaseFileCount();
        }
    };
    public void addDatabaseFileCountListener () {
        this.databaseEntry.fileCount.addListener(this.databaseFileCountListener);
    }
    public void removeDatabaseFileCountListener () {
        this.databaseEntry.fileCount.removeListener(this.databaseFileCountListener);
    }

    public void removeAllListeners () {
        removeConnectionStatusListener();
        removeFileCanonicalPathListener();
        removeCustomConnectionNameListener();
        removeLanguageListener();
        removeDatabaseFileCountListener();
    }
// ---- [event handlers]
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        Image maximizeImage = Filesystem.loadImageResource(this.application, DatabaseActiveView.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeImage);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeImage = Filesystem.loadImageResource(this.application, DatabaseActiveView.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeImage);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
    }
    public void handleEnableEditConnectionName (ActionEvent event) {
        this.textField_connectionName.setEditable(true);
            this.removeCustomConnectionNameListener();

        Image checkImage = Filesystem.loadImageResource(application, DatabaseActiveView.checkIconPath);
        ImageView checkImageView = new ImageView(checkImage);
            this.button_editConnectionName.setGraphic(checkImageView);
        this.button_editConnectionName.setOnAction(this::handleCommitEditConnectionName);
    }
    public void handleCommitEditConnectionName (ActionEvent event) {
        this.textField_connectionName.setEditable(false);

        try {
            this.databaseEntry.customName_lock.lock();
            this.databaseEntry.customName.setValue(this.textField_connectionName.getText());
        }
        finally {
            this.databaseEntry.customName_lock.unlock();
        }

        Image checkImage = Filesystem.loadImageResource(application, DatabaseActiveView.editIconPath);
        ImageView checkImageView = new ImageView(checkImage);
            this.button_editConnectionName.setGraphic(checkImageView);
        this.button_editConnectionName.setOnAction(this::handleEnableEditConnectionName);

        synchronizeConnectionCustomName();
            this.addCustomConnectionNameListener();
    }
    public void handleTryToDisconnectFromDatabase (ActionEvent event) {
        this.disconnectAndCloseItem();
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_connectionName.setText(bundle.getString("label_connectionName"));
        this.button_disconnectFromDatabase.setText(bundle.getString("button_disconnectFromDatabase"));
        this.label_fileCanonicalPath.setText(bundle.getString("label_fileCanonicalPath"));
        this.label_databaseFilesCount.setText(bundle.getString("label_databaseFilesCount"));

        // <TODO>
        this.label_lastQueryStatus.setText(bundle.getString("label_lastQueryStatus_initial"));
    }
    public void disconnectAndCloseItem () {
        this.closeConnection();
        this.application.removeObservableActiveDbView(this);
    }
    public void synchronizeConnectionCustomName() {
        String connectionName = this.databaseEntry.customName.getValue();
        if (connectionName == null || connectionName.isBlank()) {
            connectionName = this.databaseEntry.file.getName();
        }
        this.textField_connectionName.setText(connectionName);
    }
    public void synchronizeFileCanonicalPath() {
        try {
            this.textField_fileCanonicalPath.setText(this.databaseEntry.file.getValue().getCanonicalPath());
        }
        catch (IOException exception) {
            System.out.println("{DatabaseActiveView.setFileCanonicalPath()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
    }
    public void synchronizeConnectionStatus() {
        Connection connection = this.databaseEntry.connection.getValue();
            if (connection == null) {
                disconnectAndCloseItem();
            }
    }
    public void synchronizeDatabaseFileCount () {
        long fileCount = this.databaseEntry.fileCount.getValue();
        if (fileCount < 0) {
            this.textField_databaseFilesCount.setText("?");
        }
        else {
            this.textField_databaseFilesCount.setText(String.valueOf(fileCount));
        }
    }
    public void closeConnection () {
        this.databaseEntry.closeConnection();
    }
}
