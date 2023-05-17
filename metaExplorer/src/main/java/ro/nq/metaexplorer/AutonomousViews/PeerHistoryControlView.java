package ro.nq.metaexplorer.AutonomousViews;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class PeerHistoryControlView {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        PeerHistoryControlView.class,
            "PeerHistoryControlView",
            "PeerHistoryControlView.fxml",
            "PeerHistoryControlView.css",
            "ro.nq.metaexplorer.AutonomousViews.PeerHistoryControlView"
    );
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public PeerHistoryControlView (Terminal application) {
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
            System.out.println("{PeerHistoryControlView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{PeerHistoryControlView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
// ---- [controllers]
    // none yet
// ---- [FXML model]
}
