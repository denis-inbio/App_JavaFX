package ro.nq.metaexplorer.ItemViews;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.BorderControllers.BorderedExplorer;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.DatabaseEntry;
import ro.nq.metaexplorer.Utilities.Filesystem;
import ro.nq.metaexplorer.Utilities.Queries;
import ro.nq.metaexplorer.Utilities.SqliteDatabase;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class DatabaseHistoryView implements Initializable {
// ---- [ListCell variant]
    public static class DatabaseHistoryViewCell extends ListCell<DatabaseHistoryView> {
        public Terminal application;
        public DatabaseHistoryView databaseHistoryView;
        public DatabaseHistoryViewCell (Terminal application) {
            this.application = application;
            this.databaseHistoryView = null;
        }

        public void updateItem (DatabaseHistoryView item, boolean empty) {
            super.updateItem(item, empty);
            this.databaseHistoryView = item;

            if (item != null) {
                setGraphic(this.databaseHistoryView.parent);
            }
            else {
                setGraphic(null);
            }
        }
    };
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        DatabaseHistoryView.class,
        "DatabaseHistoryView",
        "DatabaseHistoryView.fxml",
        "DatabaseHistoryView.css",
        "ro.nq.metaexplorer.ItemViews.DatabaseHistoryView"
    );
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
    public static final String closeIconPath = "icons/close.png";
    public static final String editIconPath = "icons/edit.png";
    public static final String checkIconPath = "icons/check.png";
    public static final String closedConnectionStatusIconPath = "icons/red_status.png";
    public static final String openConnectionStatusIconPath = "icons/green_status.png";
// ---- [state, ctor]
public DatabaseEntry databaseEntry;
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public DatabaseHistoryView (Terminal application, DatabaseEntry databaseEntry) {
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
            System.out.println("{DatabaseHistoryView} [] (FXMLLoader.load()) - Successfully loaded FXML - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{DatabaseHistoryView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_closeItem;
@FXML Button button_minimizeOrMaximize;
@FXML ImageView imageView_connectionStatus;
@FXML Label label_connectionName;
@FXML TextField textField_connectionName;
@FXML Button button_editConnectionName;
@FXML Button button_tryToConnectToDatabase;
@FXML VBox minimizable;
@FXML Label label_fileCanonicalPath;
@FXML TextField textField_fileCanonicalPath;
@FXML Label label_databaseFilesCount;
@FXML TextField textField_databaseFilesCount;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
            this.base.setPrefWidth(BorderedExplorer.leftBorderHistoryDbListViewItemWidth);
            this.base.setMaxWidth(BorderedExplorer.leftBorderHistoryDbListViewItemWidth);
        assert this.button_closeItem != null;
            Image closeIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.closeIconPath);
            ImageView closeImageView = new ImageView(closeIcon);
                this.button_closeItem.setGraphic(closeImageView);
            this.button_closeItem.setOnAction(this::handleCloseItem);
        assert this.button_minimizeOrMaximize != null;
            Image minimizeIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
        assert this.imageView_connectionStatus != null;
            this.synchronizeConnectionStatus();
                this.addConnectionStatusListener();
        assert this.label_connectionName != null;
        assert this.textField_connectionName != null;
            this.synchronizeConnectionCustomName();
                this.addCustomConnectionNameListener();
        assert this.button_editConnectionName != null;
            Image editConnectionNameIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.editIconPath);
            ImageView editConnectionNameImageView = new ImageView(editConnectionNameIcon);
                this.button_editConnectionName.setGraphic(editConnectionNameImageView);
            this.button_editConnectionName.setOnAction(this::handleEnableEditConnectionName);
        assert this.button_tryToConnectToDatabase != null;
            this.button_tryToConnectToDatabase.managedProperty().bind(this.button_tryToConnectToDatabase.visibleProperty());
            this.button_tryToConnectToDatabase.setVisible(true);
            this.button_tryToConnectToDatabase.setOnAction(this::handleTryToConnectToDatabase);
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

        updateLanguageDependentContent();
        this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::DatabaseHistoryView");
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
            System.out.println("Custom connection name invalidation in ::DatabaseHistoryView");
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
            System.out.println("Connection status invalidation in ::DatabaseHistoryView: " + invalidation);
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
            System.out.println("File canonical path invalidation in ::DatabaseHistoryView: " + invalidation);
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
            System.out.println("Database file counts invalidation in ::DatabaseHistoryView: " + invalidation);
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
    public void handleCloseItem (ActionEvent event) {
        this.application.removeObservableHistoryDb(this.databaseEntry, true);
    }
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
            Image maximizeIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.maximizeIconPath);
            ImageView maximizeImageView = new ImageView(maximizeIcon);
                this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
            Image minimizeIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
    }
    public void handleEnableEditConnectionName (ActionEvent event) {
        this.textField_connectionName.setEditable(true);
        this.removeCustomConnectionNameListener();

        this.button_editConnectionName.setOnAction(this::handleCommitEditConnectionName);
            Image checkIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.checkIconPath);
            ImageView checkImageView = new ImageView(checkIcon);
                this.button_editConnectionName.setGraphic(checkImageView);
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

        this.button_editConnectionName.setOnAction(this::handleEnableEditConnectionName);
            Image checkIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.editIconPath);
            ImageView checkImageView = new ImageView(checkIcon);
                this.button_editConnectionName.setGraphic(checkImageView);

        synchronizeConnectionCustomName();
            this.addCustomConnectionNameListener();
    }
    public void handleTryToConnectToDatabase (ActionEvent event) {
        System.out.println("Connection: " + this.databaseEntry.connection.getValue() + "\n\t| path: " + this.databaseEntry.file.get().getAbsolutePath());

        if (this.databaseEntry.connection.getValue() == null) {
            try {
                this.databaseEntry.connection_lock.lock();

                String databaseFileCanonicalPath = this.databaseEntry.file.getValue().getCanonicalPath();
                Connection connection = DriverManager.getConnection(SqliteDatabase.jdbcConnectionPrefix + databaseFileCanonicalPath);
                   this.databaseEntry.connection.setValue(connection);

                boolean newTableCreated = Queries.evaluateCreateTableIfNotExists(this.databaseEntry.connection.getValue());
                    System.out.println("New table was created: " + newTableCreated);

                long countFiles = Queries.countFilesStatement(this.databaseEntry.connection.getValue());
                    this.databaseEntry.fileCount.setValue(countFiles);

            } catch (IOException exception) {
                System.out.println("{DatabaseHistoryView.handleTryToConnectToDatabase()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            } catch (SQLException exception) {
                System.out.println("{DatabaseHistoryView.handleTryToConnectToDatabase()} [SQLException] (DriverManager.getConnection()) - " + exception.getMessage() + "\n" + exception.getCause());
            } finally {
                this.databaseEntry.connection_lock.unlock();
            }
        }

        if (this.databaseEntry.connection.getValue() != null) {
            this.application.addObservableActiveDb(this.databaseEntry, true);
        }
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
            ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_connectionName.setText(bundle.getString("label_connectionName"));
        this.button_tryToConnectToDatabase.setText(bundle.getString("button_tryToConnectToDatabase"));
        this.label_fileCanonicalPath.setText(bundle.getString("label_fileCanonicalPath"));
        this.label_databaseFilesCount.setText(bundle.getString("label_databaseFilesCount"));
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
            System.out.println("{DatabaseHistoryView.setFileCanonicalPath()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
    }
    public void synchronizeConnectionStatus() {
        Connection connection = this.databaseEntry.connection.getValue();
            if (connection == null) {
                Image closedConnectionStatusIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.closedConnectionStatusIconPath);
                    this.imageView_connectionStatus.setImage(closedConnectionStatusIcon);

                this.button_tryToConnectToDatabase.setVisible(true);
            }
            else {
                Image openConnectionStatusIcon = Filesystem.loadImageResource(application, DatabaseHistoryView.openConnectionStatusIconPath);
                    this.imageView_connectionStatus.setImage(openConnectionStatusIcon);

                this.button_tryToConnectToDatabase.setVisible(false);
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
