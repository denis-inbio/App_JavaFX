
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.Utilities.Filesystem;
import ro.nq.metaexplorer.Utilities.Languages;
import ro.nq.metaexplorer.Utilities.Networking;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class TopBorder implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        TopBorder.class,
        "TopBorder",
        "TopBorder.fxml",
        "TopBorder.css",
        "ro.nq.metaexplorer.BorderControllers.TopBorder"
    );
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
    public static final String englishIconPath = Languages.languageIconPaths[0];
    public static final String romanianIconPath = Languages.languageIconPaths[1];
    public static final Locale englishLocale = Languages.supportedLocales[0];
    public static final Locale romanianLocale = Languages.supportedLocales[1];
    public static final String englishLanguage = Languages.supportedLanguages[0];
    public static final String romanianLanguage = Languages.supportedLanguages[1];
// ---- [extended logic state]
    // statusMessageState:
        // -1 -> initial
        // 0 -> successfully started server, 1 -> stopped server
        // 2 -> not LAN network interface available
int statusMessageState;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public TopBorder(Terminal application) {
        this.statusMessageState = -1;

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
                System.out.println("{TopBorder} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{TopBorder} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [FXML model]
@FXML HBox base;
@FXML HBox minimizable;
@FXML Label label_language;
@FXML Button button_imageEnglish;
@FXML Button button_textEnglish;
@FXML Button button_imageRomanian;
@FXML Button button_textRomanian;
@FXML Label label_localServerIp;
@FXML Label label_localServerPort;
@FXML TextField textField_localServerIp;
@FXML TextField textField_localServerPort;
@FXML Button button_startOrStopServer;
@FXML ImageView imageView_serverStatusIcon;
@FXML Label label_statusMessage;
@FXML Button button_minimizeOrMaximize;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(false);
        assert this.label_language != null;
            this.label_language.requestFocus();
        assert this.button_imageEnglish != null;
            Image englishImage = Filesystem.loadImageResource(application, TopBorder.englishIconPath);
            ImageView englishImageView = new ImageView(englishImage);
                this.button_imageEnglish.setGraphic(englishImageView);
            this.button_imageEnglish.setOnAction(this::handleSwitchToEnglishLanguage);
        assert this.button_textEnglish != null;
            this.button_textEnglish.setOnAction(this::handleSwitchToEnglishLanguage);
        assert this.button_imageRomanian != null;
            Image romanianImage = Filesystem.loadImageResource(application, TopBorder.romanianIconPath);
            ImageView romanianImageView = new ImageView(romanianImage);
                this.button_imageRomanian.setGraphic(romanianImageView);
            this.button_imageRomanian.setOnAction(this::handleSwitchToRomanianLanguage);
        assert this.button_textRomanian != null;
            this.button_textRomanian.setOnAction(this::handleSwitchToRomanianLanguage);
        assert this.label_localServerIp != null;
        assert this.label_localServerPort != null;
        assert this.textField_localServerIp != null;
        assert this.textField_localServerPort != null;
        assert this.button_startOrStopServer != null;
            this.button_startOrStopServer.setOnAction(this::handleStartServer);
        assert this.imageView_serverStatusIcon != null;
        assert this.label_statusMessage != null;
        assert this.button_minimizeOrMaximize != null;
            Image maximizeImage = Filesystem.loadImageResource(application, TopBorder.maximizeIconPath);
            ImageView maximizeImageView = new ImageView(maximizeImage);
                this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::TopBorder");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }
// ---- [event handlers]
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        Image maximizeImage = Filesystem.loadImageResource(application, TopBorder.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeImage);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);

        this.application.mainLayoutController.sizeTopBorderMinimize();
    }
    public void handleMaximizeMinimizable(ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeImage = Filesystem.loadImageResource(application, TopBorder.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeImage);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);

        this.application.mainLayoutController.sizeTopBorderMaximize();
    }
    public void handleSwitchToEnglishLanguage(ActionEvent event) {
        Locale.setDefault(TopBorder.englishLocale);
            this.application.observableLanguage.setValue(TopBorder.englishLanguage);
        System.out.println("Default locale changed to: " + Locale.getDefault() + " and observed string to `" + this.application.observableLanguage.getValue() + "`");
    }
    public void handleSwitchToRomanianLanguage(ActionEvent event) {
        Locale.setDefault(TopBorder.romanianLocale);
            this.application.observableLanguage.setValue(TopBorder.romanianLanguage);
        System.out.println("Default locale changed to: " + Locale.getDefault() + " and observed string to `" + this.application.observableLanguage.getValue() + "`");
    }
    public void handleStartServer (ActionEvent event) {
        boolean serverStarted = Networking.startServer(this.application);
            if (!serverStarted) {
                this.statusMessageState = 2;
            }
            else {
                this.statusMessageState = 0;
                this.updateSocketInformation();
                this.button_startOrStopServer.setOnAction(this::handleStopServer);
            }
        updateLanguageDependentContent();
    }
    public void handleStopServer (ActionEvent event) {
        Networking.stopServer(this.application);
            this.statusMessageState = 1;
            this.updateSocketInformation();
            this.button_startOrStopServer.setOnAction(this::handleStartServer);
        updateLanguageDependentContent();
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_language.setText(bundle.getString("label_language"));
        this.label_localServerIp.setText(bundle.getString("label_localServerIp"));
        this.label_localServerPort.setText(bundle.getString("label_localServerPort"));

        if (this.application.serverSocket.getValue() != null) {
            this.button_startOrStopServer.setText(bundle.getString("button_startOrStopServer_stop"));
        }
        else {
            this.button_startOrStopServer.setText(bundle.getString("button_startOrStopServer_start"));
        }
        updateSocketInformation();

        // statusMessageState:
            // -1 -> initial
            // 0 -> successfully started server, 1 -> stopped server
            // 2 -> not LAN network interface available
        if (this.statusMessageState == -1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
            this.label_statusMessage.setStyle("-fx-text-fill: black");
        }
        else if (this.statusMessageState == 0) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_successfullyStartedServer"));
            this.label_statusMessage.setStyle("-fx-text-fill: green");
        }
        else if (this.statusMessageState == 1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_stoppedServer"));
            this.label_statusMessage.setStyle("-fx-text-fill: green");
        }
        else if (this.statusMessageState == 2) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_noLANInterfaceAvailable"));
            this.label_statusMessage.setStyle("-fx-text-fill: red");
        }
    }

    public void updateSocketInformation () {
        if (this.application.serverSocket.getValue() != null) {
            this.textField_localServerIp.setText(this.application.serverSocket.getValue().getInetAddress().getHostAddress());
            this.textField_localServerPort.setText(String.valueOf(this.application.serverSocket.getValue().getLocalPort()));
        }
        else {
            Locale locale = Locale.getDefault();
            ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

            this.textField_localServerIp.setText(bundle.getString("textField_localServerIp_empty"));
            this.textField_localServerPort.setText(bundle.getString("label_localServerPort_empty"));
        }
    }
}
