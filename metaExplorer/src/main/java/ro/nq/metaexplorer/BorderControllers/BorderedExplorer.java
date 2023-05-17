package ro.nq.metaexplorer.BorderControllers;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class BorderedExplorer implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        BorderedExplorer.class,
        "BorderedExplorer",
        "BorderedExplorer.fxml",
        "BorderedExplorer.css",
        "ro.nq.metaexplorer.BorderControllers.BorderedExplorer"
    );
    public static final int topBorderHeight = 120;

    public static final int leftBorderHistoryDbBaseWidth = 600;
    public static final int leftBorderHistoryDbListViewWidth = 500;
    public static final int leftBorderHistoryDbListViewItemWidth = 475;
    public static final int leftBorderHistoryDbListViewHeight = 500;


    public static final int rightBorderBaseWidth = 600;
    public static final int rightBorderActiveListViewsWidth = 500;
    public static final int rightBorderActiveListViewsItemWidth = 475;
    public static final int rightBorderActiveDbListViewHeight = 300;
    public static final int rightBorderActivePeerListViewHeight = 300;

    public static final int bottomBorderBaseHeight = 950;
    public static final int bottomBorderListViewHeight = 850;

    public static final int centerFullscreenBaseWidth = 1600;
    public static final int centerFullscreenSubControllerListViewHeight = 900;
    public static final int centerNormalAutonomousViewWidth = 1600;
    public static final int centerNormalListViewFileMetadataHeight = 500;
    public static final int centerNormalListViewPagesHeight = 50;
    public static final int centerNormalListViewFileMetadataWidth = 1700;
    public static final int centerNormalListViewPagesWidth = 1700;
// ---- [extended logic state]
double previousGenerateFilteredQueueHeight;
double previousGenerateDatabaseQueryHeight;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public BorderedExplorer(Terminal application) {
        this.previousGenerateFilteredQueueHeight = 0;
        this.previousGenerateDatabaseQueryHeight = 0;

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
                System.out.println("{BorderedExplorer} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{BorderedExplorer} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }

        this.leftController = new LeftBorder(this.application);
            this.left.getChildren().add(this.leftController.parent);
        this.topController = new TopBorder(this.application);
            this.top.getChildren().add(this.topController.parent);
        this.rightController = new RightBorder(this.application);
            this.right.getChildren().add(this.rightController.parent);
        this.bottomController = new BottomBorder(this.application);
            this.bottom.getChildren().add(this.bottomController.parent);
        this.centerController = new CenterBorder(this.application);
            this.center.getChildren().add(this.centerController.parent);
    }
// ---- [controllers]
public LeftBorder leftController;
public TopBorder topController;
public RightBorder rightController;
public BottomBorder bottomController;
public CenterBorder centerController;
// ---- [FXML model]
@FXML BorderPane base;
@FXML VBox left;
@FXML VBox right;
@FXML VBox top;
@FXML VBox bottom;
@FXML VBox center;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing FXML: " + classConstants.debugClassName);

        assert this.base != null;
        assert this.left != null;
        assert this.right != null;
        assert this.top != null;
        assert this.bottom != null;
        assert this.center != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::BorderedExplorer");
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
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);
        // literally nothing to update in this view
    }
// ---- [sizing mechanisms]
    public void sizeTopBorderMinimize () {
        this.topController.base.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.topController.base.setMaxHeight(Region.USE_COMPUTED_SIZE);
    }
    public void sizeTopBorderMaximize () {
        this.topController.base.setPrefHeight(BorderedExplorer.topBorderHeight);
        this.topController.base.setMaxHeight(BorderedExplorer.topBorderHeight);
    }

    public void sizeLeftBorderMinimize () {
        this.leftController.base.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.leftController.base.setMaxWidth(Region.USE_COMPUTED_SIZE);

        this.leftController.databaseHistoryControlView.listView_historyDb.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.leftController.databaseHistoryControlView.listView_historyDb.setMaxWidth(Region.USE_COMPUTED_SIZE);

        this.leftController.databaseHistoryControlView.listView_historyDb.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.leftController.databaseHistoryControlView.listView_historyDb.setMaxHeight(Region.USE_COMPUTED_SIZE);
    }
    public void sizeSideEffectsLeftBorderMinimize () {
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setPrefWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getPrefWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setMaxWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getMaxWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getPrefWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getMaxWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.addNewFiles.listView_pageViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getPrefWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.addNewFiles.listView_pageViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getMaxWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setPrefWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getPrefWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setMaxWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getMaxWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getPrefWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getMaxWidth() + BorderedExplorer.leftBorderHistoryDbBaseWidth);
    }
    public void sizeLeftBorderMaximize () {
        this.leftController.base.setPrefWidth(BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.leftController.base.setMaxWidth(BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.leftController.databaseHistoryControlView.listView_historyDb.setPrefWidth(BorderedExplorer.leftBorderHistoryDbListViewWidth);
        this.leftController.databaseHistoryControlView.listView_historyDb.setMaxWidth(BorderedExplorer.leftBorderHistoryDbListViewWidth);

        this.leftController.databaseHistoryControlView.listView_historyDb.setPrefHeight(BorderedExplorer.leftBorderHistoryDbListViewHeight);
        this.leftController.databaseHistoryControlView.listView_historyDb.setMaxHeight(BorderedExplorer.leftBorderHistoryDbListViewHeight);

    }
    public void sizeSideEffectsLeftBorderMaximize () {
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setPrefWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getPrefWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setMaxWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getMaxWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getPrefWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getMaxWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.addNewFiles.listView_pageViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getPrefWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.addNewFiles.listView_pageViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getMaxWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setPrefWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getPrefWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setMaxWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getMaxWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);

        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getPrefWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getMaxWidth() - BorderedExplorer.leftBorderHistoryDbBaseWidth);
    }

    public void sizeCenterNormal () {
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setPrefWidth(BorderedExplorer.centerNormalAutonomousViewWidth);
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setMaxWidth(BorderedExplorer.centerNormalAutonomousViewWidth);

        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefWidth(BorderedExplorer.centerNormalListViewFileMetadataWidth);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxWidth(BorderedExplorer.centerNormalListViewFileMetadataWidth);

        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefHeight(BorderedExplorer.centerNormalListViewFileMetadataHeight);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxHeight(BorderedExplorer.centerNormalListViewFileMetadataHeight);

        this.centerController.addNewFiles.listView_pageViewCells.setPrefWidth(BorderedExplorer.centerNormalListViewPagesWidth);
        this.centerController.addNewFiles.listView_pageViewCells.setMaxWidth(BorderedExplorer.centerNormalListViewPagesWidth);

        this.centerController.addNewFiles.listView_pageViewCells.setPrefHeight(BorderedExplorer.centerNormalListViewPagesHeight);
        this.centerController.addNewFiles.listView_pageViewCells.setMaxHeight(BorderedExplorer.centerNormalListViewPagesHeight);

        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setPrefWidth(BorderedExplorer.centerNormalAutonomousViewWidth);
        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setMaxWidth(BorderedExplorer.centerNormalAutonomousViewWidth);

        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefWidth(BorderedExplorer.centerNormalListViewFileMetadataWidth);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxWidth(BorderedExplorer.centerNormalListViewFileMetadataWidth);

        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefHeight(BorderedExplorer.centerNormalListViewFileMetadataHeight);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxHeight(BorderedExplorer.centerNormalListViewFileMetadataHeight);
    }
    public void sizeSideEffectsGeneratedFilteredQueueMinimize() {
        this.previousGenerateFilteredQueueHeight = this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getHeight() - this.centerController.addNewFiles.generateFilteredQueueAutonomousView.container_header.getHeight();

        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefHeight(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getPrefHeight() + previousGenerateFilteredQueueHeight);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxHeight(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getMaxHeight() + previousGenerateFilteredQueueHeight);
    }
    public void sizeSideEffectsGeneratedFilteredQueueMaximize() {
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefHeight(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getPrefHeight() - previousGenerateFilteredQueueHeight);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxHeight(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getMaxHeight() - previousGenerateFilteredQueueHeight);
        this.previousGenerateFilteredQueueHeight = 0;
    }
    public void sizeSideEffectsGeneratedDatabaseQueryMinimize() {
        this.previousGenerateDatabaseQueryHeight = this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getHeight() - this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.container_header.getHeight();

        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefHeight(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getPrefHeight() + previousGenerateDatabaseQueryHeight);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxHeight(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getMaxHeight() + previousGenerateDatabaseQueryHeight);
    }
    public void sizeSideEffectsGeneratedDatabaseQueryMaximize() {
        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefHeight(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getPrefHeight() - previousGenerateDatabaseQueryHeight);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxHeight(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getMaxHeight() - previousGenerateDatabaseQueryHeight);

        this.previousGenerateDatabaseQueryHeight = 0;
    }

    public void sizeRightBorderMinimize () {
        this.rightController.base.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.rightController.base.setMaxWidth(Region.USE_COMPUTED_SIZE);

        this.rightController.databaseActiveControlView.listView_activeDb.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.rightController.databaseActiveControlView.listView_activeDb.setMaxWidth(Region.USE_COMPUTED_SIZE);

        this.rightController.databaseActiveControlView.listView_activeDb.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.rightController.databaseActiveControlView.listView_activeDb.setMaxHeight(Region.USE_COMPUTED_SIZE);

        this.rightController.peerActiveControlView.listView_activePeers.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.rightController.peerActiveControlView.listView_activePeers.setMaxWidth(Region.USE_COMPUTED_SIZE);

        this.rightController.peerActiveControlView.listView_activePeers.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.rightController.peerActiveControlView.listView_activePeers.setMaxHeight(Region.USE_COMPUTED_SIZE);
    }
    public void sizeSideEffectsRightBorderMinimize () {
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setPrefWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getPrefWidth() + BorderedExplorer.rightBorderBaseWidth);
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setMaxWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getMaxWidth() + BorderedExplorer.rightBorderBaseWidth);

        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getPrefWidth() + BorderedExplorer.rightBorderBaseWidth);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getMaxWidth() + BorderedExplorer.rightBorderBaseWidth);

        this.centerController.addNewFiles.listView_pageViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getPrefWidth() + BorderedExplorer.rightBorderBaseWidth);
        this.centerController.addNewFiles.listView_pageViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getMaxWidth() + BorderedExplorer.rightBorderBaseWidth);

        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setPrefWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getPrefWidth() + BorderedExplorer.rightBorderBaseWidth);
        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setMaxWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getMaxWidth() + BorderedExplorer.rightBorderBaseWidth);

        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getPrefWidth() + BorderedExplorer.rightBorderBaseWidth);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getMaxWidth() + BorderedExplorer.rightBorderBaseWidth);
    }
    public void sizeRightBorderMaximize () {
        this.rightController.base.setPrefWidth(BorderedExplorer.rightBorderBaseWidth);
        this.rightController.base.setMaxWidth(BorderedExplorer.rightBorderBaseWidth);

        this.rightController.databaseActiveControlView.listView_activeDb.setPrefWidth(BorderedExplorer.rightBorderActiveListViewsWidth);
        this.rightController.databaseActiveControlView.listView_activeDb.setMaxWidth(BorderedExplorer.rightBorderActiveListViewsWidth);

        this.rightController.databaseActiveControlView.listView_activeDb.setPrefHeight(BorderedExplorer.rightBorderActiveDbListViewHeight);
        this.rightController.databaseActiveControlView.listView_activeDb.setMaxHeight(BorderedExplorer.rightBorderActiveDbListViewHeight);

        this.rightController.peerActiveControlView.listView_activePeers.setPrefWidth(BorderedExplorer.rightBorderActiveListViewsWidth);
        this.rightController.peerActiveControlView.listView_activePeers.setMaxWidth(BorderedExplorer.rightBorderActiveListViewsWidth);

        this.rightController.peerActiveControlView.listView_activePeers.setPrefHeight(BorderedExplorer.rightBorderActivePeerListViewHeight);
        this.rightController.peerActiveControlView.listView_activePeers.setMaxHeight(BorderedExplorer.rightBorderActivePeerListViewHeight);
    }
    public void sizeSideEffectsRightBorderMaximize () {
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setPrefWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getPrefWidth() - BorderedExplorer.rightBorderBaseWidth);
        this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.setMaxWidth(
                this.centerController.addNewFiles.generateFilteredQueueAutonomousView.base.getMaxWidth() - BorderedExplorer.rightBorderBaseWidth);

        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getPrefWidth() - BorderedExplorer.rightBorderBaseWidth);
        this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_paginatedQueuedFileViewCells.getMaxWidth() - BorderedExplorer.rightBorderBaseWidth);

        this.centerController.addNewFiles.listView_pageViewCells.setPrefWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getWidth() - BorderedExplorer.rightBorderBaseWidth);
        this.centerController.addNewFiles.listView_pageViewCells.setMaxWidth(
                this.centerController.addNewFiles.listView_pageViewCells.getWidth() - BorderedExplorer.rightBorderBaseWidth);

        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setPrefWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getWidth() - BorderedExplorer.rightBorderBaseWidth);
        this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.setMaxWidth(
                this.centerController.queryDatabase.generateDatabaseQueryAutonomousView.base.getWidth() - BorderedExplorer.rightBorderBaseWidth);

        this.centerController.queryDatabase.listview_selectedQueryFiles.setPrefWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getWidth() - BorderedExplorer.rightBorderBaseWidth);
        this.centerController.queryDatabase.listview_selectedQueryFiles.setMaxWidth(
                this.centerController.queryDatabase.listview_selectedQueryFiles.getWidth() - BorderedExplorer.rightBorderBaseWidth);
    }

    public void sizeBottomBorderMinimize () {
        this.application.mainLayoutController.top.getChildren().add(this.application.mainLayoutController.topController.parent);
        this.application.mainLayoutController.left.getChildren().add(this.application.mainLayoutController.leftController.parent);
        this.application.mainLayoutController.center.getChildren().add(this.application.mainLayoutController.centerController.parent);
        this.application.mainLayoutController.right.getChildren().add(this.application.mainLayoutController.rightController.parent);

        this.bottomController.base.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.bottomController.base.setMaxHeight(Region.USE_COMPUTED_SIZE);

        this.bottomController.listView_incomingFiles.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.bottomController.listView_incomingFiles.setMaxHeight(Region.USE_COMPUTED_SIZE);
    }
    public void sizeBottomBorderMaximize () {
        this.application.mainLayoutController.top.getChildren().clear();
        this.application.mainLayoutController.left.getChildren().clear();
        this.application.mainLayoutController.center.getChildren().clear();
        this.application.mainLayoutController.right.getChildren().clear();

        this.bottomController.base.setPrefHeight(BorderedExplorer.bottomBorderBaseHeight);
        this.bottomController.base.setMaxHeight(BorderedExplorer.bottomBorderBaseHeight);

        this.bottomController.listView_incomingFiles.setPrefHeight(BorderedExplorer.bottomBorderListViewHeight);
        this.bottomController.listView_incomingFiles.setMaxHeight(BorderedExplorer.bottomBorderListViewHeight);
    }
}
