package ro.nq.metaexplorer.AutonomousViews;

import javafx.application.Platform;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.ItemViews.DatabaseHistoryView;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DatabaseHistoryControlView implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
            GenerateFilteredQueue.class,
            "DatabaseHistoryControlView",
            "DatabaseHistoryControlView.fxml",
            "DatabaseHistoryControlView.css",
            "ro.nq.metaexplorer.AutonomousViews.DatabaseHistoryControlView"
    );
    public static final int timerPeriodicityMilliseconds = 1000;
    public static final int preferredListViewWidth = 450;
    public static final int preferredListViewHeight = 450;
// ---- [status message state]
    // 0 -> [initial], 1 -> [thread has started]
    // 2 -> [views have been updated]
    // 3 -> [succeeded in saving history to file], 4 -> [failed in saving history to file]
    // 5 -> [succeeded in loading history from file], 6 -> [failed in loading history from file]
    // 7 -> [search has finished]
public int statusMessageState = 0;
Timer timer;
TimerTask historyDbSearchResultListener;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public DatabaseHistoryControlView (Terminal application) {
        this.statusMessageState = 0;
        this.timer = null;
        this.historyDbSearchResultListener = null;

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
            System.out.println("{DatabaseHistoryControlView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{DatabaseHistoryControlView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
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
@FXML VBox container_listView;
public @FXML ListView<DatabaseHistoryView> listView_historyDb;
@FXML Button button_searchAndLoadHistoryFromWorkingDirectory;
@FXML Button button_searchAndLoadHistoryFromHomeDirectory;
@FXML Button button_loadFrom;
@FXML Button button_saveTo;
@FXML Label label_statusMessage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.button_minimizeOrMaximize != null;
            Image minimizeIcon = Filesystem.loadImageResource(application, DatabaseHistoryControlView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
        assert this.label_header != null;
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(true);
        assert this.container_listView != null;
        assert this.listView_historyDb != null;
            this.listView_historyDb.setItems(this.application.observableHistoryDbViews);
            this.listView_historyDb.setCellFactory(items -> new DatabaseHistoryView.DatabaseHistoryViewCell(this.application));
        assert this.button_searchAndLoadHistoryFromWorkingDirectory != null;
            this.button_searchAndLoadHistoryFromWorkingDirectory.setOnAction(this::handleSearchAndLoadHistoryFromWorkingDirectory);
        assert this.button_searchAndLoadHistoryFromHomeDirectory != null;
            this.button_searchAndLoadHistoryFromHomeDirectory.setOnAction(this::handleSearchAndLoadHistoryFromHomeDirectory);
        assert this.button_loadFrom != null;
            this.button_loadFrom.setOnAction(this::handleLoadHistoryFrom);
        assert this.button_saveTo != null;
            this.button_saveTo.setOnAction(this::handleSaveHistoryTo);
        assert this.label_statusMessage != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::DatabaseHistoryControlView");
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
// ---- [timer task]
    public void setNewTimerBasedListeningTask() {
        this.historyDbSearchResultListener = new TimerTask() {
            @Override
            public void run() {
                if (application.foundHistoryFiles != null) {
                    List<File> historyFiles = application.foundHistoryFiles;

                        int addedHistoryDb = application.addObservableHistoryFiles(historyFiles);
                            String source = "";
                                if (application.userDir_or_userHome.getValue()) {
                                    source = "user.home";
                                }
                                else {
                                    source = "user.dir";
                                }
                            System.out.println(source + " - Added " + addedHistoryDb + " more HistoryDb");

                    // [set items back]
                    listView_historyDb.setItems(application.observableHistoryDbViews);
                        System.out.println("These are the items (HistoryDbView): " + application.observableHistoryDbViews.size());

                    Platform.runLater(() -> {
                        statusMessageState = 7;
                        synchronizeStatusMessage();
                    });

                    // [conditionally self-ending the TimerTask]
                    cancel();
                    timer.cancel();
                    timer = null;
                }
            }
        };
    }
// ---- [event handlers]
    public void handleSearchAndLoadHistoryFromWorkingDirectory(ActionEvent event) {
        this.resetListView();

        this.statusMessageState = 1;
        this.synchronizeStatusMessage();
        this.scheduleTimerTask();

        this.application.startHistoryDbThread(false);
    }
    public void handleSearchAndLoadHistoryFromHomeDirectory(ActionEvent event) {
        this.resetListView();

        this.statusMessageState = 1;
        this.synchronizeStatusMessage();
        this.scheduleTimerTask();

        this.application.startHistoryDbThread(true);
    }
    public void handleSaveHistoryTo (ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File saveFile = directoryChooser.showDialog(this.application.window);
            if (saveFile != null) {
                boolean saved = this.application.saveHistoryDbToDirectory(saveFile, false);
                    if (saved) {
                        this.statusMessageState = 3;
                    }
                    else {
                        this.statusMessageState = 4;
                    }
            }

        this.synchronizeStatusMessage();
    }
    public void handleLoadHistoryFrom (ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File loadFile = fileChooser.showOpenDialog(this.application.window);
            if (loadFile != null) {
                boolean loaded = this.application.loadHistoryFile(loadFile);
                    if (loaded) {
                        this.statusMessageState = 5;
                    }
                    else {
                        this.statusMessageState = 6;
                    }
            }

        this.synchronizeStatusMessage();
    }
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        Image maximizeIcon = Filesystem.loadImageResource(application, DatabaseHistoryControlView.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeIcon);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeIcon = Filesystem.loadImageResource(application, DatabaseHistoryControlView.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeIcon);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_header.setText(bundle.getString("label_header"));
        this.button_searchAndLoadHistoryFromWorkingDirectory.setText(bundle.getString("button_searchAndLoadHistoryFromWorkingDirectory"));
        this.button_searchAndLoadHistoryFromHomeDirectory.setText(bundle.getString("button_searchAndLoadHistoryFromHomeDirectory"));
        this.button_loadFrom.setText(bundle.getString("button_loadFrom"));
        this.button_saveTo.setText(bundle.getString("button_saveTo"));
        this.synchronizeStatusMessage();
    }
    public void resetListView () {
        System.out.println("Reset ListView");

        this.listView_historyDb.setItems(null);
        this.container_listView.getChildren().remove(this.listView_historyDb);
            this.listView_historyDb = null;

        this.listView_historyDb = new ListView<>();
            this.listView_historyDb.setCellFactory(items -> new DatabaseHistoryView.DatabaseHistoryViewCell(application));

        this.application.mainLayoutController.sizeLeftBorderMaximize();

            this.listView_historyDb.setItems(null);
        this.container_listView.getChildren().add(this.listView_historyDb);
    }
    public void scheduleTimerTask () {
        if (this.timer == null) {
            this.timer = new Timer();
        }

        if (this.historyDbSearchResultListener == null) {
            this.setNewTimerBasedListeningTask();
                this.timer.schedule(this.historyDbSearchResultListener, DatabaseHistoryControlView.timerPeriodicityMilliseconds, DatabaseHistoryControlView.timerPeriodicityMilliseconds);
        }
    }
    public void synchronizeStatusMessage () {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        // statusMessageState:
            // 0 -> [initial], 1 -> [thread has started]
            // 2 -> [views have been updated]
            // 3 -> [succeeded in saving history to file], 4 -> [failed in saving history to file]
            // 5 -> [succeeded in loading history from file], 6 -> [failed in loading history from file]
        if (this.statusMessageState == 0) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
            this.label_statusMessage.setStyle("-fx-text-fill: black");
        }
        else if (this.statusMessageState == 1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_alive"));
            this.label_statusMessage.setStyle("-fx-text-fill: black");
        }
        else if (this.statusMessageState == 2) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_viewsUpdated"));
            this.label_statusMessage.setStyle("-fx-text-fill: green");
        }
        else if (this.statusMessageState == 3) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_succeededSaveHistory"));
            this.label_statusMessage.setStyle("-fx-text-fill: green");
        }
        else if (this.statusMessageState == 4) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedSaveHistory"));
            this.label_statusMessage.setStyle("-fx-text-fill: orange");
        }
        else if (this.statusMessageState == 5) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_succeededLoadHistory"));
            this.label_statusMessage.setStyle("-fx-text-fill: green");
        }
        else if (this.statusMessageState == 6) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedLoadHistory"));
            this.label_statusMessage.setStyle("-fx-text-fill: orange");
        }
        else if (this.statusMessageState == 7) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_searchFinished"));
            this.label_statusMessage.setStyle("-fx-text-fill: green");
        }
        else {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_unknownState"));
            this.label_statusMessage.setStyle("-fx-text-fill: red");
        }
    }
}
