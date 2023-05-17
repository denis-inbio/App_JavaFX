package ro.nq.metaexplorer.ItemViews;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.FileMetadata;
import ro.nq.metaexplorer.Dialogs.FileVisualizer;
import ro.nq.metaexplorer.Utilities.Filesystem;
import ro.nq.metaexplorer.Utilities.Queries;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class FileMetadataView implements Initializable {
// ---- [ListCell variant]
    public static class FileMetadataViewCell extends ListCell<FileMetadataView> {
        public Terminal application;
        public FileMetadataView fileMetadataView;
        public FileMetadataViewCell(Terminal application) {
            this.application = application;
            this.fileMetadataView = null;
        }

        public void updateItem (FileMetadataView item, boolean empty) {
            super.updateItem(item, empty);
            this.fileMetadataView = item;

            if (item != null) {
                setGraphic(this.fileMetadataView.parent);
            }
            else {
                setGraphic(null);
            }
        }
    };
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        FileMetadataView.class,
        "FileMetadataView",
        "FileMetadataView.fxml",
        "FileMetadataView.css",
        "ro.nq.metaexplorer.ItemViews.FileMetadataView"
    );
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
    public static final String closeIconPath = "icons/close.png";
// ---- [extended logic state]
    // statusState:
        // -1 -> initial
        // 0 -> saved successfully, 1 -> failed to save
        // 2 -> successfully visualized file, 3 -> failed to visualize file
        // 4 -> successfully inserted file, 5 -> failed to insert file
        // 6 -> successfully updated file, 7 -> failed to update file
        // 8 -> queued file for sharing, 9 -> failed to queue file for sharing
int statusState;
    // metadataOrigin:
        // -2 -> invalid [no source no content]
        // -1 -> [initial state]
        // 0 -> QueuedFile [filesystem]
        // 1 -> SelectedFile [database]
        // 2 -> ? [peer]
        // 3 -> ? [unknown source, but content is set]
int metadataOrigin;
// ---- [state, ctor]
public FileMetadata fileMetadata;
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public FileMetadataView(Terminal application, FileMetadata fileMetadata) {
        this.statusState = -1;
        this.metadataOrigin = -1;

        assert fileMetadata != null;
            this.fileMetadata = fileMetadata;
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
                System.out.println("{FileMetadataView} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{FileMetadataView} [IOException] (FXMLLoader.load()) - this? - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
@FXML VBox base;
@FXML Button button_minimizeOrMaximize;
@FXML Button button_closeItem;
@FXML HBox minimizable;
@FXML Label label_fileName;
@FXML TextField textField_fileName;
@FXML TextField textField_fileType;
@FXML Label label_filesSize;
@FXML Label label_sizeMeasurementUnit;
@FXML Label label_filePathName;
@FXML TextField textField_filePathName;
@FXML Label label_databaseId;
@FXML TextField textField_databaseId;
@FXML Label label_tags;
public @FXML TextArea textArea_tags;
@FXML Button button_saveTo;
@FXML Button button_visualizeWith;
@FXML Button button_insertOrUpdate;
@FXML Button button_removeFromDatabase;
@FXML Button button_shareWith;
@FXML Label label_statusMessage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (this.fileMetadata.sourceSocket == null && this.fileMetadata.file != null && this.fileMetadata.id == -1) {
            // filesystem
            this.metadataOrigin = 0;
        }
        else if (this.fileMetadata.sourceSocket == null && this.fileMetadata.file == null && this.fileMetadata.id != -1) {
            // database
            this.metadataOrigin = 1;
        }
        else if (this.fileMetadata.sourceSocket != null && this.fileMetadata.id == -1 && this.fileMetadata.content != null) {
            // peer
            this.metadataOrigin = 2;
        }
        else if (this.fileMetadata.sourceSocket == null && this.fileMetadata.file == null && this.fileMetadata.id == -1 && this.fileMetadata.content == null) {
            // not filesystem, not database, not peer, content is available
            this.metadataOrigin = 3;
        }
        else if (this.fileMetadata.sourceSocket == null && this.fileMetadata.file == null && this.fileMetadata.id == -1 && this.fileMetadata.content != null) {
            // not filesystem, not database, not peer, no content
            this.metadataOrigin = -2;
        }

        // [common to all states]
        assert this.base != null;
        assert this.button_minimizeOrMaximize != null;
            Image minimizeIcon = Filesystem.loadImageResource(application, FileMetadataView.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeIcon);
               this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(true);
        assert this.button_closeItem != null;
            Image closeIcon = Filesystem.loadImageResource(application, FileMetadataView.closeIconPath);
            ImageView closeImageView = new ImageView(closeIcon);
                this.button_closeItem.setGraphic(closeImageView);
        assert this.label_fileName != null;
        assert this.label_filesSize != null;
        assert this.label_sizeMeasurementUnit != null;
        assert this.label_filePathName != null;
        assert this.label_databaseId != null;
        assert this.label_tags != null;
        assert this.textField_filePathName != null;
        assert this.textField_databaseId != null;
        assert this.textArea_tags != null;
        assert this.button_saveTo != null;
            if (this.metadataOrigin != -1 && this.metadataOrigin != -2) {
                this.button_saveTo.setOnAction(this::handleSaveTo);
            }
        assert this.button_visualizeWith != null;
            if (this.metadataOrigin != -1 && this.metadataOrigin != -2) {
                this.button_visualizeWith.setOnAction(this::handleVisualize);
            }
        assert this.button_removeFromDatabase != null;
        assert this.button_shareWith != null;
            if (this.metadataOrigin != -1 && this.metadataOrigin != -2) {
                this.button_shareWith.setOnAction(this::handleShareWith);
            }
        assert this.label_statusMessage != null;

        if (this.metadataOrigin == 0) {
            assert this.button_closeItem != null;
                this.button_closeItem.setOnAction(this::handleCloseQueuedFile);
            assert this.textField_fileName != null;
                this.textField_fileName.setText(Filesystem.extractFileName(this.fileMetadata.file));
            assert this.textField_fileType != null;
                this.textField_fileType.setText(Filesystem.extractFileExtension(this.fileMetadata.file));
            assert this.button_insertOrUpdate != null;
                this.button_insertOrUpdate.setOnAction(this::handleInsertInDatabase);
            assert this.button_removeFromDatabase != null;
                this.button_removeFromDatabase.setVisible(false);
                this.button_removeFromDatabase.setManaged(false);
        }
        else if (this.metadataOrigin == 1) {
            assert this.button_closeItem != null;
                this.button_closeItem.setOnAction(this::handleCloseSelectedFile);
            assert this.textField_fileName != null;
                this.textField_fileName.setText(this.fileMetadata.fileName);
            assert this.textField_fileType != null;
                this.textField_fileType.setText(this.fileMetadata.fileType);
            assert this.button_insertOrUpdate != null;
                this.button_insertOrUpdate.setOnAction(this::handleUpdateInDatabase);
            assert this.button_removeFromDatabase != null;
                this.button_removeFromDatabase.setOnAction(this::handleRemoveFromDatabase);
        }
        else if (this.metadataOrigin == 2) {
//            this.insertState = ?; // presumably false, because sharing only downloads the file to a temp directory
            // <TODO> when downloading files from peers, give a count as the filename; preferably, store them as temporary files and delete on close (*) [but not necessarily]
            // or, check right after downloading whether the file already exists; the download should be done straight onto disk; the Files.contentComparison() should also
            // be done "with external memory"; if this is a duplicate, then remove it; but what if the tags differ ? shall they be merged ? how do I ensure that each FileMetadata
            // has a unique corresponding File ?

            // <TODO> [peer]
        }
        else {
            // <TODO> invalid state Ui
        }

        this.synchronizeFileName();
            this.addTextFieldFileNameListener();
        this.synchronizeFileType();
            this.addTextFieldFileTypeListener();
        this.initialTagsFromLogicToUi();
            this.addTextAreaTagsListener();
        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::FilesystemMetadataView");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }

    InvalidationListener textFieldFileNameListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("File name invalidation in ::FilesystemMetadataView");
            synchronizeFileName();
        }
    };
    public void addTextFieldFileNameListener () {
        this.textField_fileName.textProperty().addListener(this.textFieldFileNameListener);
    }
    public void removeTextFieldFileNameListener () {
        this.textField_fileName.textProperty().removeListener(this.textFieldFileNameListener);
    }

    InvalidationListener textFieldFileTypeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("File type invalidation in ::FilesystemMetadataView");
            synchronizeFileType();
        }
    };
    public void addTextFieldFileTypeListener () {
        this.textField_fileType.textProperty().addListener(this.textFieldFileTypeListener);
    }
    public void removeTextFieldFileTypeListener () {
        this.textField_fileType.textProperty().removeListener(this.textFieldFileTypeListener);
    }

    InvalidationListener textAreaTagsListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Tags invalidation in ::FilesystemMetadataView");
            synchronizeTags();
        }
    };
    public void addTextAreaTagsListener () {
        this.textArea_tags.textProperty().addListener(this.textAreaTagsListener);
    }
    public void removeTextAreaTagsListener () {
        this.textArea_tags.textProperty().removeListener(this.textAreaTagsListener);
    }

    InvalidationListener databaseIdListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Database ID invalidation in ::FilesystemMetadataView");
            synchronizeDatabaseId();
        }
    };
    public void addDatabaseIdListener () {
        this.textArea_tags.textProperty().addListener(this.databaseIdListener);
    }
    public void removeDatabaseIdListener () {
        this.textArea_tags.textProperty().removeListener(this.databaseIdListener);
    }

    public void addAllListeners () {
        addLanguageListener();
        addTextFieldFileNameListener();
        addTextFieldFileTypeListener();
        addTextAreaTagsListener();
        addDatabaseIdListener();
    }
    public void removeAllListeners() {
        removeLanguageListener();
        removeTextFieldFileNameListener();
        removeTextFieldFileTypeListener();
        removeTextAreaTagsListener();
        removeDatabaseIdListener();
    }
// ---- [socket]
    public void closeSocket () {
        if (this.fileMetadata.sourceSocket != null) {
            try {
                this.fileMetadata.sourceSocket.close();
                this.fileMetadata.sourceSocket = null;
            }
            catch (IOException exception) {
                System.out.println("{FileMetadataView.closeSocket()} [IOException] (Socket.close()) - " + exception.getMessage() + "\n" + exception.getMessage());
            }
        }
    }
// ---- [event handlers]
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        Image maximizeIcon = Filesystem.loadImageResource(application, FileMetadataView.maximizeIconPath);
        ImageView maximizeImageView = new ImageView(maximizeIcon);
            this.button_minimizeOrMaximize.setGraphic(maximizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeIcon = Filesystem.loadImageResource(application, FileMetadataView.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeIcon);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
    }
    public void handleRemoveFromDatabase (ActionEvent event) {
        if (this.fileMetadata.id != -1 && this.fileMetadata.connection != null) {
            PreparedStatement preparedStatement = Queries.prepareRemoveByIdStatement(this.fileMetadata.connection, this.fileMetadata.id);
                if (preparedStatement != null) {
                    try {
                        preparedStatement.execute();
                    }
                    catch (SQLException exception) {
                        System.out.println("{FileMetadataView.handleRemoveFromDatabase()} [SQLException] (PreparedStatement.execute()) - " + exception.getMessage() + "\n" + exception.getCause());
                    }
                }
        }
        else {
            System.out.println("{FileMetadataView.handleRemoveFromDatabase()} [SQLException] (PreparedStatement.execute()) - invalid executing context of `remove from database`");
        }
    }
    public void handleSaveTo (ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directoryFile = directoryChooser.showDialog(this.application.window);
        File file = Paths.get(directoryFile.getAbsolutePath(), this.fileMetadata.fileName + "." + this.fileMetadata.fileType).toFile();
            if (this.fileMetadata.file != null || this.fileMetadata.content != null) {
                try {

                    if (this.fileMetadata.content == null) {
                        FileInputStream fileInputStream = new FileInputStream(this.fileMetadata.file);
                        this.fileMetadata.content = fileInputStream.readAllBytes();
                            fileInputStream.close();
                    }

                    Files.write(file.toPath(), this.fileMetadata.content);
                    this.statusState = 0;
                }
                catch (FileNotFoundException exception) {
                    System.out.println("{FileMetadataView.handleSaveTo()} [FileNotFoundException] (new FileInputStream()) - " + exception.getMessage() + "\n" + exception.getCause());
                    this.statusState = -1;
                }
                catch (IOException exception) {
                    System.out.println("{FileMetadataView.handleSaveTo()} [IOException] (Files.write()) - " + exception.getMessage() + "\n" + exception.getCause());
                    this.statusState = -1;
                }
            }

        this.updateStatusMessage();
    }
    public void handleVisualize(ActionEvent event) {
        if (this.textField_fileType.getText().contains("pdf")) {
            try {
                PDDocument pdfFile = PDDocument.load(this.fileMetadata.file);

                FileVisualizer fileVisualizer = new FileVisualizer(this.application, this, pdfFile, null, null);
                    fileVisualizer.window.showAndWait();

                pdfFile.close();
                this.statusState = 2;
            }
            catch (IOException exception) {
                System.out.println("{FileMetadataView.handleVisualize()} [IOException] (PDDocument.load() | PDDocument.close()) - " + exception.getMessage() + "\n" + exception.getCause());
                this.statusState = 3;
            }
        }
        else if (this.textField_fileType.getText().contains("jpg") ||
                 this.textField_fileType.getText().contains("jpeg") ||
                 this.textField_fileType.getText().contains("png"))
        {
            Image image = Filesystem.loadImage(this.application, this.fileMetadata.file.getAbsolutePath());
                if (image != null) {
                    FileVisualizer fileVisualizer = new FileVisualizer(this.application, this, null, image, null);
                       fileVisualizer.window.showAndWait();

                   this.statusState = 2;
                }
                else {
                    this.statusState = 3;
                }
        }
        else if (this.textField_fileType.getText().contains("txt")) {
            try {
                FileInputStream fileInputStream = new FileInputStream(this.fileMetadata.file);
                String content = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);

                FileVisualizer fileVisualizer = new FileVisualizer(this.application, this, null, null, content);
                    fileVisualizer.window.showAndWait();

                this.statusState = 2;
            }
            catch (IOException exception) {
                System.out.println("{FileMetadataView.handleVisualize()} [IOException] (new FileInputStream() | FileInputStream.readAllBytes()) - " + exception.getMessage() + "\n" + exception.getCause());
                this.statusState = 3;
            }
        }
        else {
            // <TODO> Ui error message "unknown/unsupported file format"
            this.statusState = 3;   // <TODO> not the correct message
        }

        this.updateStatusMessage();
    }
    public void handleInsertInDatabase (ActionEvent event) {
        // <TODO> (*) actually, first check whether the id is != -1, this might become an update instead; maybe try to search by the id, and deduce something
            // also, I don't want to use an SQLException message to deduce if the content is unique or not,
            // so I also need a select query for this -> Queries.isContentUnique(byte[] content)

        // 0 -> saved successfully, 1 -> failed to save
        // 2 -> successfully visualized file, 3 -> failed to visualize file
        // 4 -> successfully inserted file, 5 -> failed to insert file
        // 6 -> successfully updated file, 7 -> failed to update file
        // 8 -> queued file for sharing, 9 -> failed to queue file for sharing

        try {
            Connection activeConnection = this.application.mainLayoutController.centerController.addNewFiles.generateFilteredQueueAutonomousView.comboBox_selectAnActiveDatabase.getSelectionModel().getSelectedItem().databaseEntry.connection.getValue();
            if (activeConnection == null) {
                // <TODO> status message: connection failed
                this.statusState = 5;
                this.updateStatusMessage();

                return;
            }

            File file = this.fileMetadata.file;
                if (file == null) {
                    // <TODO> status message: file is null / doesn't exist
                    this.statusState = 5;
                    this.updateStatusMessage();

                    return;
                }

            FileInputStream fileInputStream = new FileInputStream(file);
            PreparedStatement preparedStatement = Queries.createInsertFileStatement(activeConnection);
                if (preparedStatement == null) {
                    // <TODO> status message: failed to create SQL statement
                    this.statusState = 5;
                    this.updateStatusMessage();

                    return;
                }

    //        INSERT INTO "files" ("fileName", "fileType", "fileSize", "tags", "content") VALUES (?, ?, ?, ?, ?);
            String fileName = this.textField_fileName.getText();
            String fileType = this.textField_fileType.getText();
            long fileSize = this.fileMetadata.fileSize;
            String tags = this.textArea_tags.getText();
            byte[] content = fileInputStream.readAllBytes();
                fileInputStream.close();
                System.out.println("FileInputStream " + fileInputStream + " @" + file.getAbsolutePath() + " read: " + content.length + " Bytes");

            boolean statusPreparation = Queries.prepareInsertFileStatement(preparedStatement, fileName, fileType, fileSize, tags, content);
                if (statusPreparation) {
                    long generatedKey = Queries.evaluateInsertFileStatement(preparedStatement);
                        if (generatedKey == -1) {
                            // <TODO> status message: failed to insert, probably invalid object
                            this.statusState = 5;
                            this.updateStatusMessage();

                            return;
                        }

                    // <TODO> present the key, persist it in the view's state (*)
                    System.out.println("Key: " + generatedKey);

                    // <TODO> if insert is successful (it might not be, due to invalid input), switch to Update query
                    this.button_insertOrUpdate.setOnAction(this::handleUpdateInDatabase);

                    // <TODO> (*) replace this; use a state-based update for language-dependent content for components that have state-dependent content (*)
                    Locale locale = Locale.getDefault();
                    ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);
                        this.button_insertOrUpdate.setText(bundle.getString("button_insertOrUpdate_update"));

                    this.statusState = 4;
                    this.updateStatusMessage();

                    // <TODO> for now, just remove the button
                    this.button_insertOrUpdate.setVisible(false);
                    this.button_insertOrUpdate.setManaged(false);
                }
        }
        catch (NullPointerException exception) {
            System.out.println("{FileMetadataView.handleInsertInDatabase()} [NullPointerException] (ComboBox.getSelectionModel().getSelectedItem()) - " + exception.getMessage() + "\n" + exception.getCause());
            this.statusState = 5;
        }
        catch (FileNotFoundException exception) {
            System.out.println("{FilesystemMetadataView.handleInsertInDatabase()} [FileNotFoundException] (new FileInputStream()) - " + exception.getMessage() + "\n" + exception.getCause());
            // <TODO> status message
            this.statusState = 5;
        }
        catch (IOException exception) {
            System.out.println("{FilesystemMetadataView.handleInsertInDatabase()} [IOException] (FileInputStream.readAllBytes()) - " + exception.getMessage() + "\n" + exception.getCause());
            // <TODO> status message
            this.statusState = 5;
        }

        this.updateStatusMessage();
    }
    public void handleUpdateInDatabase (ActionEvent event) {
        // <TODO> first, test that the entry is still the same, using the stored key and the content as comparison
        // in case they are not the same, consider the state as being insertion again
        // but if the selection coincides, then it is indeed an update
        // the content cannot be updated, only fileName, fileType, fileSize and tags can be updated

        Connection activeConnection = this.application.mainLayoutController.centerController.addNewFiles.generateFilteredQueueAutonomousView.comboBox_selectAnActiveDatabase.getSelectionModel().getSelectedItem().databaseEntry.connection.getValue();
            if (activeConnection == null) {
                // <TODO> status message
                return;
            }

//        PreparedStatement preparedStatement = Queries.prepareSelectByIdStatement(activeConnection, this.filesystemMetadata.id);

    }
    public void handleShareWith (ActionEvent event) {
        // 0 -> saved successfully, 1 -> failed to save
        // 2 -> successfully visualized file, 3 -> failed to visualize file
        // 4 -> successfully inserted file, 5 -> failed to insert file
        // 6 -> successfully updated file, 7 -> failed to update file
        // 8 -> queued file for sharing, 9 -> failed to queue file for sharing
    }
    public void handleCloseQueuedFile(ActionEvent event) {
        this.application.removeObservableQueuedFile(this.fileMetadata, true);
    }
    public void handleCloseSelectedFile(ActionEvent event) {
        this.application.removeObservableSelectedFile(this.fileMetadata, true);
    }
    public void handleClosePeerFile (ActionEvent event) {

    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        this.label_fileName.setText(bundle.getString("label_fileName"));
        if (this.fileMetadata.fileSize < 1024) {
            this.label_filesSize.setText(String.valueOf(this.fileMetadata.fileSize));
            this.label_sizeMeasurementUnit.setText(" " + bundle.getString("label_sizeMeasurementUnit_B"));
        }
        else if (this.fileMetadata.fileSize < 1024 * 1024) {
            float fileSize = (float)this.fileMetadata.fileSize / 1024.0f;
            String finalFileSize = String.valueOf(fileSize);
            finalFileSize = finalFileSize.substring(0, finalFileSize.indexOf(".") + 3);
            this.label_filesSize.setText(finalFileSize);
            this.label_sizeMeasurementUnit.setText(" " + bundle.getString("label_sizeMeasurementUnit_KiB"));
        }
        else if (this.fileMetadata.fileSize < 1024 * 1024 * 1024) {
            float fileSize = (float)this.fileMetadata.fileSize / (1024.0f * 1024.0f);
            String finalFileSize = String.valueOf(fileSize);
            finalFileSize = finalFileSize.substring(0, finalFileSize.indexOf(".") + 3);
            this.label_filesSize.setText(finalFileSize);
            this.label_sizeMeasurementUnit.setText(" " + bundle.getString("label_sizeMeasurementUnit_MiB"));
        }
        else {
            float fileSize = (float)this.fileMetadata.fileSize / (1024.0f * 1024.0f * 1024.0f);
            String finalFileSize = String.valueOf(fileSize);
            finalFileSize = finalFileSize.substring(0, finalFileSize.indexOf(".") + 3);
            this.label_filesSize.setText(finalFileSize);
            this.label_sizeMeasurementUnit.setText(" " + bundle.getString("label_sizeMeasurementUnit_GiB"));
        }
        this.label_filePathName.setText(bundle.getString("label_filePathName"));
        this.label_databaseId.setText(bundle.getString("label_databaseId"));
        this.label_tags.setText(bundle.getString("label_tags"));
        this.button_visualizeWith.setText(bundle.getString("button_visualizeWith"));

        // metadataOrigin:
            // -2 -> invalid [no source no content]
            // -1 -> [initial state]
            // 0 -> QueuedFile [filesystem]
            // 1 -> SelectedFile [database]
            // 2 -> ? [peer]
            // 3 -> ? [unknown source, but content is set]
        if (this.metadataOrigin == 0 || this.metadataOrigin == 2 || this.metadataOrigin == 3) {
            this.button_insertOrUpdate.setText(bundle.getString("button_insertOrUpdate_insert"));
        }
        else if (this.metadataOrigin == 1) {
            this.button_insertOrUpdate.setText(bundle.getString("button_insertOrUpdate_update"));
        }

        this.button_shareWith.setText(bundle.getString("button_shareWith"));

        this.updateStatusMessage();

        if (this.metadataOrigin == 0) {
            try {
                this.textField_filePathName.setText(this.fileMetadata.file.getCanonicalPath());
            } catch (IOException exception) {
                System.out.println("{FilesystemMetadataView.initialize()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            }
        }
        else if (this.metadataOrigin == 1) {
            this.textField_filePathName.setText(bundle.getString("textField_filePathName_fromDatabase"));
        }

        synchronizeDatabaseId();
    }
    public void updateStatusMessage() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        // statusState:
        // -1 -> initial
        // 0 -> saved successfully, 1 -> failed to save
        // 2 -> successfully visualized file, 3 -> failed to visualize file
        // 4 -> successfully inserted file, 5 -> failed to insert file
        // 6 -> successfully updated file, 7 -> failed to update file
        // 8 -> queued file for sharing, 9 -> failed to queue file for sharing
        if (this.statusState == -1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initialState"));
        }
        else if (this.statusState == 0) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_savedSuccessfully"));
        }
        else if (this.statusState == 1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedToSave"));
        }
        else if (this.statusState == 2) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_visualizedSuccessfully"));
        }
        else if (this.statusState == 3) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedToVisualize"));
        }
        else if (this.statusState == 4) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_insertedSuccessfully"));
        }
        else if (this.statusState == 5) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedToInsert"));
        }
        else if (this.statusState == 6) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_updatedSuccessfully"));
        }
        else if (this.statusState == 7) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedToUpdate"));
        }
        else if (this.statusState == 8) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_successfullyQueuedForSharing"));
        }
        else if (this.statusState == 9) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_failedToQueueForSharing"));
        }
    }
    public void synchronizeFileName () {
        this.fileMetadata.fileName = this.textField_fileName.getText();
    }
    public void synchronizeFileType () {
        this.fileMetadata.fileType = this.textField_fileType.getText();
    }
    public void initialTagsFromLogicToUi () {
        this.textArea_tags.setText(this.fileMetadata.tags);
    }
    public void synchronizeTags () {
        this.fileMetadata.tags = this.textArea_tags.getText();
    }
    public void synchronizeDatabaseId () {
        if (this.fileMetadata.id != -1) {
            this.textField_databaseId.setText(String.valueOf(this.fileMetadata.id));
        }
        else {
            Locale locale = Locale.getDefault();
            ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);
            this.textField_databaseId.setText(bundle.getString("textField_databaseId_notAvailable"));
        }
    }
// ---- [comparison]
    public static boolean equalsByFileMetadata (FileMetadataView left, FileMetadataView right) {
        return FileMetadata.equalsByFileAttributes(left.fileMetadata, right.fileMetadata);
    }
}
