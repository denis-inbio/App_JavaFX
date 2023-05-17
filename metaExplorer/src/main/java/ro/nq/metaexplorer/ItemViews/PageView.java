package ro.nq.metaexplorer.ItemViews;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class PageView implements Initializable {
// ---- [ListCell variant]
    public static class PageViewCell extends ListCell<PageView> {
        public Terminal application;
        public PageView pageView;
        public PageViewCell (Terminal application) {
            this.application = application;
            this.pageView = null;
        }

        public void updateItem (PageView item, boolean empty) {
            super.updateItem(item, empty);
            this.pageView = item;

            if (item != null) {
                this.setGraphic(this.pageView.parent);
                this.setAlignment(Pos.CENTER);
            }
            else {
                this.setGraphic(null);
            }
        }
    };
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        PageView.class,
        "PageView",
        "PageView.fxml",
        "PageView.css",
        "ro.nq.metaexplorer.ItemViews.PageView"
    );
// ---- [extended logic state]
public SimpleBooleanProperty isSelected;
// ---- [state, ctor]
Integer pageIndex;
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public PageView (Terminal application, Integer pageIndex) {
        this.isSelected = new SimpleBooleanProperty(false);

        assert pageIndex != null;
            this.pageIndex = pageIndex;
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
            System.out.println("{PageView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{PageView} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML HBox base;
@FXML Button button_page;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.button_page != null;
            this.button_page.setOnAction(this::handlePagePressed);

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::PageView");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }

    InvalidationListener selectionListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Selection invalidation in ::PageView");
            updateSelectedView();
        }
    };
    public void addSelectionListener () {
        this.isSelected.addListener(this.selectionListener);
    }
    public void removeSelectionListener () {
        this.isSelected.removeListener(this.selectionListener);
    }

    public void addAllListeners () {
        this.addLanguageListener();
        this.addSelectionListener();
    }
    public void removeAllListeners () {
        this.removeLanguageListener();
        this.removeSelectionListener();
    }
// ---- [event handlers]
    public void handlePagePressed(ActionEvent event) {
        this.application.setPageIndex(this.pageIndex);
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.button_page.setText(bundle.getString("page_prefix") + " " + String.valueOf(this.pageIndex + 1));
    }
    public void updateSelectedView () {
        if (this.isSelected.getValue() == false) {
            this.base.setStyle("-fx-background-color: transparent");
        }
        else {
            this.base.setStyle("-fx-background-color: grey");
        }
    }
}