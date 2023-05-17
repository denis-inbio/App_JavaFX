package ro.nq.metaexplorer.Dialogs;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.PeerSocket;

import java.io.IOException;
import java.net.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class CreateNewPeerConnection implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        CreateNewPeerConnection.class,
            "CreateNewPeerConnection",
            "CreateNewPeerConnection.fxml",
            "CreateNewPeerConnection.css",
            "ro.nq.metaexplorer.Dialogs.CreateNewPeerConnection"
    );
    public static final int connectionTimeout = 3000;
// ---- [extended logic state]
    // statusMessageState:
        // -1 -> initial
        // 0 -> error connecting to specified <IP, port>
int statusMessageState;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Stage window;
Scene scene;
public Parent parent;
    public CreateNewPeerConnection(Terminal application) {
        this.statusMessageState = -1;

        assert application != null;
            this.application = application;

        this.window = new Stage();

        URL stylesheet = this.getClass().getResource(classConstants.stylesheetPathName);
        System.out.println("Stylesheet `" + classConstants.stylesheetPathName + "` is: " + stylesheet);
        assert stylesheet != null;

        this.fxmlLoader = new FXMLLoader();
            this.fxmlLoader.setController(this);
            this.fxmlLoader.setLocation(this.getClass().getResource(classConstants.fxmlPathName));
        try {
            this.parent = this.fxmlLoader.load();
            parent.getStylesheets().add(stylesheet.toExternalForm());
            System.out.println("{CreateNewPeerConnection} [] (FXMLLoader.load()) - Parent: " + this.parent);

            this.scene = new Scene(this.parent);
                this.window.setScene(this.scene);
        }
        catch (IOException exception) {
            System.out.println("{CreateNewPeerConnection} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;
@FXML Label label_remoteHostIp;
@FXML Label label_remoteHostPort;
@FXML TextField textField_remoteHostIp;
@FXML TextField textField_remoteHostPort;
@FXML Button button_tryToConnect;
@FXML Button button_close;
@FXML Label label_statusMessage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.label_remoteHostIp != null;
        assert this.label_remoteHostPort != null;
        assert this.textField_remoteHostIp != null;
        assert this.textField_remoteHostPort != null;
        assert this.button_tryToConnect != null;
            this.button_tryToConnect.setOnAction(this::handleTryToConnect);
        assert this.button_close != null;
            this.button_close.setOnAction(this::handleCancel);
        assert this.label_statusMessage != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::CreateNewPeerConnection");
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
    public void handleCancel (ActionEvent event) {
        this.window.close();
    }
    public void handleTryToConnect (ActionEvent event) {
        this.statusMessageState = 1;
        this.updateLanguageDependentContent();

        try {
            String ip = this.textField_remoteHostIp.getText();
            Integer port = Integer.parseInt(this.textField_remoteHostPort.getText());

            InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
            Socket socket = new Socket();

            socket.connect(socketAddress, CreateNewPeerConnection.connectionTimeout);
            if (socket.isConnected()) {
                    this.statusMessageState = 2;
                    this.updateLanguageDependentContent();

                    PeerSocket peerSocket = new PeerSocket(socket);
                        System.out.println("Connected to new peer: `" + peerSocket.socket.getValue() + "` named `" + peerSocket.customName.getValue() + "`");

                    this.application.addObservableActivePeer(peerSocket, true);
                }
        }
        catch (NumberFormatException exception) {
            this.statusMessageState = 0;
            updateLanguageDependentContent();
                System.out.println("{CreateNewPeerConnection} [NumberFormatException] (Integer.parseInt()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
        catch (IOException exception) {
            this.statusMessageState = 0;
            updateLanguageDependentContent();
                System.out.println("{CreateNewPeerConnection} [IOException] (Socket.connect()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_remoteHostIp.setText(bundle.getString("label_remoteHostIp"));
        this.label_remoteHostPort.setText(bundle.getString("label_remoteHostPort"));
        this.button_tryToConnect.setText(bundle.getString("button_tryToConnect"));
        this.button_close.setText(bundle.getString("button_close"));

        if (this.statusMessageState == -1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
            this.label_statusMessage.setStyle("-fx-text-fill: black;");
        }
        else if (this.statusMessageState == 0) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_errorConnecting"));
            this.label_statusMessage.setStyle("-fx-text-fill: red;");
        }
        else if (this.statusMessageState == 1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_tryingToConnect"));
            this.label_statusMessage.setStyle("-fx-text-fill: orange;");
        }
        else if (this.statusMessageState == 2) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_success"));
            this.label_statusMessage.setStyle("-fx-text-fill: green;");
        }
    }
}
