package ro.nq.metaexplorer.AutonomousViews;

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
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.DatabaseEntry;
import ro.nq.metaexplorer.ItemViews.DatabaseActiveView;
import ro.nq.metaexplorer.Utilities.Filesystem;
import ro.nq.metaexplorer.Utilities.SqliteDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class DatabaseActiveControlView implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        DatabaseActiveControlView.class,
            "DatabaseActiveControlView",
            "DatabaseActiveControlView.fxml",
            "DatabaseActiveControlView.css",
            "ro.nq.metaexplorer.AutonomousViews.DatabaseActiveControlView"
    );
// ---- [status message state]
    // <TODO> this is a model from a different view
    // 0 -> [initial], 1 -> [thread has started]
    // 2 -> [views have been updated]
    // 3 -> [succeeded in saving history to file], 4 -> [failed in saving history to file]
    // 5 -> [succeeded in loading history from file], 6 -> [failed in loading history from file]
public int statusMessageState = 0;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public DatabaseActiveControlView (Terminal application) {
        this.statusMessageState = 0;

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
            System.out.println("{DatabaseActiveControlView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{DatabaseActiveControlView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
// ---- [controllers]
    // none yet
// ---- [FXML model]
public @FXML VBox base;
@FXML Button button_minimizeOrMaximize;
@FXML Label label_header;
@FXML VBox minimizable;
public @FXML ListView<DatabaseActiveView> listView_activeDb;
@FXML Button button_createNewDatabase;
@FXML Button button_openExistingDatabaseFile;
@FXML Button button_disconnectSelectedDatabases;
@FXML Label label_statusMessage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;

        assert this.button_minimizeOrMaximize != null;
            Image minimizeIcon = Filesystem.loadImageResource(application, DatabaseActiveControlView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
        assert this.label_header != null;
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(true);

        assert this.listView_activeDb != null;
            this.listView_activeDb.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            this.listView_activeDb.setItems(this.application.observableActiveDbViews);
            this.listView_activeDb.setCellFactory(items -> new DatabaseActiveView.DatabaseActiveViewCell(this.application));
        assert this.button_createNewDatabase != null;
            this.button_createNewDatabase.setOnAction(this::handleCreateNewDatabase);
        assert this.button_openExistingDatabaseFile != null;
            this.button_openExistingDatabaseFile.setOnAction(this::handleOpenExistingDatabase);
        assert this.button_disconnectSelectedDatabases != null;
            this.button_disconnectSelectedDatabases.setOnAction(this::handleDisconnectSelectedDatabases);
        assert this.label_statusMessage != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::DatabaseActiveControlView");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }

    public void addAllListeners () {
        addLanguageListener();
    }
    public void removeAllListeners () {
        removeLanguageListener();
    }
// ---- [event handlers]
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        Image maximizeIcon = Filesystem.loadImageResource(application, DatabaseActiveControlView.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeIcon);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeIcon = Filesystem.loadImageResource(application, DatabaseActiveControlView.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeIcon);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
    }
    public void handleCreateNewDatabase (ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File savedFile = fileChooser.showSaveDialog(this.application.window);
            if (savedFile != null) {
                try {
                    FileWriter fileWriter = new FileWriter(savedFile);
                    fileWriter.flush();
                    fileWriter.close();

                    DatabaseEntry databaseEntry = SqliteDatabase.connectToDatabase(this.application, savedFile);
                        if (databaseEntry != null && databaseEntry.connection.getValue() != null) {
                            this.application.addObservableHistoryDb(databaseEntry, true);
                            this.application.addObservableActiveDb(databaseEntry, true);
                        }
                }
                catch (IOException exception) {
                    System.out.println("{DatabaseActiveControlView.handleCreateNewDatabase()} [IOException] (new FileWriter()) - " + exception.getMessage() + "\n" + exception.getCause());
                }
            }
    }
    public void handleOpenExistingDatabase (ActionEvent event) {
        // <TODO>
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.application.window);
            if (file != null) {
                DatabaseEntry databaseEntry = SqliteDatabase.connectToDatabase(this.application, file);
                    if (databaseEntry != null && databaseEntry.connection.getValue() != null) {
                        this.application.addObservableHistoryDb(databaseEntry, true);
                        this.application.addObservableActiveDb(databaseEntry, true);
                    }
                    else {
                        // <TODO> Ui, file not a valid SQL JDBC connection
                        ;
                    }
            }
    }
    public void handleDisconnectSelectedDatabases (ActionEvent event) {
        ObservableList<DatabaseActiveView> selectedDatabaseActiveViews = this.listView_activeDb.getSelectionModel().getSelectedItems();
        while (selectedDatabaseActiveViews.size() > 0) {
            DatabaseActiveView databaseActiveView = selectedDatabaseActiveViews.get(0);
            this.application.removeObservableActiveDb(databaseActiveView.databaseEntry, true);
        }
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_header.setText(bundle.getString("label_header"));
        this.button_createNewDatabase.setText(bundle.getString("button_createNewDatabase"));
        this.button_openExistingDatabaseFile.setText(bundle.getString("button_openExistingDatabaseFile"));
        this.button_disconnectSelectedDatabases.setText(bundle.getString("button_disconnectSelectedDatabases"));

        // 0 -> [initial]
        // 1 -> [views have been updated]
        // 2 -> [succeeded in creating new database], 3 -> [failed to create new database]
        // 4 -> [succeeded in opening database], 5 -> [failed to open database]
        // 6 -> [disconnected database]
        if (statusMessageState == 0) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
        }
        else if (statusMessageState == 1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_viewsUpdated"));
        }
        else if (statusMessageState == 2) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_successCreateNewDatabase"));
        }
        else if (statusMessageState == 3) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failureCreateNewDatabaase"));
        }
        else if (statusMessageState == 4) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_successOpenDatabase"));
        }
        else if (statusMessageState == 5) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failureOpenDatabase"));
        }
        else if (statusMessageState == 6) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_disconnectedDatabase"));
        }
    }
    public void synchronizeStatusMessage () {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        // <TODO> this is a model from a different  view
        // 0 -> [initial], 1 -> [thread has started]
        // 2 -> [views have been updated]
        // 3 -> [succeeded in saving history to file], 4 -> [failed in saving history to file]
        // 5 -> [succeeded in loading history from file], 6 -> [failed in loading history from file]

//        if (this.statusMessageState == 0) {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
//            this.label_statusMessage.setStyle("-fx-text-fill: black");
//        }
//        else if (this.statusMessageState == 1) {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_alive"));
//            this.label_statusMessage.setStyle("-fx-text-fill: black");
//        }
//        else if (this.statusMessageState == 2) {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_viewsUpdated"));
//            this.label_statusMessage.setStyle("-fx-text-fill: green");
//        }
//        else if (this.statusMessageState == 3) {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_succeededSaveHistory"));
//            this.label_statusMessage.setStyle("-fx-text-fill: green");
//        }
//        else if (this.statusMessageState == 4) {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedSaveHistory"));
//            this.label_statusMessage.setStyle("-fx-text-fill: orange");
//        }
//        else if (this.statusMessageState == 5) {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_succeededLoadHistory"));
//            this.label_statusMessage.setStyle("-fx-text-fill: green");
//        }
//        else if (this.statusMessageState == 6) {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedLoadHistory"));
//            this.label_statusMessage.setStyle("-fx-text-fill: orange");
//        }
//        else {
//            this.label_statusMessage.setText(bundle.getString("label_statusMessage_unknownState"));
//            this.label_statusMessage.setStyle("-fx-text-fill: red");
//        }
    }
}
