package ro.nq.metaexplorer.ItemViews;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.BorderControllers.BorderedExplorer;
import ro.nq.metaexplorer.BorderControllers.TopBorder;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.PeerSocket;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class PeerSocketView implements Initializable {
// ---- [ListCell variant]
    public static class PeerSocketViewCell extends ListCell<PeerSocketView> {
        public Terminal application;
        public PeerSocketView peerSocketView;
        public PeerSocketViewCell (Terminal application) {
            this.application = application;
            this.peerSocketView = null;
        }

        public void updateItem (PeerSocketView item, boolean empty) {
            super.updateItem(item, empty);
            this.peerSocketView = item;

            if (item != null) {
                // <TODO>
                this.setGraphic(this.peerSocketView.parent);
                this.setAlignment(Pos.CENTER);
            }
            else {
                this.setGraphic(null);
            }
        }
    };
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        PeerSocketView.class,
        "PeerSocketView",
        "PeerSocketView.fxml",
        "PeerSocketView.css",
        "ro.nq.metaexplorer.ItemViews.PeerSocketView"
    );
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
    public static final String editIconPath = "icons/edit.png";
    public static final String checkIconPath = "icons/check.png";
// ---- [extended logic state]
    // connectState:
        // -1 -> initial
        // 0 -> disconnected
        // 1 -> connected
int connectState;
    // statusMessagesState
        // -1 -> initial
        // 0 -> socket is null
        // 1 -> socket connection is established
int statusMessagesState;
// ---- [state, ctor]
public PeerSocket peerSocket;
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public PeerSocketView (Terminal application, PeerSocket peerSocket) {
        this.connectState = -1;
        this.statusMessagesState = -1;

        assert peerSocket != null;
            this.peerSocket = peerSocket;
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
            System.out.println("{PeerSocketView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{PeerSocketView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_minimizeOrMaximize;
@FXML Label label_customName;
@FXML TextField textField_customName;
@FXML Button button_editOrCommitCustomName;
@FXML Button button_connectOrDisconnectPeer;
@FXML VBox minimizable;
@FXML Label label_remoteHostIp;
@FXML Label label_remoteHostPort;
@FXML Label label_localPort;
@FXML TextField textField_remoteHostIp;
@FXML TextField textField_remoteHostPort;
@FXML TextField textField_localPort;
@FXML Label label_statusMessage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.peerSocket.socket == null) {
            this.connectState = 0;
        }
        else {
            this.connectState = 1;
        }

        assert this.base != null;
            this.base.setPrefWidth(BorderedExplorer.rightBorderActiveListViewsItemWidth);
            this.base.setMaxWidth(BorderedExplorer.rightBorderActiveListViewsItemWidth);
        assert this.button_minimizeOrMaximize != null;
            Image minimizeIcon = Filesystem.loadImageResource(application, PeerSocketView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
        assert this.label_customName != null;
        assert this.textField_customName != null;
        assert this.button_editOrCommitCustomName != null;
            Image editImage = Filesystem.loadImageResource(application, PeerSocketView.editIconPath);
            ImageView editImageView = new ImageView(editImage);
                this.button_editOrCommitCustomName.setGraphic(editImageView);
            this.button_editOrCommitCustomName.setOnAction(this::handleEnableEditConnectionName);
        assert this.button_connectOrDisconnectPeer != null;
            this.button_connectOrDisconnectPeer.setOnAction(this::handleDisconnectPeer);
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(true);
        assert this.label_remoteHostIp != null;
        assert this.label_remoteHostPort != null;
        assert this.label_localPort != null;
        assert this.textField_remoteHostIp != null;
        assert this.textField_remoteHostPort != null;
        assert this.textField_localPort != null;
        assert this.label_statusMessage != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();

        this.synchronizeCustomName();
            this.addCustomNameListener();

        this.updateSocketInformation();
            this.addSocketListener();
    }

// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::PeerSocketView");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }
    InvalidationListener socketListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Socket invalidation in ::PeerSocketView");
            updateSocketInformation();
        }
    };
    public void addSocketListener() {
        this.peerSocket.socket.addListener(this.socketListener);
    }
    public void removeSocketListener() {
        this.peerSocket.socket.removeListener(this.socketListener);
    }
    InvalidationListener customNameListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Peer socket custom name invalidation in ::PeerSocketView");
            synchronizeCustomName();
        }
    };
    public void addCustomNameListener() {
        this.peerSocket.customName.addListener(this.customNameListener);
    }
    public void removeCustomNameListener() {
        this.peerSocket.customName.removeListener(this.customNameListener);
    }

    public void addAllListeners () {
        addLanguageListener();
        addSocketListener();
        addCustomNameListener();
    }
    public void removeAllListeners () {
        removeLanguageListener();
        removeSocketListener();
        removeCustomNameListener();
    }
// ---- [event handlers]
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        Image maximizeImage = Filesystem.loadImageResource(application, TopBorder.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeImage);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
    }
    public void handleMaximizeMinimizable(ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeImage = Filesystem.loadImageResource(application, TopBorder.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeImage);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);

        // <TODO> all *Borders' min/max functions need to call a corresponding function from CenterBorder in order to adjust its width/height
        // currently, I think that these functions are placed in Terminal, but they should be moved into CenterBorder
    }
    public void handleDisconnectPeer (ActionEvent event) {
        this.application.removeObservableActivePeer(this.peerSocket, true);
    }
    public void handleEnableEditConnectionName (ActionEvent event) {
        this.textField_customName.setEditable(true);
            this.removeCustomNameListener();

        Image checkImage = Filesystem.loadImageResource(application, PeerSocketView.checkIconPath);
        ImageView checkImageView = new ImageView(checkImage);
            this.button_editOrCommitCustomName.setGraphic(checkImageView);
        this.button_editOrCommitCustomName.setOnAction(this::handleCommitEditConnectionName);
    }
    public void handleCommitEditConnectionName (ActionEvent event) {
        this.textField_customName.setEditable(false);

        try {
            this.peerSocket.customName_lock.lock();
            this.peerSocket.customName.setValue(this.textField_customName.getText());
        }
        finally {
            this.peerSocket.customName_lock.unlock();
        }

        Image checkImage = Filesystem.loadImageResource(application, PeerSocketView.editIconPath);
        ImageView checkImageView = new ImageView(checkImage);
            this.button_editOrCommitCustomName.setGraphic(checkImageView);
        this.button_editOrCommitCustomName.setOnAction(this::handleEnableEditConnectionName);

        synchronizeCustomName();
            this.addCustomNameListener();
    }
// ---- [view methods]
    public void updateLanguageDependentContent () {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_customName.setText(bundle.getString("label_customName"));
            if (connectState == -1) {
                this.button_connectOrDisconnectPeer.setText(bundle.getString("button_connectOrDisconnectPeer_initial"));
            }
            else if (connectState == 0) {
                this.button_connectOrDisconnectPeer.setText(bundle.getString("button_connectOrDisconnectPeer_disconnected"));
            }
            else if (connectState == 1) {
                this.button_connectOrDisconnectPeer.setText(bundle.getString("button_connectOrDisconnectPeer_connected"));
            }
        this.label_remoteHostIp.setText(bundle.getString("label_remoteHostIp"));
        this.label_remoteHostPort.setText(bundle.getString("label_remoteHostPort"));
        this.label_localPort.setText(bundle.getString("label_localPort"));
        // <TODO>
        if (statusMessagesState == 0) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
        }
    }
    public void updateSocketInformation () {
        if (this.peerSocket.socket.getValue() != null) {
            this.textField_remoteHostIp.setText(this.peerSocket.socket.getValue().getInetAddress().getHostAddress());
            this.textField_remoteHostPort.setText(String.valueOf(this.peerSocket.socket.getValue().getPort()));
            this.textField_localPort.setText(String.valueOf(this.peerSocket.socket.getValue().getLocalPort()));
        }
    }
    public void synchronizeCustomName () {
        try {
            this.peerSocket.customName_lock.lock();
            this.peerSocket.customName.setValue(this.textField_customName.getText());
        }
        finally {
            this.peerSocket.customName_lock.unlock();
        }
    }
}
