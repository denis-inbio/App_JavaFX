package ro.nq.metaexplorer.AutonomousViews;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.ItemViews.DatabaseActiveView;
import ro.nq.metaexplorer.Utilities.Filesystem;
import ro.nq.metaexplorer.Utilities.Languages;
import ro.nq.metaexplorer.Utilities.Queries;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;

public class GenerateDatabaseQuery implements Initializable {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        GenerateDatabaseQuery.class,
        "GenerateDatabaseQuery",
        "GenerateDatabaseQuery.fxml",
        "GenerateDatabaseQuery.css",
        "ro.nq.metaexplorer.AutonomousViews.GenerateDatabaseQuery"
    );
    public static final ObservableList<String> english_SizeMultiplierUnits = FXCollections.observableArrayList("Bytes", "KiB", "MiB", "GiB");
    public static final ObservableList<String> romanian_SizeMultiplierUnits = FXCollections.observableArrayList("Octeți", "KiO", "MiO", "GiO");
    public static final String englishLanguage = Languages.supportedLanguages[0];
    public static final String romanianLanguage = Languages.supportedLanguages[1];
    public static final String minimizeIconPath = "icons/up_arrow.png";
    public static final String maximizeIconPath = "icons/down_arrow.png";
// ---- [extended logic state]
    // statusMessageState:
        // -1 -> initial, nothing
        // 0 -> no active database selected
int statusMessageState;
// ---- [state, ctor]
Terminal application;
FXMLLoader fxmlLoader;
public Parent parent;
    public GenerateDatabaseQuery(Terminal application) {
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
            System.out.println("{SearchInDatabase} [] (FXMLLoader.load()) - Parent: " + this.parent);
        }
        catch (IOException exception) {
            System.out.println("{SearchInDatabase} [IOException] (FXMLLoader.load()) - " + exception.getMessage() + "\t" + exception.getCause());
        }
    }
// ---- [controllers]
    // none yet
// ---- [FXML model]
public @FXML VBox base;
public @FXML HBox container_header;
@FXML Button button_minimizeOrMaximize;
@FXML VBox minimizable;
@FXML Label label_selectAnActiveDatabase;
@FXML ComboBox<DatabaseActiveView> comboBox_selectAnActiveDatabase;
@FXML Label label_fileName;
@FXML Label label_fileType;
@FXML Label label_fileSize;
@FXML Label label_tags;
@FXML TextField textField_fileName;
@FXML TextField textField_fileType;
@FXML TextField textField_fileSizeMinimum;
@FXML ComboBox<String> comboBox_fileSizeMultiplierUnitMinimum;
@FXML TextField textField_fileSizeMaximum;
@FXML ComboBox<String> comboBox_fileSizeMultiplierUnitMaximum;
@FXML TextField textField_tags;
@FXML RadioButton radioButton_or;
@FXML RadioButton radioButton_and;
@FXML Button button_search;
@FXML Button button_clearFilters;
@FXML Label label_limit;
@FXML TextField textField_limit;
@FXML Label label_statusMessage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert this.base != null;
        assert this.container_header != null;
        assert this.button_minimizeOrMaximize != null;
            Image minimizeImage = Filesystem.loadImageResource(this.application, GenerateDatabaseQuery.minimizeIconPath);
            ImageView minimizeImageView = new ImageView(minimizeImage);
                this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
            this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);
        assert this.minimizable != null;
            this.minimizable.managedProperty().bind(this.minimizable.visibleProperty());
            this.minimizable.setVisible(true);
        assert this.label_selectAnActiveDatabase != null;
        assert this.comboBox_selectAnActiveDatabase != null;
            this.comboBox_selectAnActiveDatabase.setItems(this.application.observableActiveDbViews);
            this.comboBox_selectAnActiveDatabase.setCellFactory(items -> new DatabaseActiveView.DatabaseActiveViewComboCell(this.application));
            this.comboBox_selectAnActiveDatabase.setButtonCell(new ListCell<DatabaseActiveView>() {
                @Override
                protected void updateItem(DatabaseActiveView item, boolean btl){
                    super.updateItem(item, btl);

                    if (item != null) {
                        String customConnectionName = item.databaseEntry.customName.getValue();
                        if (customConnectionName.isBlank()) {
                            customConnectionName = item.databaseEntry.file.getValue().getName();
                        }
                        setText(customConnectionName);
                        System.out.println("Selected active database: " + customConnectionName);
                    }
                    else {
                        setText("");
                    }
                }
            });
        assert this.label_fileName != null;
        assert this.label_fileType != null;
        assert this.label_fileSize != null;
        assert this.label_tags != null;
        // <TODO> eventually, I could also have some form of "validation as you type"
        assert this.textField_fileName != null;
        assert this.textField_fileType != null;
        assert this.textField_fileSizeMinimum != null;
        assert this.comboBox_fileSizeMultiplierUnitMinimum != null;
        assert this.textField_fileSizeMaximum != null;
        assert this.comboBox_fileSizeMultiplierUnitMaximum != null;
        assert this.textField_tags != null;
        assert this.radioButton_or != null;
        assert this.radioButton_and != null;
            this.radioButton_and.setSelected(true);
        assert this.button_search != null;
            this.button_search.setOnAction(this::handleSearch);
        assert this.button_clearFilters != null;
            this.button_clearFilters.setOnAction(this::handleClearFilters);
        assert this.label_limit != null;
        assert this.textField_limit != null;
        assert this.label_statusMessage != null;

        this.updateLanguageDependentContent();
            this.addLanguageListener();
    }
// ---- [listeners]
    InvalidationListener languageListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable invalidation) {
            System.out.println("Language invalidation in ::GenerateDatabaseQuery");
            updateLanguageDependentContent();
        }
    };
    public void addLanguageListener () {
        this.application.observableLanguage.addListener(this.languageListener);
    }
    public void removeLanguageListener () {
        this.application.observableLanguage.removeListener(this.languageListener);
    }
// ---- [event handlers]
    public void handleMinimizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(false);

        this.button_minimizeOrMaximize.setOnAction(this::handleMaximizeMinimizable);
            Image maximizeImage = Filesystem.loadImageResource(this.application, GenerateDatabaseQuery.maximizeIconPath);
            ImageView maximizeImageView = new ImageView(maximizeImage);
                this.button_minimizeOrMaximize.setGraphic(maximizeImageView);

        this.application.mainLayoutController.sizeSideEffectsGeneratedDatabaseQueryMinimize();
    }
    public void handleMaximizeMinimizable (ActionEvent event) {
        this.minimizable.setVisible(true);

        Image minimizeImage = Filesystem.loadImageResource(this.application, GenerateDatabaseQuery.minimizeIconPath);
        ImageView minimizeImageView = new ImageView(minimizeImage);
            this.button_minimizeOrMaximize.setGraphic(minimizeImageView);
        this.button_minimizeOrMaximize.setOnAction(this::handleMinimizeMinimizable);

        this.application.mainLayoutController.sizeSideEffectsGeneratedDatabaseQueryMaximize();
    }
    public void handleSearch (ActionEvent event) {
        Connection activeConnection = this.application.mainLayoutController.centerController.queryDatabase.generateDatabaseQueryAutonomousView.comboBox_selectAnActiveDatabase.getSelectionModel().getSelectedItem().databaseEntry.connection.getValue();
            if (activeConnection == null) {
                this.statusMessageState = 0;
                updateLanguageDependentContent();
                return;
            }

        this.statusMessageState = -1;
        updateLanguageDependentContent();

        PreparedStatement preparedStatement = generateAndPrepareStatementFromInput(activeConnection);
            System.out.println("Generated prepared statement: " + preparedStatement + "\n\t| for database connection: " + activeConnection);

        this.application.recomputeObservableSelectedFiles(activeConnection, preparedStatement);
    }
    public void handleClearFilters (ActionEvent event) {
        this.textField_fileName.setText("");
        this.textField_fileType.setText("");
        this.textField_fileSizeMinimum.setText("");
        this.comboBox_fileSizeMultiplierUnitMinimum.getSelectionModel().select(0);
        this.textField_fileSizeMaximum.setText("");
        this.comboBox_fileSizeMultiplierUnitMaximum.getSelectionModel().select(0);
        this.textField_tags.setText("");
        this.radioButton_and.setSelected(true);

        this.label_statusMessage.setText("Filters have been reset");
        this.label_statusMessage.setStyle("-fx-text-fill: green;");

    }
// ---- [view methods]
    public void updateLanguageDependentContent() {
        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle(classConstants.bundlePathName, locale);

        if (locale.getLanguage().contains(GenerateFilteredQueue.englishLanguage)) {
            this.comboBox_fileSizeMultiplierUnitMinimum.setItems(english_SizeMultiplierUnits);
            this.comboBox_fileSizeMultiplierUnitMinimum.getSelectionModel().select(0);
            this.comboBox_fileSizeMultiplierUnitMaximum.setItems(english_SizeMultiplierUnits);
            this.comboBox_fileSizeMultiplierUnitMaximum.getSelectionModel().select(0);
        }
        else if (locale.getLanguage().contains(GenerateFilteredQueue.romanianLanguage)) {
            this.comboBox_fileSizeMultiplierUnitMinimum.setItems(romanian_SizeMultiplierUnits);
            this.comboBox_fileSizeMultiplierUnitMinimum.getSelectionModel().select(0);
            this.comboBox_fileSizeMultiplierUnitMaximum.setItems(romanian_SizeMultiplierUnits);
            this.comboBox_fileSizeMultiplierUnitMaximum.getSelectionModel().select(0);
        }

        if (statusMessageState == -1) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_initial"));
            this.label_statusMessage.setStyle("-fx-text-fill: black");
        }
        else if (statusMessageState == 0) {
            this.label_statusMessage.setText(bundle.getString("label_statusMessage_noActiveDatabaseSelected"));
            this.label_statusMessage.setStyle("-fx-text-fill: red");
        }

        this.label_selectAnActiveDatabase.setText(bundle.getString("label_selectAnActiveDatabase"));

        this.label_fileName.setText(bundle.getString("label_fileName"));
        this.label_fileType.setText(bundle.getString("label_fileType"));
        this.label_fileSize.setText(bundle.getString("label_fileSize"));
        this.label_tags.setText(bundle.getString("label_tags"));

        this.radioButton_or.setText(bundle.getString("radioButton_or"));
        this.radioButton_and.setText(bundle.getString("radioButton_and"));

        this.button_search.setText(bundle.getString("button_search"));
        this.label_limit.setText(bundle.getString("label_limit"));
    }
    public PreparedStatement generateAndPrepareStatementFromInput(Connection connection) {
            if (connection == null) {
                return null;
            }

        String fileName = this.textField_fileName.getText();
            boolean enableFileName = !fileName.isBlank();
        String fileType = this.textField_fileType.getText();
            boolean enableFileType = !fileType.isBlank();

        Integer minimum = null;
        Integer sizeMultiplierMinimum = null;
            try {
                minimum = Integer.parseInt(this.textField_fileSizeMinimum.getText());
            }
            catch (NumberFormatException exception) {
                System.out.println("{getPreparedStatementFromInput()} [NumberFormatException] (Integer.parseInt()) - minimum - " + exception.getMessage() + "\n" + exception.getCause());
            }
            try {
                String sizeMultiplierUnitMinimum = this.comboBox_fileSizeMultiplierUnitMinimum.getSelectionModel().getSelectedItem();

                if (sizeMultiplierUnitMinimum.equals(english_SizeMultiplierUnits.get(0)) || sizeMultiplierUnitMinimum.equals(romanian_SizeMultiplierUnits.get(0))) {
                    sizeMultiplierMinimum = 1;
                }
                else if (sizeMultiplierUnitMinimum.equals(english_SizeMultiplierUnits.get(1)) || sizeMultiplierUnitMinimum.equals(romanian_SizeMultiplierUnits.get(1))) {
                    sizeMultiplierMinimum = 1024;
                }
                else if (sizeMultiplierUnitMinimum.equals(english_SizeMultiplierUnits.get(2)) || sizeMultiplierUnitMinimum.equals(romanian_SizeMultiplierUnits.get(2))) {
                    sizeMultiplierMinimum = 1024 * 1024;
                }
                else if (sizeMultiplierUnitMinimum.equals(english_SizeMultiplierUnits.get(3)) || sizeMultiplierUnitMinimum.equals(romanian_SizeMultiplierUnits.get(3))) {
                    sizeMultiplierMinimum = 1024 * 1024 * 1024;
                }

                if (sizeMultiplierMinimum == null) {
                    // <TODO> this shouldn't happen
                    // <TODO> for status messages, use an observable int instead, which maps to strings of states which are also defined in the *.properties
                    this.label_statusMessage.setText("Select a valid size unit from the ComboBox for minimum");
                    this.label_statusMessage.setStyle("-fx-text-fill: orange;");
                    return null;
                }
            }
            catch (NumberFormatException exception) {
                System.out.println("{FilterQueue.getFilters()} [NumberFormatException] (Integer.parseInt()) - size multiplier for minimum - " + exception.getMessage() + "\n" + exception.getCause());
            }
            boolean enableFileSizeMinimum = (minimum != null) && (sizeMultiplierMinimum != null);

        Integer maximum = null;
        Integer sizeMultiplierMaximum = null;
            try {
                maximum = Integer.parseInt(this.textField_fileSizeMaximum.getText());
            }
            catch (NumberFormatException exception) {
                System.out.println("{getPreparedStatementFromInput()} [NumberFormatException] (Integer.parseInt()) - maximum - " + exception.getMessage() + "\n" + exception.getCause());
            }
            try {
                String sizeMultiplierUnitMaximum = this.comboBox_fileSizeMultiplierUnitMaximum.getSelectionModel().getSelectedItem();
                if (sizeMultiplierUnitMaximum.equals(english_SizeMultiplierUnits.get(0)) || sizeMultiplierUnitMaximum.equals(romanian_SizeMultiplierUnits.get(0))) {
                    sizeMultiplierMaximum = 1;
                }
                else if (sizeMultiplierUnitMaximum.equals(english_SizeMultiplierUnits.get(1)) || sizeMultiplierUnitMaximum.equals(romanian_SizeMultiplierUnits.get(1))) {
                    sizeMultiplierMaximum = 1024;
                }
                else if (sizeMultiplierUnitMaximum.equals(english_SizeMultiplierUnits.get(2)) || sizeMultiplierUnitMaximum.equals(romanian_SizeMultiplierUnits.get(2))) {
                    sizeMultiplierMaximum = 1024 * 1024;
                }
                else if (sizeMultiplierUnitMaximum.equals(english_SizeMultiplierUnits.get(3)) || sizeMultiplierUnitMaximum.equals(romanian_SizeMultiplierUnits.get(3))) {
                    sizeMultiplierMaximum = 1024 * 1024 * 1024;
                }

                if (sizeMultiplierMaximum == null) {
                    // <TODO> this shouldn't happen
                    // <TODO> for status messages, use an observable int instead, which maps to strings of states which are also defined in the *.properties
                    this.label_statusMessage.setText("Select a valid size unit from the ComboBox for maximum");
                    this.label_statusMessage.setStyle("-fx-text-fill: orange;");
                    return null;
                }
            }
            catch (NumberFormatException exception) {
                System.out.println("{FilterQueue.getFilters()} [NumberFormatException] (Integer.parseInt()) - size multiplier for maximum - " + exception.getMessage() + "\n" + exception.getCause());
            }
            boolean enableFileSizeMaximum = (maximum != null) && (sizeMultiplierMaximum != null);


        String tags = this.textField_tags.getText();
            String[] splitTags = tags.split(",");
            LinkedList<String> validTags = new LinkedList<>();
                for (int index = 0; index < splitTags.length; index++) {
                    String trimmedTag = splitTags[index].trim();
                        if (trimmedTag.length() > 0) {
                            validTags.add(trimmedTag);
                        }
                }
            int enableTags = validTags.size();

        Integer limit = null;
            try {
                limit = Integer.parseInt(this.textField_limit.getText());
            }
            catch (NumberFormatException exception) {
                limit = 0;
                System.out.println("{getPreparedStatementFromInput()} [NumberFormatException] (Integer.parseInt()) - limit - " + exception.getMessage() + "\n" + exception.getCause());
            }

        String selectQuery = Queries.generateConditionalSelectQuery(enableFileName, enableFileType, enableFileSizeMinimum, enableFileSizeMaximum, enableTags, limit);
            System.out.println("Generated SELECT query: `" + selectQuery + "`");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            int position = 1;

                if (enableFileName) {
                    preparedStatement.setString(position, "%" + fileName + "%");
                    position++;
                }
                if (enableFileType) {
                    preparedStatement.setString(position, "%" + fileType + "%");
                    position++;
                }
                if (enableFileSizeMinimum) {
                    preparedStatement.setInt(position, minimum * sizeMultiplierMinimum);
                    position++;
                }
                if (enableFileSizeMaximum) {
                    preparedStatement.setInt(position, maximum * sizeMultiplierMaximum);
                    position++;
                }
                if (enableTags > 0) {
                    for (int index = 0; index < enableTags; index++) {
                        preparedStatement.setString(position, "%" + validTags.get(index) + "%");
                        position++;
                    }
                }

            this.label_statusMessage.setText("Successfully generated the query");
                this.label_statusMessage.setStyle("-fx-text-fill: green;");

            return preparedStatement;
        }
        catch (SQLException exception) {
            System.out.println("{getPreparedStatementFromInput()} [SQLException] (Connection.prepareStatement()) - " + exception.getMessage() + "\n" + exception.getCause());
            this.label_statusMessage.setText("Failed to create a query");
                this.label_statusMessage.setStyle("-fx-text-fill: red;");

            return null;
        }
    }

}
