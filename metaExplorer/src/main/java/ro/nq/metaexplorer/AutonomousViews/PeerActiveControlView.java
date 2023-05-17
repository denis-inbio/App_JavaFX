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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.Dialogs.CreateNewPeerConnection;
import ro.nq.metaexplorer.ItemViews.PeerSocketView;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class PeerActiveControlView implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        PeerActiveControlView.class,
            "PeerActiveControlView",
            "PeerActiveControlView.fxml",
            "PeerActiveControlView.css",
            "ro.nq.metaexplorer.AutonomousViews.PeerActiveControlView"
    );
// ---- [extended logic state]
    // -1 -> initial
    // ... <TODO>
int statusMessageState;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public PeerActiveControlView (Terminal application) {
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
            System.out.println("{PeerActiveControlView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{PeerActiveControlView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
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
public @FXML ListView<PeerSocketView> listView_activePeers;
@FXML Button button_connectToNewPeer;
@FXML Button button_disconnectSelectedPeers;
@FXML Label label_statusMessage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.button_minimizeOrMaximize != null;
            Image minimizeIcon = Filesystem.loadImageResource(application, PeerActiveControlView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
        assert this.label_header != null;
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(true);
        assert this.listView_activePeers != null;
            this.listView_activePeers.setItems(this.application.observableActivePeerViews);
            this.listView_activePeers.setCellFactory(items -> new PeerSocketView.PeerSocketViewCell(this.application));
        assert this.button_connectToNewPeer != null;
            this.button_connectToNewPeer.setOnAction(this::handleConnectionToNewPeer);
        assert this.button_disconnectSelectedPeers != null;
            this.button_disconnectSelectedPeers.setOnAction(this::handleDisconnectSelectedPeers);
        assert this.label_statusMessage != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::PeerActiveControlView");
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

        Image maximizeIcon = Filesystem.loadImageResource(application, PeerActiveControlView.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeIcon);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeIcon = Filesystem.loadImageResource(application, PeerActiveControlView.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeIcon);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
    }
    public void handleConnectionToNewPeer (ActionEvent event) {
        CreateNewPeerConnection createNewPeerConnection = new CreateNewPeerConnection(this.application);
            createNewPeerConnection.window.show();
    }
    public void handleDisconnectSelectedPeers (ActionEvent event) {
        ObservableList<PeerSocketView> activePeerViews = this.listView_activePeers.getSelectionModel().getSelectedItems();
            for (PeerSocketView activePeerView : activePeerViews) {
                this.application.removeObservableActivePeer(activePeerView.peerSocket, false);
            }
            this.application.recomputeObservableActivePeerViews();
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_header.setText(bundle.getString("label_header"));
        this.button_connectToNewPeer.setText(bundle.getString("button_connectToNewPeer"));
        this.button_disconnectSelectedPeers.setText(bundle.getString("button_disconnectSelectedPeers"));

        if (this.statusMessageState == -1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
            this.label_statusMessage.setStyle("-fx-text-fill: black");
        }
    }
}
