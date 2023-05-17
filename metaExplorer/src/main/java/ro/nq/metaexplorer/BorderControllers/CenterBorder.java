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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.SubControllers.AddNewFiles;
import ro.nq.metaexplorer.SubControllers.QueryDatabase;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class CenterBorder implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        CenterBorder.class,
        "CenterBorder",
        "CenterBorder.fxml",
        "CenterBorder.css",
        "ro.nq.metaexplorer.BorderControllers.CenterBorder"
    );
    public static final String enterFullscreenIconPath = "icons/enter_fullscreen.png";
    public static final String exitFullscreenIconPath = "icons/exit_fullscreen.png";
// ---- [extended logic state]
    // fullscreenMode:
        // 0 -> not fullscreen
        // 1 -> fullscreen
int fullscreenMode;
double previousAddNewFilesWidth;
double previousAddNewFilesHeight;
double previousSearchInDatabaseWidth;
double previousSearchInDatabaseHeight;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public CenterBorder(Terminal application) {
        this.fullscreenMode = 0;

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
                System.out.println("{CenterBorder} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{CenterBorder} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }

        this.addNewFiles = new AddNewFiles(this.application);
            this.container_addNewFilesToDatabase.getChildren().add(this.addNewFiles.parent);
        this.queryDatabase = new QueryDatabase(this.application);
            this.container_searchFilesInDatabase.getChildren().add(this.queryDatabase.parent);
    }
// ---- [controllers]
public AddNewFiles addNewFiles;
public QueryDatabase queryDatabase;
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_fullscreenModeSwitch;
@FXML Button button_addNewFilesMode;
@FXML Button button_searchADatabaseMode;
@FXML HBox switchContainer;
@FXML VBox container_addNewFilesToDatabase;
@FXML VBox container_searchFilesInDatabase;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.button_fullscreenModeSwitch != null;
            Image enterFullscreenImage = Filesystem.loadImageResource(application, CenterBorder.enterFullscreenIconPath);
            ImageView enterFullscreenImageView = new ImageView(enterFullscreenImage);
                this.button_fullscreenModeSwitch.setGraphic(enterFullscreenImageView);
            this.button_fullscreenModeSwitch.setOnAction(this::handleFullscreenModeSwitch);
        assert this.button_addNewFilesMode != null;
            this.button_addNewFilesMode.setOnAction(this::handleAddNewFilesToDatabaseMode);
        assert this.button_searchADatabaseMode != null;
            this.button_searchADatabaseMode.setOnAction(this::handleSearchFilesDatabaseMode);
        assert this.switchContainer != null;
        assert this.container_addNewFilesToDatabase != null;
            this.container_addNewFilesToDatabase.managedProperty().bind(this.container_addNewFilesToDatabase.visibleProperty());
            this.container_addNewFilesToDatabase.setVisible(true);
        assert this.container_searchFilesInDatabase != null;
            this.container_searchFilesInDatabase.managedProperty().bind(this.container_searchFilesInDatabase.visibleProperty());
            this.container_searchFilesInDatabase.setVisible(false);

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::CenterBorder");
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
        this.addLanguageListener();
    }
    public void removeAllListeners () {
        this.removeLanguageListener();
    }
// ---- [event handlers]
    public void handleAddNewFilesToDatabaseMode(ActionEvent event) {
        this.container_addNewFilesToDatabase.setVisible(true);
        this.container_searchFilesInDatabase.setVisible(false);
    }
    public void handleSearchFilesDatabaseMode(ActionEvent event) {
        this.container_addNewFilesToDatabase.setVisible(false);
        this.container_searchFilesInDatabase.setVisible(true);
    }
    public void handleFullscreenModeSwitch (ActionEvent event) {
        if (this.fullscreenMode == 0) {
            this.application.mainLayoutController.left.getChildren().clear();
            this.application.mainLayoutController.top.getChildren().clear();
            this.application.mainLayoutController.right.getChildren().clear();
            this.application.mainLayoutController.bottom.getChildren().clear();

            Image exitFullscreenImage = Filesystem.loadImageResource(application, CenterBorder.exitFullscreenIconPath);
            ImageView exitFullscreenImageView = new ImageView(exitFullscreenImage);
                this.button_fullscreenModeSwitch.setGraphic(exitFullscreenImageView);

            this.previousAddNewFilesWidth = this.application.mainLayoutController.centerController.addNewFiles.base.getMaxWidth();
            this.previousAddNewFilesHeight = this.application.mainLayoutController.centerController.addNewFiles.base.getMaxHeight();

            this.previousSearchInDatabaseWidth = this.application.mainLayoutController.centerController.queryDatabase.base.getMaxWidth();
            this.previousSearchInDatabaseHeight = this.application.mainLayoutController.centerController.queryDatabase.base.getMaxHeight();

            this.application.mainLayoutController.centerController.addNewFiles.base.setPrefWidth(BorderedExplorer.centerFullscreenBaseWidth);
            this.application.mainLayoutController.centerController.addNewFiles.base.setMaxWidth(BorderedExplorer.centerFullscreenBaseWidth);
            this.application.mainLayoutController.centerController.addNewFiles.base.setPrefHeight(BorderedExplorer.centerFullscreenSubControllerListViewHeight);
            this.application.mainLayoutController.centerController.addNewFiles.base.setMaxHeight(BorderedExplorer.centerFullscreenSubControllerListViewHeight);

            this.application.mainLayoutController.centerController.queryDatabase.base.setPrefWidth(BorderedExplorer.centerFullscreenBaseWidth);
            this.application.mainLayoutController.centerController.queryDatabase.base.setMaxWidth(BorderedExplorer.centerFullscreenBaseWidth);
            this.application.mainLayoutController.centerController.queryDatabase.base.setPrefHeight(BorderedExplorer.centerFullscreenSubControllerListViewHeight);
            this.application.mainLayoutController.centerController.queryDatabase.base.setMaxHeight(BorderedExplorer.centerFullscreenSubControllerListViewHeight);

            this.application.window.setFullScreen(true);
            this.fullscreenMode = 1;
        }
        else if (this.fullscreenMode == 1) {
            this.application.mainLayoutController.left.getChildren().add(this.application.mainLayoutController.leftController.parent);
            this.application.mainLayoutController.top.getChildren().add(this.application.mainLayoutController.topController.parent);
            this.application.mainLayoutController.right.getChildren().add(this.application.mainLayoutController.rightController.parent);
            this.application.mainLayoutController.bottom.getChildren().add(this.application.mainLayoutController.bottomController.parent);

            Image enterFullscreenImage = Filesystem.loadImageResource(application, CenterBorder.enterFullscreenIconPath);
            ImageView enterFullscreenImageView = new ImageView(enterFullscreenImage);
                this.button_fullscreenModeSwitch.setGraphic(enterFullscreenImageView);

            this.application.mainLayoutController.centerController.addNewFiles.base.setPrefWidth(previousAddNewFilesWidth);
            this.application.mainLayoutController.centerController.addNewFiles.base.setMaxWidth(previousAddNewFilesWidth);
            this.application.mainLayoutController.centerController.addNewFiles.base.setPrefHeight(previousAddNewFilesHeight);
            this.application.mainLayoutController.centerController.addNewFiles.base.setMaxHeight(previousAddNewFilesHeight);

            this.application.mainLayoutController.centerController.queryDatabase.base.setPrefWidth(previousSearchInDatabaseWidth);
            this.application.mainLayoutController.centerController.queryDatabase.base.setMaxWidth(previousSearchInDatabaseWidth);
            this.application.mainLayoutController.centerController.queryDatabase.base.setPrefHeight(previousSearchInDatabaseHeight);
            this.application.mainLayoutController.centerController.queryDatabase.base.setMaxHeight(previousSearchInDatabaseHeight);

            this.application.window.setFullScreen(false);
            this.fullscreenMode = 0;
        }
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.button_addNewFilesMode.setText(bundle.getString("button_addNewFilesMode"));
        this.button_searchADatabaseMode.setText(bundle.getString("button_searchADatabaseMode"));
    }
}
