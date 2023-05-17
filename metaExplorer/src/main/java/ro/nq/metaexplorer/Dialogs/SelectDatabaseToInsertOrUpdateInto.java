package ro.nq.metaexplorer.Dialogs;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SelectDatabaseToInsertOrUpdateInto implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        SelectDatabaseToInsertOrUpdateInto.class,
            "SelectDatabaseToInsertOrUpdateInto",
            "SelectDatabaseToInsertOrUpdateInto.fxml",
            "SelectDatabaseToInsertOrUpdateInto.css",
            "ro.nq.metaexplorer.Dialogs.SelectDatabaseToInsertOrUpdateInto"
    );
    public static final String initialItemsPerPage = String.valueOf(Terminal.initialItemsPerPage);
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public SelectDatabaseToInsertOrUpdateInto(Terminal application) {
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
            System.out.println("{SelectDatabaseConnections} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{SelectDatabaseConnections} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_close;
@FXML Label label_message;
@FXML ListView listView_databaseConnections;
@FXML Button button_select;
@FXML Button button_cancel;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing FXML: " + classConstants.debugClassName);

        assert this.base != null;
        assert this.button_close != null;
        assert this.label_message != null;
        assert this.listView_databaseConnections != null;
        assert this.button_select != null;
        assert this.button_cancel != null;

        updateLanguageDependentContent();
        this.application.observableLanguage.addListener(this.languageListener);
    }
// ---- [change language]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);
        // <TODO>
    }
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::CenterBorder");
            updateLanguageDependentContent();
        }
    };
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }
// ---- [event handlers]

}
