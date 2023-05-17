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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SelectPeerToShareWith implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        SelectPeerToShareWith.class,
        "SelectPeerToShareWith",
        "SelectPeerToShareWith.fxml",
        "SelectPeerToShareWith.css",
        "ro.nq.metaexplorer.Dialogs.SelectPeerToShareWith"
    );
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
    public SelectPeerToShareWith(Terminal application) {
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
                System.out.println("{SelectPeerToShareWith} [] (FXMLLoader.load()) - Parent: " + this.parent);

            this.scene = new Scene(this.parent);
            this.window.setScene(this.scene);
        }
        catch (IOException exception) {
            System.out.println("{SelectPeerToShareWith} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        // <TODO>
        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::SelectPeerToShareWith");
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

// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        // <TODO>
    }
}
