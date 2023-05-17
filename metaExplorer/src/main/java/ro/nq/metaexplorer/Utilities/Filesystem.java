package ro.nq.metaexplorer.Utilities;

import javafx.scene.image.Image;
import ro.nq.metaexplorer.Applications.Terminal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Filesystem {
// ---- [constants]
    public static final String serverSocketConfiguration_configFileName = "serverSocketConfig.txt";
    public static final List<String> commonDirectoriesToAvoidSearchingIn = List.of(".wine", ".wine32");
    public static final Map<String, String> supportedFileFormatVisualizers = Map.of(
            "pdf", "visualizer0",
            "doc", "visualizer1", "docx", "visualizer1",
            "ods", "visualizer2",
            "xls", "visualizer3", "xlsx", "visualizer3",
            "txt", "visualizer4",
            "png", "visualizer5", "jpeg", "visualizer5", "tiff", "visualizer5"
    );
    public static final List<String> supportedFileTypes = supportedFileFormatVisualizers.keySet().stream().toList();
// ---- [substitutable methods]
    public static LinkedList<File> recursivelyEnumerateFiles (File directory,
                                                              boolean positiveFilterFileByName, List<String> positiveFilterFileName,
                                                              boolean negativeFilterDirectoryByName, List<String> negativeFilterDirectoryName) {
            if (directory == null || !directory.isDirectory()) {
                return new LinkedList<File>();
            }
        LinkedList<File> files = new LinkedList<>();
        LinkedList<File> frontier = new LinkedList<>();
            frontier.add(directory);
        LinkedList<File> visitedDirectories = new LinkedList<>();
        while(frontier.size() > 0) {
            if (frontier.get(0) != null && frontier.get(0).listFiles() != null) {
                for (File file : frontier.get(0).listFiles()) {

                    if (file.isDirectory() && !frontier.contains(file) && !visitedDirectories.contains(file)) {
                        boolean passed = true;
                            if (negativeFilterDirectoryByName && negativeFilterDirectoryName != null) {
                                passed &= !negativeFilterDirectoryName.contains(file.getName());
                            }
                        if (passed) {
                            frontier.add(file);
                        }
                    } else if (file.isFile()) {
                        boolean passed = true;
                            if (positiveFilterFileByName && positiveFilterFileName != null) {
                                passed &= positiveFilterFileName.contains(file.getName());
                            }
                        if (passed) {
                            files.add(file);
                        }
                    }

                }
            }
            visitedDirectories.add(frontier.get(0));
            frontier.remove(0);
        }
        System.out.println("Filesystem traversal from given origin (directory file) has found " + files.size() + " files");
        return files;
    }
    public static LinkedList<File> filterFilesByNameAndSize (LinkedList<File> inputFiles, boolean filterByFileName, String filterFileName, boolean filterByFileSize, long filterFileSize) {
        LinkedList<File> filteredFiles = new LinkedList<>();

        for (int index = 0; index < inputFiles.size(); index++) {
            try {
                File file = inputFiles.get(index);
                    String fileName = file.getName();
                    long fileSize = Files.size(file.toPath());

                boolean passed = true;
                    if (filterByFileName) {
                        passed &= fileName.equals(filterFileName);
                    }
                    if (filterByFileSize) {
                        passed &= (fileSize == filterFileSize);
                    }

                if (passed) {
                    filteredFiles.add(file);
                }
            }
            catch (IOException exception) {
                System.out.println("{filterFilesByNameAndSize} [IOException] () - Files.size(File.toPath()) - " + exception.getMessage());
            }
        }

        return filteredFiles;
    }
    public static boolean compareFilesByCanonicalPath (File left, File right) {
        try {
            String leftCanonicalPath = left.getCanonicalPath();
            String rightCanonicalPath = right.getCanonicalPath();

            return leftCanonicalPath.equals(rightCanonicalPath);
        }
        catch (IOException exception) {
            System.out.println("{compareFilesByCanonicalPath} [IOException] () - File.getCanonicalPath() - " + exception.getMessage());
            return false;
        }
    }
    public static String getUserSearchSpace (boolean userDir_or_userHome) {
        String searchSpacePath = null;
        if (!userDir_or_userHome) {
            searchSpacePath = System.getProperty("user.dir");
        }
        else {
            searchSpacePath = System.getProperty("user.home");
        }

        return searchSpacePath;
    }
    public static String extractFileName (File file) {
        String path = file.getAbsolutePath();

        int indexStemSeparator = path.lastIndexOf("/");
        int indexExtensionSeparator = path.lastIndexOf(".");
            if (indexStemSeparator < indexExtensionSeparator) {
                return path.substring(indexStemSeparator + 1, indexExtensionSeparator);
            }
            else {
                return "";
            }
    }
    public static String extractFileExtension (File file) {
        String path = file.getAbsolutePath();

        int indexOfExtensionSeparator = path.lastIndexOf(".");
            if (indexOfExtensionSeparator >= 0 && (indexOfExtensionSeparator + 1) < path.length()) {
                return path.substring(indexOfExtensionSeparator + 1);
            }
            else {
                return "";
            }
    }
    public static Image loadImage(Terminal application, String absolutePath) {
        Image result = null;
        try {
            application.cachedImages_lock.lock();

            if (!application.cachedImages.containsKey(absolutePath)) {
                System.out.println("{Filesystem.loadImage()} [] () - Image is not cached yet. Searching in path: " + absolutePath);

                File file = new File(absolutePath);
                    if (!file.exists()) {
                        System.out.println("\t{Filesystem.loadImage()} [] (File.exists()) - File does not exist");
                    }
                    else {
                        Image image = new Image(file.toURI().toURL().openStream());
                           application.cachedImages.put(absolutePath, image);
                    }
            } else {
                System.out.println("{Filesystem.loadImage()} [] () - Image is already cached");
            }
            result = application.cachedImages.get(absolutePath);
        }
        catch (MalformedURLException exception) {
            System.out.println("{Filesystem.loadImage()} [MalformedURLException] (File.toURI().toURL()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
        catch (IOException exception) {
            System.out.println("{Filesystem.loadImage()} [IOException] (new Image()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
        finally {
            application.cachedImages_lock.unlock();
        }
        return result;
    }
    public static Image loadImageResource(Terminal application, String path) {
        Image result = null;
        try {
            application.cachedImages_lock.lock();

            if (!application.cachedImages.containsKey(path)) {
                System.out.println("{Filesystem.loadImageResource()} [] () - Image is not cached yet. Searching in path: " + path);
                URL resourceUrl = Filesystem.class.getResource(path);
                    if (resourceUrl == null) {
                        System.out.println("\t{Filesystem.loadImageResource()} [] (Class.getResource()) - Resource URL is null");
                    }
                    else {
                        String urlString = resourceUrl.toExternalForm();
                            assert urlString != null;

                        Image image = new Image(urlString);
                            System.out.println("{Filesystem.loadImageResource()} [] (new Image()) - " + image);

                        application.cachedImages.put(path, image);
                    }
            } else {
                System.out.println("{Filesystem.loadImageResource()} [] () - Image is already cached");
            }
            result = application.cachedImages.get(path);
        }
        finally {
            application.cachedImages_lock.unlock();
        }
        return result;
    }
    public static String readFileToString (File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
                byte[] contentBytes = fileInputStream.readAllBytes();
            fileInputStream.close();

            return new String(contentBytes, StandardCharsets.UTF_8);
        }
        catch (FileNotFoundException exception) {
            System.out.println("{Filesystem.readFileToString()} [FileNotFoundException] () - new FileInputStream() - " + exception.getMessage());
            return null;
        }
        catch (IOException exception) {
            System.out.println("{Filesystem.readFileToString()} [IOException] () - new FileInputStream.readAllBytes() - " + exception.getMessage());
            return null;
        }
    }
}
