
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
import ro.nq.metaexplorer.AutonomousViews.DatabaseActiveControlView;
import ro.nq.metaexplorer.AutonomousViews.PeerActiveControlView;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class RightBorder implements Initializable {
    public static final ClassConstants classConstants = new ClassConstants(
        RightBorder.class,
        "RightBorder",
        "RightBorder.fxml",
        "RightBorder.css",
        "ro.nq.metaexplorer.BorderControllers.RightBorder"
    );
    public static final String minimizeIconPath = "icons/right_arrow.png";
    public static final String maximizeIconPath = "icons/left_arrow.png";
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public RightBorder(Terminal application) {
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
                System.out.println("{RightBorder} [] (FXMLLoader.load()) - " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{BorderedTabExplorer} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }

        this.databaseActiveControlView = new DatabaseActiveControlView(this.application);
            this.container_activeDb.getChildren().add(this.databaseActiveControlView.parent);
        this.peerActiveControlView = new PeerActiveControlView(this.application);
            this.container_activePeer.getChildren().add(this.peerActiveControlView.parent);
    }
// ---- [controllers]
DatabaseActiveControlView databaseActiveControlView;
PeerActiveControlView peerActiveControlView;
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_minimizeOrMaximize;
@FXML VBox minimizable;
@FXML VBox container_activeDb;
@FXML VBox container_activePeer;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;

        assert this.button_minimizeOrMaximize != null;
            Image maximizeIcon = Filesystem.loadImageResource(application, RightBorder.maximizeIconPath);
            ImageView maximizeImageView = new ImageView(maximizeIcon);
                this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(false);
        assert this.container_activeDb != null;
        assert this.container_activePeer != null;
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::RightBorder");
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

        Image maximizeIcon = Filesystem.loadImageResource(application, RightBorder.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeIcon);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);

        this.application.mainLayoutController.sizeRightBorderMinimize();
        this.application.mainLayoutController.sizeSideEffectsRightBorderMinimize();
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeIcon = Filesystem.loadImageResource(application, RightBorder.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeIcon);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);

        this.application.mainLayoutController.sizeRightBorderMaximize();
        this.application.mainLayoutController.sizeSideEffectsRightBorderMaximize();
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);
        // there is literally nothing to update here [at the moment]
    }
}
