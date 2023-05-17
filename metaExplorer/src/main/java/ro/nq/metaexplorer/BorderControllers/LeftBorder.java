package ro.nq.metaexplorer.BorderControllers;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.AutonomousViews.DatabaseHistoryControlView;
import ro.nq.metaexplorer.AutonomousViews.PeerHistoryControlView;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class LeftBorder implements Initializable {
    public static final ClassConstants classConstants = new ClassConstants(
        LeftBorder.class,
        "LeftBorder",
        "LeftBorder.fxml",
        "LeftBorder.css",
        "ro.nq.metaexplorer.BorderControllers.LeftBorder"
    );
    public static final String minimizeIconPath = "icons/left_arrow.png";
    public static final String maximizeIconPath = "icons/right_arrow.png";
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public LeftBorder (Terminal application) {
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
                System.out.println("{LeftBorder} [] (FXMLLoader.load()) - " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{LeftBorder} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }

        this.databaseHistoryControlView = new DatabaseHistoryControlView(this.application);
            this.container_historyDb.getChildren().add(this.databaseHistoryControlView.parent);
//        this.peerHistoryControlView = new PeerHistoryControlView(this.application);
//            this.container_historyPeer.getChildren().add(this.peerHistoryControlView.parent);
    }
// ---- [controllers]
public DatabaseHistoryControlView databaseHistoryControlView;
PeerHistoryControlView peerHistoryControlView;
// ---- [FXML model]
public @FXML VBox base;
@FXML Button button_minimizeOrMaximize;
@FXML VBox minimizable;
public @FXML VBox container_historyDb;
public @FXML VBox container_historyPeer;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;

        assert this.button_minimizeOrMaximize != null;
            Image maximizeIcon = Filesystem.loadImageResource(application, LeftBorder.maximizeIconPath);
            ImageView maximizeImageView = new ImageView(maximizeIcon);
                this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(false);
        assert this.container_historyDb != null;

        // <TODO> not implemented
        assert this.container_historyPeer != null;
            this.container_historyPeer.setVisible(false);
            this.container_historyPeer.setManaged(false);
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::LeftBorder");
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

        Image maximizeIcon = Filesystem.loadImageResource(application, LeftBorder.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeIcon);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);

        this.application.mainLayoutController.sizeLeftBorderMinimize();
        this.application.mainLayoutController.sizeSideEffectsLeftBorderMinimize();
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeIcon = Filesystem.loadImageResource(application, LeftBorder.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeIcon);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);

        this.application.mainLayoutController.sizeLeftBorderMaximize();
        this.application.mainLayoutController.sizeSideEffectsLeftBorderMaximize();
    }

// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);
        // there is literally nothing to update here [at the moment]
    }
}
