package ro.nq.metaexplorer.Utilities;

import javafx.collections.ObservableList;
import ro.nq.metaexplorer.DataEncapsulations.DatabaseEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class History {

    // <TODO> persistence for Db with custom names
    // <TODO> persistence for peers with custom names
// ---- [constants]
    public static final String defaultHistoryFileName = "historyFile.config";
    public static final List<String> possibleHistoryFileNames = List.of(
            History.defaultHistoryFileName,
            "databaseHistory.txt"
    );
// ---- [shared state methods]
    public static LinkedList<File> searchConfigFiles(String searchSpacePath) {
        File searchDirectory = new File(searchSpacePath);

        return Filesystem.recursivelyEnumerateFiles(searchDirectory,
                true, History.possibleHistoryFileNames,
                true, Filesystem.commonDirectoriesToAvoidSearchingIn);
    }
    public static LinkedList<DatabaseEntry> parseObservableHistoryFiles(ObservableList<File> historyFiles) {
        LinkedList<DatabaseEntry> parsedDatabaseEntries = new LinkedList<>();

        System.out.println("split by line separator | trim lines | expect \"<string : custom name>\" <space> <path>");
        for (File historyFile : historyFiles) {
            if (historyFile != null && historyFile.exists() && historyFile.canRead()) {
                String content = Filesystem.readFileToString(historyFile);
                    if (content != null && !content.isBlank()) {
                        String[] splitLines = content.split(System.lineSeparator());
                            for (String splitLine : splitLines) {
                                splitLine = splitLine.trim();
                            }

                        for (String splitLine : splitLines) {
                            int position = 0;

                            // "
                                if (position < splitLine.length() && splitLine.charAt(position) != '"') {
                                    continue;
                                }
                                else {
                                    position++;
                                }

                            // (any \ {"})* -> customName
                            String customName = "";
                                while (position < splitLine.length() && splitLine.charAt(position) != '"') {
                                    customName += splitLine.charAt(position);
                                    position ++;
                                }

                            // "
                                if (position < splitLine.length() && splitLine.charAt(position) != '"') {
                                    continue;
                                }
                                else {
                                    position++;
                                }

                            // (. \ System.lineSeparator())+ -> path
                            StringBuilder path = new StringBuilder();
                                while (position < splitLine.length() && splitLine.charAt(position) != '\n') {
                                    path.append(splitLine.charAt(position));
                                    position ++;
                                }
                                if (path.toString().isBlank()) {
                                    continue;
                                }

                            // commit
                            File file = Paths.get(path.toString().trim()).toFile();
                            DatabaseEntry databaseEntry = new DatabaseEntry(customName, file, null);
                                parsedDatabaseEntries.add(databaseEntry);
                        }
                    }
            }
        }

        return parsedDatabaseEntries;
    }
}
