package ro.nq.metaexplorer.Applications;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ro.nq.metaexplorer.BorderControllers.*;
import ro.nq.metaexplorer.DataEncapsulations.*;
import ro.nq.metaexplorer.ItemViews.*;
import ro.nq.metaexplorer.Utilities.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Terminal extends Application {
    public Stage window;
    Scene scene;
    public BorderedExplorer mainLayoutController;

    @Override
    public void start(Stage stage) throws IOException {
        Locale.setDefault(Languages.supportedLocales[0]);
        this.observableLanguage.set(Languages.supportedLanguages[0]);

        assert stage != null;
        this.window = stage;

        this.mainLayoutController = new BorderedExplorer(this);
        this.scene = new Scene(this.mainLayoutController.parent);
        this.window.setScene(this.scene);
        this.window.setMaximized(true);
        this.window.show();

        this.mainLayoutController.sizeTopBorderMinimize();
        this.mainLayoutController.sizeLeftBorderMinimize();
        this.mainLayoutController.sizeCenterNormal();
        this.mainLayoutController.sizeRightBorderMinimize();

        this.window.setOnCloseRequest(this::handleOnCloseRequest);
    }

    public static void main(String[] args) {
        Application.launch();
    }

    public static final Integer initialItemsPerPage = 7;
    public static final int threadJoinTimeoutMillis = 1000;
    // [language listener]: string trigger
    public SimpleStringProperty observableLanguage = new SimpleStringProperty(Languages.supportedLanguages[0]);


    // [caching]: Image and ImageView caches
    public Lock cachedImages_lock = new ReentrantLock();
    public Map<String, Image> cachedImages = new HashMap<>();


    // [history db]: historyDbThread (and its state properties), history files (Hf), history db (Hdb), history db views (Hdbv)
    public SimpleObjectProperty<Thread> historyDbThread = new SimpleObjectProperty<>(null);
    public SimpleBooleanProperty userDir_or_userHome = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty historyDbDefaultFileWasLoaded = new SimpleBooleanProperty(false);
    public List<File> foundHistoryFiles = null;
        public Runnable historyDbSearchRunnable = new Runnable() {
            @Override
            public void run() {
                foundHistoryFiles = null;
                initializeHistoryDb();
            }
        };
        public void startHistoryDbThread (boolean userDir_or_userHome) {
            boolean joined = false;
                if (this.historyDbThread.getValue() != null) {
                    try {
                        this.historyDbThread.getValue().join(Terminal.threadJoinTimeoutMillis);
                        joined = true;
                    } catch (InterruptedException exception) {
                        System.out.println("{Terminal.startHistoryDbThread()} [InterruptedException] (Thread.join()) - " + exception.getMessage() + "\n" + exception.getCause());
                    }
                }

            if (joined || this.historyDbThread.getValue() == null) {
                this.userDir_or_userHome.setValue(userDir_or_userHome);
                this.historyDbThread.setValue(new Thread(this.historyDbSearchRunnable));
                    this.historyDbThread.getValue().setDaemon(false);
                    this.historyDbThread.getValue().start();
            }
        }
        public void initializeHistoryDb() {
            String searchSpacePath = Filesystem.getUserSearchSpace(this.userDir_or_userHome.getValue());
            List<File> filteredHistoryFiles = History.searchConfigFiles(searchSpacePath);
                System.out.println("Searching in " + searchSpacePath + " has found " + filteredHistoryFiles);

            this.foundHistoryFiles = filteredHistoryFiles;
        }
    public Lock observableHistoryFiles_lock = new ReentrantLock();
        public ObservableList<File> observableHistoryFiles = FXCollections.observableArrayList();
        public void addObservableHistoryFile (File historyFile, boolean recompute) {
            try {
                observableHistoryFiles_lock.lock();

                this.observableHistoryFiles.add(historyFile);
            }
            finally {
                observableHistoryFiles_lock.unlock();
            }

            if (recompute) {
                this.recomputeObservableHistoryDb();
            }
        }
        public int getIndex_observableHistoryFile (File file) {
            try {
                String fileCanonicalPath = file.getCanonicalPath();
                    for (int index = 0; index < this.observableHistoryFiles.size(); index++) {
                        String historyFileCanonicalPath = this.observableHistoryFiles.get(index).getCanonicalPath();
                            if (fileCanonicalPath.equals(historyFileCanonicalPath)) {
                                return index;
                            }
                    }
                return -1;
            }
            catch (IOException exception) {
                System.out.println("{Terminal.getIndex_observableHistoryFile} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
                return -1;
            }
        }
        public int addObservableHistoryFiles (List<File> historyFiles) {
            int countChanged = 0;
            int addedHistoryDb = 0;

            try {
                observableHistoryFiles_lock.lock();

                File defaultHistoryDbFile = getHistoryDbDefaultFile();

                for (File historyFile : historyFiles) {
                    int indexHistoryFile = getIndex_observableHistoryFile(historyFile);
                        if (indexHistoryFile == -1) {
                            try {
                                String historyFileCanonicalPath = historyFile.getCanonicalPath();
                                String defaultHistoryFileCanonicalPath = defaultHistoryDbFile.getCanonicalPath();
                                    if (historyFileCanonicalPath.equals(defaultHistoryFileCanonicalPath)) {
                                        this.historyDbDefaultFileWasLoaded.setValue(true);
                                        System.out.println("Found default history Db file");
                                    }
                                System.out.println("Testing for default history Db: " + historyFileCanonicalPath + " vs " + defaultHistoryFileCanonicalPath);
                            }
                            catch (IOException exception) {
                                System.out.println("{} [] () - " + exception.getMessage() + "\n" + exception.getCause());
                            }

                            this.observableHistoryFiles.add(historyFile);
                            countChanged++;
                                System.out.println("History files: " + this.observableHistoryFiles);
                        }
                }
            }
            finally {
                observableHistoryFiles_lock.unlock();
            }

            addedHistoryDb += this.recomputeObservableHistoryDb();
            return addedHistoryDb;
        }
        public boolean loadHistoryFile(File loadFile) {
            boolean belongs = false;

            try {
                observableHistoryFiles_lock.lock();

                this.observableHistoryFiles.add(loadFile);
                    belongs = this.observableHistoryFiles.contains(loadFile);
            }
            finally {
                observableHistoryFiles_lock.unlock();
            }

            this.recomputeObservableHistoryDb();
            return belongs;
        }
    public Lock observableHistoryDb_lock = new ReentrantLock();
        public ObservableList<DatabaseEntry> observableHistoryDb = FXCollections.observableArrayList();
        public int getIndex_observableHistoryDb (DatabaseEntry databaseEntry) {
            for (int index = 0; index < this.observableHistoryDb.size(); index++) {
                if (DatabaseEntry.equalsCanonicalPath(this.observableHistoryDb.get(index), databaseEntry)) {
                    return index;
                }
            }
            return -1;
        }
        public boolean addObservableHistoryDb (DatabaseEntry databaseEntry, boolean recompute) {
            boolean added = false;
            int indexHistoryDb = getIndex_observableHistoryDb(databaseEntry);
                if (indexHistoryDb == -1) {
                    try {
                        observableHistoryDb_lock.lock();
                        observableHistoryDb.add(databaseEntry);
                        added = true;
                    }
                    finally {
                        observableHistoryDb_lock.unlock();
                    }
                }

            if (added && recompute) {
                this.recomputeObservableHistoryDbViews();
            }
            return added;
        }
        public void removeObservableHistoryDb (DatabaseEntry databaseEntry, boolean recompute) {
            int indexHistoryDb = getIndex_observableHistoryDb(databaseEntry);
            if (indexHistoryDb != -1) {
                try {
                    observableHistoryDb_lock.lock();
                    observableHistoryDb.get(indexHistoryDb).closeConnection();
                    observableHistoryDb.remove(indexHistoryDb);
                }
                finally {
                    observableHistoryDb_lock.unlock();
                }
            }

            if (recompute) {
                this.recomputeObservableHistoryDbViews();
            }
        }
        public int recomputeObservableHistoryDb () {
            int countInsertedHistoryDb = 0;
            List<DatabaseEntry> parsedDatabaseEntries = History.parseObservableHistoryFiles(this.observableHistoryFiles);

            System.out.println("Hdb U= PHdb [union newly parsed entries]");
            for (DatabaseEntry parsedDatabaseEntry : parsedDatabaseEntries) {
                boolean added = addObservableHistoryDb(parsedDatabaseEntry, false);
                    if (added) {
                        countInsertedHistoryDb++;
                    }
            }

            if (countInsertedHistoryDb != 0) {
                this.recomputeObservableHistoryDbViews();
            }
            return countInsertedHistoryDb;
        }
        public boolean saveHistoryDbToDirectory(File directory, boolean append) {
            boolean success = false;
            File file = null;

            try {
                String directoryCanonicalPath = directory.getCanonicalPath();
                file = Paths.get(directoryCanonicalPath, History.defaultHistoryFileName).toFile();
                    System.out.println("Absolute path file [Paths.get().toFile()]: " + file.getCanonicalPath());
            }
            catch (IOException exception) {
                System.out.println("{Terminal.saveHistoryDbToDirectory()} [IOException] (File.getCanonicalPath()) - directory file - " + exception.getMessage() + "\n" + exception.getCause());
            }

            if (file != null) {
                success = true;
                try {
                    FileWriter fileWriter = new FileWriter(file, append);

                    try {
                        this.observableHistoryDb_lock.lock();

                        for (DatabaseEntry databaseEntry : this.observableHistoryDb) {
                            String customName = databaseEntry.customName.getValue();
                                if (customName == null) {
                                    customName = "";
                                }

                            try {
                                String path = databaseEntry.file.getValue().getCanonicalPath();
                                String entryLine = "\"" + customName + "\" " + path + System.lineSeparator();

                                try {
                                    fileWriter.write(entryLine);

                                } catch (IOException exception) {
                                    System.out.println("{Terminal.saveHistoryDbToDirectory()} [IOException] (FileWriter.write()) - " + exception.getMessage() + "\n" + exception.getCause());
                                    success = false;
                                }
                            } catch (IOException exception) {
                                System.out.println("{Terminal.saveHistoryDbToDirectory()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
                                success = false;
                            }
                        }
                    } finally {
                        this.observableHistoryDb_lock.unlock();
                    }

                    fileWriter.close();
                } catch (IOException exception) {
                    System.out.println("{Terminal.saveHistoryDbToDirectory()} [IOException] (new FileWriter()) - " + exception.getMessage() + "\n" + exception.getCause());
                    success = false;
                }
            }

            return success;
        }
    public Lock observableHistoryDbViews_lock = new ReentrantLock();
        public ObservableList<DatabaseHistoryView> observableHistoryDbViews = FXCollections.observableArrayList();
        public int getIndex_observableHistoryDbView(DatabaseHistoryView databaseHistoryView) {
            for (int index = 0; index < this.observableHistoryDbViews.size(); index++) {
                if (this.observableHistoryDbViews.get(index) == databaseHistoryView) {
                    return index;
                }
            }
            return -1;
        }
        public int getIndex_observableHistoryDbView(DatabaseEntry databaseEntry) {
            for (int index = 0; index < this.observableHistoryDbViews.size(); index++) {
                if (DatabaseEntry.equalsCanonicalPath(this.observableHistoryDbViews.get(index).databaseEntry, databaseEntry)) {
                    return index;
                }
            }
            return -1;
        }
        public void addObservableHistoryDbView (DatabaseHistoryView databaseHistoryView) {
            int indexHistoryDbView = getIndex_observableHistoryDbView(databaseHistoryView);
            if (indexHistoryDbView == -1) {
                try {
                    observableHistoryDbViews_lock.lock();
                    observableHistoryDbViews.add(databaseHistoryView);
                }
                finally {
                    observableHistoryDbViews_lock.unlock();
                }
            }
        }
        public void removeObservableHistoryDbView (DatabaseHistoryView databaseHistoryView) {
                if (databaseHistoryView == null) {
                    return;
                }

            int indexHistoryDbView = getIndex_observableHistoryDbView(databaseHistoryView);
                if (indexHistoryDbView != -1) {
                    try {
                        observableHistoryDbViews_lock.lock();
                            databaseHistoryView.removeAllListeners();
                            databaseHistoryView.closeConnection();
                            observableHistoryDbViews.remove(indexHistoryDbView);
                    }
                    finally {
                        observableHistoryDbViews_lock.unlock();
                    }
                }
        }
        public void recomputeObservableHistoryDbViews() {
            System.out.println("Hdbv \\= (Hdbv \\ Hdb) [reduce to a subset]");
            for (int index = 0; index < this.observableHistoryDbViews.size(); index++) {
                int indexHistoryDb = getIndex_observableHistoryDb(this.observableHistoryDbViews.get(index).databaseEntry);
                    if (indexHistoryDb == -1) {
                        removeObservableHistoryDbView(this.observableHistoryDbViews.get(index));
                        index--;
                    }
            }

            System.out.println("Hdbv U= (Hdb \\ Hdbv) [union the remainder]");
            for (int index = 0; index < this.observableHistoryDb.size(); index++) {
                int indexHistoryDbView = getIndex_observableHistoryDbView(this.observableHistoryDb.get(index));
                    if (indexHistoryDbView == -1) {
                        addObservableHistoryDbView(new DatabaseHistoryView(this, this.observableHistoryDb.get(index)));
                    }
            }
        }

    // [active db]
    public Lock observableActiveDb_lock = new ReentrantLock();
        public ObservableList<DatabaseEntry> observableActiveDb = FXCollections.observableArrayList();
        public int getIndex_observableActiveDb (DatabaseEntry databaseEntry) {
            for (int index = 0; index < this.observableActiveDb.size(); index++) {
                if (DatabaseEntry.equalsCanonicalPath(this.observableActiveDb.get(index), databaseEntry)) {
                    return index;
                }
            }
            return -1;
        }
        public void addObservableActiveDb(DatabaseEntry databaseEntry, boolean recompute) {
                if (databaseEntry == null) {
                    return;
                }

            int indexActiveDb = getIndex_observableActiveDb(databaseEntry);
            if (indexActiveDb == -1) {
                try {
                    observableActiveDb_lock.lock();
                    observableActiveDb.add(databaseEntry);
                }
                finally {
                    observableActiveDb_lock.unlock();
                }
            }

            if (recompute) {
                this.recomputeObservableActiveDbViews();
            }
        }
        public void removeObservableActiveDb (DatabaseEntry databaseEntry, boolean recompute) {
                if (databaseEntry == null) {
                    return;
                }

            int indexActiveDb = getIndex_observableActiveDb(databaseEntry);
            if (indexActiveDb != -1) {
                try {
                    observableActiveDb_lock.lock();
                    databaseEntry.closeConnection();
                    observableActiveDb.remove(indexActiveDb);
                }
                finally {
                    observableActiveDb_lock.unlock();
                }
            }

            if (recompute) {
                this.recomputeObservableActiveDbViews();
            }
        }
    public Lock observableActiveDbViews_lock = new ReentrantLock();
        public ObservableList<DatabaseActiveView> observableActiveDbViews = FXCollections.observableArrayList();
        public int getIndex_observableActiveDbView (DatabaseEntry databaseEntry) {
            for (int index = 0; index < this.observableActiveDbViews.size(); index++) {
                if (DatabaseEntry.equalsCanonicalPath(this.observableActiveDbViews.get(index).databaseEntry, databaseEntry)) {
                    return index;
                }
            }
            return -1;
        }
        public int getIndex_observableActiveDbView (DatabaseActiveView databaseActiveView) {
            for (int index = 0; index < this.observableActiveDbViews.size(); index++) {
                if (this.observableActiveDbViews.get(index) == databaseActiveView) {
                    return index;
                }
            }
            return -1;
        }
        public void addObservableActiveDbView (DatabaseActiveView databaseActiveView) {
                if (databaseActiveView == null) {
                    return;
                }

            int indexActiveDbView = getIndex_observableActiveDbView(databaseActiveView);
            if (indexActiveDbView == -1) {
                try {
                    observableActiveDbViews_lock.lock();
                    observableActiveDbViews.add(databaseActiveView);
                }
                finally {
                    observableActiveDbViews_lock.unlock();
                }
            }
        }
        public void removeObservableActiveDbView (DatabaseActiveView databaseActiveView) {
                if (databaseActiveView == null) {
                    return;
                }

            int indexActiveDbView = getIndex_observableActiveDbView(databaseActiveView);
            if (indexActiveDbView != -1) {
                try {
                    observableActiveDbViews_lock.lock();
                    databaseActiveView.removeAllListeners();
                    databaseActiveView.closeConnection();
                    observableActiveDbViews.remove(indexActiveDbView);
                }
                finally {
                    observableActiveDbViews_lock.unlock();
                }
            }
        }
        public void recomputeObservableActiveDbViews() {
            System.out.println("Adbv \\= (Adbv \\ Adb) [make a subset]");
            for (int index = 0; index < this.observableActiveDbViews.size(); index++) {
                int indexActiveDb = getIndex_observableActiveDb(this.observableActiveDbViews.get(index).databaseEntry);
                    if (indexActiveDb == -1) {
                        removeObservableActiveDbView(this.observableActiveDbViews.get(index));
                        index--;
                    }
            }

            System.out.println("Adbv U= (Adb \\ Adbv) [union remainder]");
            for (int index = 0; index < this.observableActiveDb.size(); index++) {
                int indexActiveDbView = getIndex_observableActiveDbView(this.observableActiveDb.get(index));
                    if (indexActiveDbView == -1) {
                        addObservableActiveDbView(new DatabaseActiveView(this, this.observableActiveDb.get(index)));
                    }
            }
        }


    // [files pipeline]: queued files (Qf), filtered queued files (FQf), filtered queued file views (FQfv),
    // items per page (ipp), pagination<ipp> of FQfv (p_ipp FQfv), paginated<ipp, pageIndex> FQfv (PFQfv<ipp, pageIndex>)
    public Lock observableQueuedFiles_lock = new ReentrantLock();
        public ObservableList<FileMetadata> observableQueuedFiles = FXCollections.observableArrayList();
        public int getIndex_observableQueuedFiles (FileMetadata fileMetadata) {
            for (int index = 0; index < this.observableQueuedFiles.size(); index++) {
                if (this.observableQueuedFiles.get(index) == fileMetadata) {
                    return index;
                }
            }

            return -1;
        }
        public void addObservableQueuedFile (File file, boolean recompute) {
            try {
                observableQueuedFiles_lock.lock();
                FileMetadata fileMetadata = new FileMetadata(file, "");

                boolean contained = false;
                for (int index = 0; index < this.observableQueuedFiles.size(); index++) {
                    if (FileMetadata.equalsByCanonicalPath(this.observableQueuedFiles.get(index), fileMetadata)) {
                        System.out.println("File " + fileMetadata.file.getAbsolutePath() + " is already contained");
                        contained = true;
                        break;
                    }
                    if (FileMetadata.equalsByContent(this.observableQueuedFiles.get(index), fileMetadata)) {
                        System.out.println("File " + fileMetadata.file.getAbsolutePath() + " is already contained");
                        contained = true;
                        break;
                    }
                }

                if (!contained) {
                    System.out.println("File " + fileMetadata.file.getAbsolutePath() + " is not yet contained");
                    this.observableQueuedFiles.add(fileMetadata);
                }
            }
            catch (IOException exception) {
                System.out.println("{Terminal.addObservableQueuedFiles()} [IOException] () - " + exception.getMessage() + "\n" + exception.getCause());
            }
            finally {
                observableQueuedFiles_lock.unlock();
            }

            if (recompute) {
                this.recomputeFilteredQueuedFiles();
            }
        }
        public void addObservableQueuedFiles (List<File> files) {
                if (files == null) {
                    return;
                }

            for (File file : files) {
                if (file.isFile()) {
                    System.out.println("\tIs file: " + file);
                    this.addObservableQueuedFile(file, false);
                }
                else if (file.isDirectory()) {
                    System.out.println("\tIs directory: " + file);
                    LinkedList<File> recursiveFiles = Filesystem.recursivelyEnumerateFiles(file,
                            false, null,
                            false, null);
                    for (File recursiveFile : recursiveFiles) {
                        this.addObservableQueuedFile(recursiveFile, false);
                    }
                }
            }

            this.recomputeFilteredQueuedFiles();
        }
        public void removeObservableQueuedFile (FileMetadata fileMetadata, boolean recompute) {
            int indexQueuedFile = getIndex_observableQueuedFiles(fileMetadata);
                if (indexQueuedFile != -1) {
                    try {
                        this.observableQueuedFiles_lock.lock();
                        this.observableQueuedFiles.remove(indexQueuedFile);
                    }
                    finally {
                        this.observableQueuedFiles_lock.unlock();
                    }

                    if (recompute) {
                        this.recomputeFilteredQueuedFiles();
                    }
                }
        }
        public void clearObservableQueuedFiles () {
            try {
                this.observableQueuedFiles_lock.lock();
                this.observableQueuedFiles.clear();
            } finally {
                this.observableQueuedFiles_lock.unlock();
            }

            this.recomputeFilteredQueuedFiles();
        }
    public Lock observableFilteredQueuedFiles_lock = new ReentrantLock();
        public ObservableList<FileMetadata> observableFilteredQueuedFiles = FXCollections.observableArrayList();
        public int getIndex_observableFilteredQueuedFiles (FileMetadata fileMetadata) {
            for (int index = 0; index < this.observableFilteredQueuedFiles.size(); index++) {
                if (this.observableFilteredQueuedFiles.get(index) == fileMetadata) {
                    return index;
                }
            }

            return -1;
        }
        public void recomputeFilteredQueuedFiles () {
            try {
                this.observableFilteredQueuedFiles_lock.lock();
                QueuedFilesFilters filters = this.mainLayoutController.centerController.addNewFiles.generateFilteredQueueAutonomousView.getFilters();

                this.observableFilteredQueuedFiles.clear();

                for (FileMetadata observableQueuedFile : this.observableQueuedFiles) {
                    if (filters.positiveFilterDecision(observableQueuedFile)) {
                        this.observableFilteredQueuedFiles.add(observableQueuedFile);
                    }
                }

                System.out.println("{Terminal.recomputeFQf()} [] () - |FQf| = " + this.observableFilteredQueuedFiles.size());
            }
            finally {
                this.observableFilteredQueuedFiles_lock.unlock();
            }

            recomputeFilteredQueuedFileViews();
        }
    public Lock observableFilteredQueuedFileViews_lock = new ReentrantLock();
        public ObservableList<FileMetadataView> observableFilteredQueuedFileViews = FXCollections.observableArrayList();
        public int getIndex_observableFilteredQueuedFileViews (FileMetadata fileMetadata) {
            for (int index = 0; index < this.observableFilteredQueuedFileViews.size(); index++) {
                if (this.observableFilteredQueuedFileViews.get(index).fileMetadata == fileMetadata) {
                    return index;
                }
            }

            return -1;
        }
        public int getIndex_observableFilteredQueuedFileViews (FileMetadataView fileMetadataView) {
            for (int index = 0; index < this.observableFilteredQueuedFileViews.size(); index++) {
                if (this.observableFilteredQueuedFileViews.get(index) == fileMetadataView) {
                    return index;
                }
            }

            return -1;
        }
        public void addObservableFilteredQueuedFileView(FileMetadataView fileMetadataView, boolean recompute) {
            int indexFilteredQueuedFileView = getIndex_observableFilteredQueuedFileViews(fileMetadataView);
                if (indexFilteredQueuedFileView == -1) {
                    try {
                        this.observableFilteredQueuedFileViews_lock.lock();
                            this.observableFilteredQueuedFileViews.add(fileMetadataView);
                    }
                    finally {
                        this.observableFilteredQueuedFileViews_lock.unlock();
                    }
                }

            if (recompute) {
                this.recomputePaginationViews();
            }
        }
        public void removeObservableFilteredQueuedFileView(FileMetadataView fileMetadataView, boolean recompute) {
            int indexFilteredQueuedFileView = getIndex_observableFilteredQueuedFileViews(fileMetadataView);
                if (indexFilteredQueuedFileView != -1) {
                    try {
                        this.observableFilteredQueuedFileViews_lock.lock();
                            fileMetadataView.removeAllListeners();
                            this.observableFilteredQueuedFileViews.remove(indexFilteredQueuedFileView);
                    }
                    finally {
                        this.observableFilteredQueuedFileViews_lock.unlock();
                    }
                }

            if (recompute) {
                this.recomputePaginationViews();
            }
        }
        public void recomputeFilteredQueuedFileViews () {
            boolean recompute = false;

            System.out.println("FQfv \\= (FQfv \\ FQf) [make FQfv a subset of FQf]");
            for (int indexFilteredQueuedFileView = 0; indexFilteredQueuedFileView < this.observableFilteredQueuedFileViews.size(); indexFilteredQueuedFileView++) {
                int indexFilteredQueuedFile = this.getIndex_observableFilteredQueuedFiles(this.observableFilteredQueuedFileViews.get(indexFilteredQueuedFileView).fileMetadata);
                    if (indexFilteredQueuedFile == -1) {
                        this.removeObservableFilteredQueuedFileView(this.observableFilteredQueuedFileViews.get(indexFilteredQueuedFileView), false);
                            indexFilteredQueuedFileView--;

                        recompute = true;
                    }
            }

            System.out.println("FQfv U= (Qf \\ FQfv)|filters");
            QueuedFilesFilters filters = this.mainLayoutController.centerController.addNewFiles.generateFilteredQueueAutonomousView.getFilters();
            for (FileMetadata observableFilteredQueuedFile : this.observableFilteredQueuedFiles) {
                int indexCorrespondingFilteredQueuedFileView = this.getIndex_observableFilteredQueuedFileViews(observableFilteredQueuedFile);
                    if (indexCorrespondingFilteredQueuedFileView == -1 && filters.positiveFilterDecision(observableFilteredQueuedFile)) {
                            FileMetadataView fileMetadataView = new FileMetadataView(this, observableFilteredQueuedFile);
                            this.addObservableFilteredQueuedFileView(fileMetadataView, false);

                            recompute = true;
                    }
            }

            if (recompute) {
                this.recomputePaginationViews();
            }
        }
    public Lock observableItemsPerPage_lock = new ReentrantLock();
        public SimpleIntegerProperty observableItemsPerPage = new SimpleIntegerProperty(Terminal.initialItemsPerPage);
        public void setItemsPerPage (int itemsPerPage) {
            if (itemsPerPage < 1) {
                if (this.observableItemsPerPage.getValue() < 1) {
                    itemsPerPage = Terminal.initialItemsPerPage;
                }
                else {
                    return;
                }
            }

            if (itemsPerPage != this.observableItemsPerPage.getValue()) {
                try {
                    this.observableItemsPerPage_lock.lock();
                        this.observableItemsPerPage.setValue(itemsPerPage);
                }
                finally {
                    this.observableItemsPerPage_lock.unlock();
                }

                this.recomputePaginationViews();
            }
        }
    public Lock observablePageIndex_lock = new ReentrantLock();
        public SimpleIntegerProperty observablePageIndex = new SimpleIntegerProperty(Terminal.initialItemsPerPage);
        public void setPageIndex (int pageIndex) {
            if (pageIndex < 0) {
                pageIndex = 0;
            }

            try {
                this.observablePageIndex_lock.lock();

                    if (this.observablePageIndex.getValue() < 0) {
                        this.observablePageIndex.setValue(0);
                    }
                    if (this.observablePageViews.size() < this.observablePageIndex.getValue()) {
                        this.observablePageIndex.setValue(this.observablePageViews.size() - 1);
                    }

                    this.observablePageViews.get(this.observablePageIndex.getValue()).isSelected.setValue(false);

                this.observablePageIndex.setValue(pageIndex);
                this.observablePageViews.get(pageIndex).isSelected.setValue(true);
            }
            finally {
                this.observablePageIndex_lock.unlock();
            }

            this.recomputePaginatedFilteredQueuedFileViews();
        }
    public Lock observablePageViews_lock = new ReentrantLock();
        public ObservableList<PageView> observablePageViews = FXCollections.observableArrayList();
        public int computeMaximumPageIndex () {
            int totalItems = this.observableFilteredQueuedFileViews.size();
                int necessarySufficientPages = (totalItems + (this.observableItemsPerPage.getValue() - 1)) / this.observableItemsPerPage.getValue();
            return necessarySufficientPages - 1;
        }
        public void addObservablePageView (PageView pageView, boolean recompute) {
            try {
                this.observablePageViews_lock.lock();
                    pageView.addAllListeners();
                    this.observablePageViews.add(pageView);
            }
            finally {
                this.observablePageViews_lock.unlock();
            }

            if (recompute) {
                int pageIndex = this.observablePageIndex.getValue();
                int maximumPageIndex = this.computeMaximumPageIndex();
                    if (maximumPageIndex < pageIndex) {
                        pageIndex = maximumPageIndex;
                    }

                this.setPageIndex(pageIndex);
            }
        }
        public void removeObservablePageView (PageView pageView, boolean recompute) {
            try {
                this.observablePageViews_lock.lock();
                    pageView.removeAllListeners();
                    this.observablePageViews.remove(pageView);
            }
            finally {
                this.observablePageViews_lock.unlock();
            }

            if (recompute) {
                int pageIndex = this.observablePageIndex.getValue();
                int maximumPageIndex = this.computeMaximumPageIndex();
                    if (maximumPageIndex < pageIndex) {
                        pageIndex = maximumPageIndex;
                    }

                this.setPageIndex(pageIndex);
            }
        }
        public void clearObservablePageViews(boolean recompute) {
            try {
                this.observablePageViews_lock.lock();

                for (PageView pageView : this.observablePageViews) {
                    pageView.removeAllListeners();
                        this.observablePageViews.remove(pageView);
                }
            }
            finally {
                this.observablePageViews_lock.unlock();
            }

            if (recompute) {
                this.setPageIndex(0);
                    this.recomputePaginatedFilteredQueuedFileViews();
            }
        }
        public void recomputePaginationViews() {

            int maximumPageIndexExclusive = this.computeMaximumPageIndex() + 1;
                while(maximumPageIndexExclusive < this.observablePageViews.size()) {
                    this.removeObservablePageView(this.observablePageViews.get(this.observablePageViews.size() - 1), false);
                }

            for (int index = this.observablePageViews.size(); index < maximumPageIndexExclusive; index++) {
                this.addObservablePageView(new PageView(this, index), false);
            }

            int pageIndex = this.observablePageIndex.getValue();
                if (pageIndex > (maximumPageIndexExclusive - 1)) {
                    pageIndex = maximumPageIndexExclusive - 1;
                }
            setPageIndex(pageIndex);
        }
    public Lock observablePaginatedFilteredQueuedFileViews_lock = new ReentrantLock();
        public ObservableList<FileMetadataView> observablePaginatedFilteredQueuedFileViews = FXCollections.observableArrayList();
        public void addObservablePaginatedFilteredQueuedFileViews (FileMetadataView fileMetadataView) {
            try {
                this.observablePaginatedFilteredQueuedFileViews_lock.lock();
                    fileMetadataView.addAllListeners();
                    this.observablePaginatedFilteredQueuedFileViews.add(fileMetadataView);
            }
            finally {
                this.observablePaginatedFilteredQueuedFileViews_lock.unlock();
            }
        }
        public void removeObservablePaginatedFilteredQueuedFileViews (FileMetadataView fileMetadataView) {
            try {
                this.observablePaginatedFilteredQueuedFileViews_lock.lock();

                fileMetadataView.removeAllListeners();
                    this.observablePaginatedFilteredQueuedFileViews.remove(fileMetadataView);
            }
            finally {
                this.observablePaginatedFilteredQueuedFileViews_lock.unlock();
            }
        }
        public void clearObservablePaginatedFilteredQueuedFileViews () {
            try {
                this.observablePaginatedFilteredQueuedFileViews_lock.lock();

                for (int index = 0; index < this.observablePaginatedFilteredQueuedFileViews.size(); index++) {
                    this.observablePaginatedFilteredQueuedFileViews.get(index).removeAllListeners();
                }
                this.observablePaginatedFilteredQueuedFileViews.clear();
            }
            finally {
                this.observablePaginatedFilteredQueuedFileViews_lock.unlock();
            }
        }
        public void recomputePaginatedFilteredQueuedFileViews() {
            this.clearObservablePaginatedFilteredQueuedFileViews();

            int pageIndex = this.observablePageIndex.getValue();
            int itemsPerPage = this.observableItemsPerPage.getValue();
                for (int offset = 0; offset < this.observableItemsPerPage.get(); offset++) {
                    int index = (itemsPerPage * pageIndex + offset);
                    System.out.println("Getting item at index: " + index + " out of " + this.observableFilteredQueuedFileViews.size());
                    if (index < this.observableFilteredQueuedFiles.size()) {
                        this.addObservablePaginatedFilteredQueuedFileViews(this.observableFilteredQueuedFileViews.get(index));
                    }
                }
        }

    // [select files]
    public Lock observableSelectedFiles_lock = new ReentrantLock();
        public ObservableList<FileMetadata> observableSelectedFiles = FXCollections.observableArrayList();
        public int getIndex_observableSelectedFile (FileMetadata fileMetadata) {
            for (int index = 0; index < observableSelectedFiles.size(); index++) {
                if (observableSelectedFiles.get(index) == fileMetadata) {
                    return index;
                }
            }

            return -1;
        }
        public void addObservableSelectedFile (FileMetadata fileMetadata, boolean recompute) {
            int indexSelectedFile = getIndex_observableSelectedFile(fileMetadata);
                if (indexSelectedFile == -1) {
                    try {
                        observableSelectedFiles_lock.lock();
                        observableSelectedFiles.add(fileMetadata);
                    } finally {
                        observableSelectedFiles_lock.unlock();
                    }
                }

            if (recompute) {
                this.recomputeObservableSelectedFileViews();
            }
        }
        public void removeObservableSelectedFile (FileMetadata fileMetadata, boolean recompute) {
            int indexSelectedFile = getIndex_observableSelectedFile(fileMetadata);
                if (indexSelectedFile != -1) {
                    try {
                        observableSelectedFiles_lock.lock();
                        observableSelectedFiles.remove(indexSelectedFile);
                    }
                    finally {
                        observableSelectedFiles_lock.unlock();
                    }
                }

            if (recompute) {
                this.recomputeObservableSelectedFileViews();
            }
        }
        public void clearObservableSelectedFiles (boolean recompute) {
            try {
                this.observableSelectedFiles_lock.lock();
                this.observableSelectedFiles.clear();
            }
            finally {
                this.observableSelectedFiles_lock.unlock();
            }

            if (recompute) {
                recomputeObservableSelectedFileViews();
            }
        }
        public void recomputeObservableSelectedFiles(Connection connection, PreparedStatement preparedStatement) {
            LinkedList<FileMetadata> result = Queries.evaluateConditionalSelectQuery(connection,preparedStatement);
                if (result != null) {
                    this.clearObservableSelectedFiles(false);
                    this.observableSelectedFiles.addAll(result);

                    recomputeObservableSelectedFileViews();
                }
                else {
                    // <TODO> Ui error message (but that's problematic design-wise, since it's "deeply nested", but still doable as-is) ?
                }
        }
    public Lock observableSelectedFileViews_lock = new ReentrantLock();
        public ObservableList<FileMetadataView> observableSelectedFileViews = FXCollections.observableArrayList();
        public int getIndex_observableSelectedFileView (FileMetadata fileMetadata) {
            for (int index = 0; index < observableSelectedFileViews.size(); index++) {
                if (observableSelectedFileViews.get(index).fileMetadata == fileMetadata) {
                    return index;
                }
            }

            return -1;
        }
        public void addObservableSelectedFileView (FileMetadata fileMetadata) {
            int indexSelectedFileView = getIndex_observableSelectedFileView(fileMetadata);
                if (indexSelectedFileView == -1) {
                    try {
                        observableSelectedFileViews_lock.lock();
                        observableSelectedFileViews.add(new FileMetadataView(this, fileMetadata));
                    }
                    finally {
                        observableSelectedFileViews_lock.unlock();
                    }
                }
        }
        public void removeObservableSelectedFileView (FileMetadata fileMetadata) {
            int indexObservableSelectedFileView = getIndex_observableSelectedFileView(fileMetadata);
                if (indexObservableSelectedFileView != -1) {
                    try {
                        observableSelectedFileViews_lock.lock();
                        FileMetadataView fileMetadataView = observableSelectedFileViews.get(indexObservableSelectedFileView);
                            fileMetadataView.removeAllListeners();
                        observableSelectedFileViews.remove(indexObservableSelectedFileView);
                    }
                    finally {
                        observableSelectedFileViews_lock.unlock();
                    }
                }
        }
        public void recomputeObservableSelectedFileViews() {
            System.out.println("Sfv \\= (Sfv \\ Sf) [make Sfv a subset of Sf]");
            for (int indexSelectedFileView = 0; indexSelectedFileView < this.observableSelectedFileViews.size(); indexSelectedFileView++) {
                int indexSelectedFile = this.getIndex_observableSelectedFile(this.observableSelectedFileViews.get(indexSelectedFileView).fileMetadata);
                    if (indexSelectedFile == -1) {
                        this.removeObservableSelectedFileView(this.observableSelectedFileViews.get(indexSelectedFileView).fileMetadata);
                        indexSelectedFileView--;
                    }
            }

            System.out.println("Sfv U= Sf \\ Sfv)");
            for (int indexSelectedFile = 0; indexSelectedFile < this.observableSelectedFiles.size(); indexSelectedFile++) {
                int indexSelectedFileView = getIndex_observableSelectedFileView(this.observableSelectedFiles.get(indexSelectedFile));
                    if (indexSelectedFileView == -1) {
                        this.addObservableSelectedFileView(this.observableSelectedFiles.get(indexSelectedFile));
                    }
            }
        }


    // [active peer]
    public Lock observableActivePeers_lock = new ReentrantLock();
        public ObservableList<PeerSocket> observableActivePeers = FXCollections.observableArrayList();
        public int getIndex_observableActivePeer (PeerSocket peerSocket) {
            for (int index = 0; index < this.observableActivePeers.size(); index++) {
                if (PeerSocket.equalsByEffectiveSocketAddress(this.observableActivePeers.get(index), peerSocket)) {
                    return index;
                }
            }

            return -1;
        }
        public void addObservableActivePeer (PeerSocket peerSocket, boolean recompute) {
            System.out.println("Before lock");
            try {
                observableActivePeers_lock.lock();

                int indexActivePeer = getIndex_observableActivePeer(peerSocket);
                    if (indexActivePeer == -1) {
                        observableActivePeers.add(peerSocket);
                    }
            }
            finally {
                observableActivePeers_lock.unlock();
            }

            if (recompute) {
                this.recomputeObservableActivePeerViews();
            }
        }
        public void removeObservableActivePeer (PeerSocket peerSocket, boolean recompute) {
            try {
                observableActivePeers_lock.lock();

                int indexActivePeer = getIndex_observableActivePeer(peerSocket);
                    if (indexActivePeer != -1) {
                            Networking.disconnectFromPeer(peerSocket);
                            observableActivePeers.remove(indexActivePeer);
                        }
            }
            finally {
                observableActivePeers_lock.unlock();
            }
            if (recompute) {
                this.recomputeObservableActivePeerViews();
            }
        }
        public void clearObservableActivePeers (boolean recompute) {
            try {
                observableActivePeers_lock.lock();

                for (PeerSocket observableActivePeer : observableActivePeers) {
                    Networking.disconnectFromPeer(observableActivePeer);
                }
                observableActivePeers.clear();
            }
            finally {
                observableActivePeers_lock.unlock();
            }

            if (recompute) {
                this.recomputeObservableActivePeerViews();
            }
        }
    public Lock observableActivePeerViews_lock = new ReentrantLock();
        public ObservableList<PeerSocketView> observableActivePeerViews = FXCollections.observableArrayList();
        public int getIndex_observableActivePeerView (PeerSocket peerSocket) {
            for (int index = 0; index < this.observableActivePeerViews.size(); index++) {
                if (PeerSocket.equalsBySocketReference(this.observableActivePeerViews.get(index).peerSocket, peerSocket)) {
                    return index;
                }
            }

            return -1;
        }
        public int getIndex_observableActivePeerView (PeerSocketView peerSocketView) {
            for (int index = 0; index < this.observableActivePeerViews.size(); index++) {
                if (PeerSocket.equalsBySocketReference(this.observableActivePeerViews.get(index).peerSocket, peerSocketView.peerSocket)) {
                    return index;
                }
            }

            return -1;
        }
        public void addObservableActivePeerView (PeerSocketView peerSocketView) {
            int indexActivePeerView = getIndex_observableActivePeerView(peerSocketView);
                if (indexActivePeerView == -1) {
                    try {
                        observableActivePeers_lock.lock();
                        observableActivePeerViews.add(peerSocketView);
                    }
                    finally {
                        observableActivePeers_lock.unlock();
                    }
                }
        }
        public void removeObservableActivePeerView (PeerSocketView peerSocketView) {
            int indexActivePeerView = getIndex_observableActivePeerView(peerSocketView);
                if (indexActivePeerView != -1) {
                    peerSocketView.removeAllListeners();
                    this.observableActivePeerViews.remove(indexActivePeerView);
                }
        }
        public void clearObservableActivePeerViews () {
            for (int index = 0; index < this.observableActivePeerViews.size(); index++) {
                this.observableActivePeerViews.get(index).removeAllListeners();
            }

            this.clearObservableActivePeers(true);
        }
        public void recomputeObservableActivePeerViews() {
            System.out.println("Apv \\= (Apv \\ Ap) [make Apv a subset of Ap]");
            for (int indexActivePeerView = 0; indexActivePeerView < this.observableActivePeerViews.size(); indexActivePeerView++) {
                int indexActivePeer = getIndex_observableActivePeer(this.observableActivePeerViews.get(indexActivePeerView).peerSocket);
                    if (indexActivePeer == -1) {
                        removeObservableActivePeerView(this.observableActivePeerViews.get(indexActivePeerView));
                        indexActivePeerView--;
                    }
            }
            System.out.println("Apv U= (Ap \\ Apv) [union remainder of Ap into Apv]");
            for (int indexActivePeer = 0; indexActivePeer < this.observableActivePeers.size(); indexActivePeer++) {
                int indexActivePeerView = getIndex_observableActivePeerView(this.observableActivePeers.get(indexActivePeer));
                    if (indexActivePeerView == -1) {
                        addObservableActivePeerView(new PeerSocketView(this, this.observableActivePeers.get(indexActivePeer)));
                    }
            }
        }

    // [server socket]
    public SimpleBooleanProperty serverRunning = new SimpleBooleanProperty(false);
    public SimpleObjectProperty<ServerSocket> serverSocket = new SimpleObjectProperty<>(null);
    public SimpleObjectProperty<Thread> serverSocketThread = new SimpleObjectProperty<>(null);
    public Runnable serverSocketRunnable = new Runnable() {
        @Override
        public void run() {
            while (serverRunning.getValue()) {
                try {
                    Socket clientSocket = serverSocket.getValue().accept();
                        System.out.println("Accepted client: " + clientSocket + " @" + clientSocket.getInetAddress());

                    PeerSocket peerSocket = new PeerSocket(clientSocket);
                        addObservableActivePeer(peerSocket, true);
                }
                catch (IOException exception) {
                    System.out.println("{ServerSocket Runnable} [IOException] (ServerSocket.accept())" + exception.getMessage() + "\n\t" + exception.getCause());
                }
            }
        }
    };

    // [queued incoming files]
    public Lock observableQueuedIncomingFiles_lock = new ReentrantLock();
        public ObservableList<QueuedIncomingFile> observableQueuedIncomingFiles = FXCollections.observableArrayList();
        public int getIndex_observableQueuedIncomingFile (QueuedIncomingFile queuedIncomingFile) {
            for (int index = 0; index < this.observableQueuedIncomingFiles.size(); index++) {
                if (QueuedIncomingFile.equalsByFileMetadataPacketAndSocket(this.observableQueuedIncomingFiles.get(index), queuedIncomingFile)) {
                    return index;
                }
            }
            return -1;
        }
        public int getIndex_observableQueuedIncomingFile (FileMetadataView fileMetadataView) {
            for (int index = 0; index < this.observableQueuedIncomingFiles.size(); index++) {
                if (QueuedIncomingFile.equalsByFileMetadata(this.observableQueuedIncomingFiles.get(index).fileMetadata, fileMetadataView.fileMetadata)) {
                    return index;
                }
            }
            return -1;
        }
        public void addObservableQueuedIncomingFile (QueuedIncomingFile queuedIncomingFile, boolean recompute) {
            int indexQueuedIncomingFile = getIndex_observableQueuedIncomingFile(queuedIncomingFile);
                if (indexQueuedIncomingFile == -1) {
                    try {
                        observableQueuedIncomingFiles_lock.lock();
                        observableQueuedIncomingFiles.add(queuedIncomingFile);
                    }
                    finally {
                        observableQueuedIncomingFiles_lock.unlock();
                    }
                }

            if (recompute) {
                this.recomputeObservableQueuedIncomingFileViews();
            }
        }
        public void removeObservableQueuedIncomingFile (QueuedIncomingFile queuedIncomingFile, boolean recompute) {
            int indexQueuedIncomingFile = getIndex_observableQueuedIncomingFile(queuedIncomingFile);
                if (indexQueuedIncomingFile != -1) {
                    try {
                        observableQueuedIncomingFiles_lock.lock();
                        observableQueuedIncomingFiles.remove(indexQueuedIncomingFile);
                    }
                    finally {
                        observableQueuedIncomingFiles_lock.unlock();
                    }
                }

            if (recompute) {
                this.recomputeObservableQueuedIncomingFileViews();
            }
        }
        public void clearObservableQueuedIncomingFiles (QueuedIncomingFile queuedIncomingFile, boolean recompute) {
            int indexQueuedIncomingFile = getIndex_observableQueuedIncomingFile(queuedIncomingFile);
                try {
                    observableQueuedIncomingFiles_lock.lock();
                    observableQueuedIncomingFiles.clear();
                }
                finally {
                    observableQueuedIncomingFiles_lock.unlock();
                }

            if (recompute) {
                this.recomputeObservableQueuedIncomingFileViews();
            }
        }
    public Lock observableQueuedIncomingFileViews_lock = new ReentrantLock();
        public ObservableList<FileMetadataView> observableQueuedIncomingFileViews = FXCollections.observableArrayList();
        public int getIndex_observableQueuedIncomingFileView (FileMetadataView fileMetadataView) {
            for (int index = 0; index < this.observableQueuedIncomingFileViews.size(); index++) {
                if (FileMetadataView.equalsByFileMetadata(this.observableQueuedIncomingFileViews.get(index), fileMetadataView)) {
                    return index;
                }
            }

            return -1;
        }
        public int getIndex_observableQueuedIncomingFileView (QueuedIncomingFile queuedIncomingFile) {
            for (int index = 0; index < this.observableQueuedIncomingFileViews.size(); index++) {
                if (QueuedIncomingFile.equalsByFileMetadata(this.observableQueuedIncomingFileViews.get(index).fileMetadata, queuedIncomingFile.fileMetadata)) {
                    return index;
                }
            }

            return -1;
        }
        public void addObservableQueuedIncomingFileView (FileMetadataView fileMetadataView) {
            int indexQueuedIncomingFileView = getIndex_observableQueuedIncomingFileView(fileMetadataView);
                if (indexQueuedIncomingFileView == -1) {
                    try {
                        observableQueuedIncomingFileViews_lock.lock();
                        observableQueuedIncomingFileViews.add(fileMetadataView);
                    }
                    finally {
                        observableQueuedIncomingFileViews_lock.unlock();
                    }
                }
        }
        public void removeObservableQueuedIncomingFileView (FileMetadataView fileMetadataView) {
            int indexQueuedIncomingFileView = getIndex_observableQueuedIncomingFileView(fileMetadataView);
                if (indexQueuedIncomingFileView != -1) {
                    try {
                        observableQueuedIncomingFileViews_lock.lock();
                        fileMetadataView.removeAllListeners();
                        fileMetadataView.closeSocket();
                        observableQueuedIncomingFileViews.remove(fileMetadataView);
                    }
                    finally {
                        observableQueuedIncomingFileViews_lock.unlock();
                    }
                }
        }
        public void recomputeObservableQueuedIncomingFileViews() {
            System.out.println("QIfv \\= (QIfv \\ QIf) [make QIfv a sub-set of QIf]");
                for (int indexQueuedIncomingFileView = 0; indexQueuedIncomingFileView < this.observableQueuedIncomingFileViews.size(); indexQueuedIncomingFileView++) {
                    int indexQueuedIncomingFile = getIndex_observableQueuedIncomingFile(this.observableQueuedIncomingFileViews.get(indexQueuedIncomingFileView));
                        if (indexQueuedIncomingFile == -1) {
                            this.removeObservableQueuedIncomingFileView(this.observableQueuedIncomingFileViews.get(indexQueuedIncomingFileView));
                            indexQueuedIncomingFileView--;
                        }
                }
            System.out.println("QIfv U= (QIf \\ QIfv) [union the remainder]");
                for (int indexQueuedIncomingFile = 0; indexQueuedIncomingFile < this.observableQueuedIncomingFiles.size(); indexQueuedIncomingFile++) {
                    int indexQueuedIncomingFileView = getIndex_observableQueuedIncomingFileView(this.observableQueuedIncomingFiles.get(indexQueuedIncomingFile));
                        if (indexQueuedIncomingFileView == -1) {
                            this.addObservableQueuedIncomingFileView(new FileMetadataView(this, this.observableQueuedIncomingFiles.get(indexQueuedIncomingFile).fileMetadata));
                        }
                }
        }
// ---- [FXML model]


// ---- [handle events]
    public void handleOnCloseRequest (WindowEvent closeRequest) {
        try {
            if (this.historyDbThread.getValue() != null) {
                System.out.println("Waiting to join history db search thread");
                    this.historyDbThread.getValue().join();
            }
            if (this.serverSocketThread.getValue() != null) {
                Networking.stopServer(this);
            }
        }
        catch (InterruptedException exception) {
            System.out.println("{Terminal.handleOnCloseRequest} [InterruptedException] (Thread.close()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
        System.out.println("Saving history Db");
            boolean saveHistoryDbStatus = saveHistoryDb();
        System.out.println("Closing all Sqlite connections");
            closeAllSqliteConnections();

        System.out.println("Now it's time to close");
    }
// ---- [on close methods]
    public void closeAllSqliteConnections () {
        while(this.observableActiveDb.size() > 0) {
            this.removeObservableActiveDb(this.observableActiveDb.get(0), false);
        }
    }
    public boolean defaultHistoryDbFileAlreadyExists (File directory) {
        try {
            String directoryCanonicalPath = directory.getCanonicalPath();
            File file = Paths.get(directoryCanonicalPath, History.defaultHistoryFileName).toFile();
                System.out.println("Absolute path file [Paths.get().toFile()]: " + file.getCanonicalPath());

            FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.close();

            return true;
        }
        catch (FileNotFoundException exception) {
            System.out.println("{Terminal.defaultHistoryDbFileAlreadyExists()} [FileNotFoundException] (new FileInputStream()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
        catch (IOException exception) {
            System.out.println("{Terminal.defaultHistoryDbFileAlreadyExists()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public File getHistoryDbDefaultFile () {
        String applicationDirectoryPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            System.out.println("Application's directory path: " + applicationDirectoryPath);
                if (applicationDirectoryPath.endsWith(".jar"))
                {
                    applicationDirectoryPath = new File(applicationDirectoryPath).getParent();
                }
                else {
                    applicationDirectoryPath = new File(applicationDirectoryPath).getParent();
                    applicationDirectoryPath = new File(applicationDirectoryPath).getParent();
                }
            System.out.println("Corrected application's directory path (in case of *.jar): " + applicationDirectoryPath);

        return Paths.get(applicationDirectoryPath, History.defaultHistoryFileName).toFile();
    }
    public boolean saveHistoryDb () {
        File fileHistoryDb = getHistoryDbDefaultFile().getParentFile();
            if (!this.historyDbDefaultFileWasLoaded.getValue()) {
                System.out.println("Appending history db: " + fileHistoryDb);
                return saveHistoryDbToDirectory(fileHistoryDb, true);
            }
            else {
                System.out.println("Overwriting history db: " + fileHistoryDb);
                return saveHistoryDbToDirectory(fileHistoryDb, false);
            }
    }
}