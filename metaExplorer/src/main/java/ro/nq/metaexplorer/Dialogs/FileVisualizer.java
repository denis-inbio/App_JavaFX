package ro.nq.metaexplorer.Dialogs;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.FileMetadata;
import ro.nq.metaexplorer.ItemViews.FileMetadataView;
import ro.nq.metaexplorer.Utilities.Filesystem;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileVisualizer implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
            CreateNewPeerConnection.class,
            "FileVisualizer",
            "FileVisualizer.fxml",
            "FileVisualizer.css",
            "ro.nq.metaexplorer.Dialogs.FileVisualizer"
    );
    public static final int contentWidth = 900;
    public static final int contentHeight = 800;
    public static final String previousPageIconPath = "icons/left_arrow.png";
    public static final String nextPageIconPath = "icons/right_arrow.png";
    public static final String temporaryPdfDirectory = "temp";
// ---- [extended logic state]
    // <TODO> Image, PDocument, String, etc. -> pass in the constructor
PDDocument pdfFile;
PDFRenderer pdfRenderer;
int currentPage;
Image imageFile;
String textFile;
// ---- [state, ctor]
public FileMetadataView fileMetadataView;
Terminal application;
FXMLLoader fxmlLoader;
public Stage window;
Scene scene;
public Parent parent;
    public FileVisualizer(Terminal application, FileMetadataView fileMetadataView, PDDocument pdfFile, Image imageFile, String textFile) {
        assert application != null;
            this.application = application;

        assert fileMetadataView != null;
            this.fileMetadataView = fileMetadataView;

        assert (pdfFile != null || imageFile != null || textFile != null);
            this.pdfFile = pdfFile;
                if (this.pdfFile != null) {
                    this.pdfRenderer = new PDFRenderer(this.pdfFile);
                }
                else {
                    this.pdfRenderer = null;
                }
                this.currentPage = 0;
            this.imageFile = imageFile;
            this.textFile = textFile;


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
            System.out.println("{FileVisualizer} [] (FXMLLoader.load()) - Parent: " + this.parent);

            this.scene = new Scene(this.parent);
            this.window.setScene(this.scene);
        }
        catch (IOException exception) {
            System.out.println("{FileVisualizer} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;
@FXML TextArea textArea_contentText;
@FXML VBox container_contentPdf;
@FXML ImageView imageView_pdfContent;
@FXML Button button_prevPage;
@FXML Button button_nextPage;
@FXML Label label_pagePrefix;
@FXML Label label_currentPage;
@FXML Label label_pageMiddle;
@FXML Label label_totalPages;
@FXML HBox container_contentImage;
@FXML ImageView imageView_contentImage;
@FXML Label label_tagsHeader;
@FXML TextArea textArea_tags;
@FXML Button button_commitTags;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;

        assert this.textArea_contentText != null;
            this.textArea_contentText.managedProperty().bind(this.textArea_contentText.visibleProperty());
            this.textArea_contentText.setVisible(false);
            if (this.textFile != null) {
                this.textArea_contentText.setVisible(true);
                this.textArea_contentText.setText(this.textFile);
                this.textArea_contentText.setPrefWidth(FileVisualizer.contentWidth);
                this.textArea_contentText.setPrefHeight(FileVisualizer.contentHeight);
            }

        assert this.container_contentPdf != null;
            this.container_contentPdf.managedProperty().bind(this.container_contentPdf.visibleProperty());
            this.container_contentPdf.setVisible(false);
            if (this.textFile == null && this.pdfFile != null) {
                this.container_contentPdf.setVisible(true);
                this.container_contentPdf.setPrefWidth(FileVisualizer.contentWidth);
                this.container_contentPdf.setPrefHeight(FileVisualizer.contentHeight);
            }
        assert this.imageView_pdfContent != null;
            if (this.textFile == null && this.pdfFile != null) {
                this.renderCurrentPdfPage();
            }
        assert this.button_prevPage != null;
            Image previousPageIcon = Filesystem.loadImage(application, FileVisualizer.previousPageIconPath);
            ImageView previousPageIconView = new ImageView(previousPageIcon);
                this.button_prevPage.setGraphic(previousPageIconView);
            this.button_prevPage.setOnAction(this::handlePreviousPage);
        assert this.button_nextPage != null;
            Image nextPageIcon = Filesystem.loadImage(application, FileVisualizer.nextPageIconPath);
            ImageView nextPageIconView = new ImageView(nextPageIcon);
                this.button_nextPage.setGraphic(nextPageIconView);
            this.button_nextPage.setOnAction(this::handleNextPage);
        assert this.label_pagePrefix != null;
        assert this.label_currentPage != null;
        assert this.label_pageMiddle != null;
        assert this.label_totalPages != null;
            if (this.pdfFile != null) {
                this.label_totalPages.setText(String.valueOf(this.pdfFile.getNumberOfPages()));
            }

        assert this.container_contentImage != null;
            this.container_contentImage.managedProperty().bind(this.container_contentImage.visibleProperty());
            this.container_contentImage.setVisible(false);
        assert this.imageView_contentImage != null;
            if (textFile == null && pdfFile == null && imageFile != null) {
                this.container_contentImage.setVisible(true);
                this.imageView_contentImage.setImage(imageFile);
                this.imageView_contentImage.setPreserveRatio(true);
                    double minWidth = imageFile.getWidth();
                        if (minWidth > FileVisualizer.contentWidth) {
                            minWidth = FileVisualizer.contentWidth;
                        }
                    double minHeight = imageFile.getHeight();
                    if (minHeight > FileVisualizer.contentHeight) {
                        minHeight = FileVisualizer.contentHeight;
                    }
                this.container_contentImage.setPrefWidth(minWidth);
                this.container_contentImage.setPrefHeight(minHeight);
                this.imageView_contentImage.setFitWidth(minWidth);
                this.imageView_contentImage.setFitHeight(minHeight);
            }

        assert this.label_tagsHeader != null;
        assert this.textArea_tags != null;
            this.textArea_tags.setText(this.fileMetadataView.textArea_tags.getText());
        assert this.button_commitTags != null;
            this.button_commitTags.setOnAction(this::handleCommitTags);

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::FileVisualizer");
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
    public void handleCommitTags(ActionEvent event) {
        this.fileMetadataView.textArea_tags.setText(this.textArea_tags.getText());
    }
    public void handlePreviousPage (ActionEvent event) {
        this.currentPage--;
        this.renderCurrentPdfPage();
    }
    public void handleNextPage (ActionEvent event) {
        this.currentPage++;
        this.renderCurrentPdfPage();
    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_tagsHeader.setText(bundle.getString("label_tagsHeader"));
        this.label_pagePrefix.setText(bundle.getString("label_pagePrefix"));
        this.label_pageMiddle.setText(bundle.getString("label_pageMiddle"));
        this.button_commitTags.setText(bundle.getString("button_commitTags"));
    }

    public void renderCurrentPdfPage () {
            if (this.currentPage < 0) {
                this.currentPage = 0;
            }
            else if (this.pdfFile.getNumberOfPages() < this.currentPage) {
                this.currentPage = this.pdfFile.getNumberOfPages() - 1;
            }

        try {
            Image image = SwingFXUtils.toFXImage(pdfRenderer.renderImage(this.currentPage), null);
            this.imageView_pdfContent.setImage(image);
            this.imageView_pdfContent.setPreserveRatio(true);
            this.imageView_pdfContent.setFitWidth(FileVisualizer.contentWidth);
            this.imageView_pdfContent.setFitHeight(FileVisualizer.contentHeight);
            this.label_currentPage.setText(String.valueOf(this.currentPage + 1));

            // <TODO> what about width and height ?
        }
        catch (IOException exception) {
            System.out.println("{FileVisualizer} [IOException] (PDFRenderer.renderImage()) - " + exception.getMessage() + "\n" + exception.getCause());
            // <TODO> Ui message
        }
    }
}
