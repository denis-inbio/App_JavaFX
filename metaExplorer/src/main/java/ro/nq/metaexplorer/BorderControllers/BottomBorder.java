
package ro.nq.metaexplorer.BorderControllers;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.ItemViews.FileMetadataView;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class BottomBorder implements Initializable {
// ---- [classConstants]
    public static final ClassConstants classConstants = new ClassConstants(
        BottomBorder.class,
        "BottomBorder",
        "BottomBorder.fxml",
        "BottomBorder.css",
        "ro.nq.metaexplorer.BorderControllers.BottomBorder"
    );
    public static final String minimizeIconPath = "icons/down_arrow.png";
    public static final String maximizeIconPath = "icons/up_arrow.png";
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public BottomBorder(Terminal application) {
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
                System.out.println("{BottomBorder} [] (FXMLLoader.load()) - " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{BottomBorder} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none so far
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_minimizeOrMaximize;
@FXML VBox minimizable;
@FXML Label label_incomingFiles;
public @FXML ListView<FileMetadataView> listView_incomingFiles;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.button_minimizeOrMaximize != null;
            Image maximizeIcon = Filesystem.loadImageResource(application, BottomBorder.maximizeIconPath);
            ImageView maximizeImageView = new ImageView(maximizeIcon);
                this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(false);
        assert this.label_incomingFiles != null;
        assert this.listView_incomingFiles != null;
            this.listView_incomingFiles.setItems(this.application.observableQueuedIncomingFileViews);
            this.listView_incomingFiles.setCellFactory(items -> new FileMetadataView.FileMetadataViewCell(this.application));

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::BottomBorder");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }

    public void removeAllListeners () {
        removeLanguageListener();
    }
// ---- [event handlers]
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        Image maximizeImage = Filesystem.loadImageResource(application, BottomBorder.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeImage);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);

        this.application.mainLayoutController.sizeBottomBorderMinimize();
    }
    public void handleMaximizeMinimizable(ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeImage = Filesystem.loadImageResource(application, BottomBorder.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeImage);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);

        this.application.mainLayoutController.sizeBottomBorderMaximize();
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_incomingFiles.setText(bundle.getString("label_incomingFiles"));
    }
}
