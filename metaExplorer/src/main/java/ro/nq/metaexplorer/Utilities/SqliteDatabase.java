package ro.nq.metaexplorer.Utilities;

import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.DatabaseEntry;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteDatabase {
    public static final String jdbcConnectionPrefix = "jdbc:sqlite:";

    public static DatabaseEntry connectToDatabase (Terminal application, File file) {
            if (file == null) {
                return null;
            }

        try {
            String canonicalPath = file.getCanonicalPath();
            DatabaseEntry databaseEntry = new DatabaseEntry("", file, null);

            try {
                databaseEntry.connection_lock.lock();

                Connection connection = DriverManager.getConnection(SqliteDatabase.jdbcConnectionPrefix + canonicalPath);
                databaseEntry.connection.setValue(connection);

                boolean newTableCreated = Queries.evaluateCreateTableIfNotExists(databaseEntry.connection.getValue());
                System.out.println("New table was created: " + newTableCreated);

                long countFiles = Queries.countFilesStatement(databaseEntry.connection.getValue());
                databaseEntry.fileCount.setValue(countFiles);

            } catch (SQLException exception) {
                System.out.println("{DatabaseHistoryView.handleTryToConnectToDatabase()} [SQLException] (DriverManager.getConnection()) - " + exception.getMessage() + "\n" + exception.getCause());
                return null;
            } finally {
                databaseEntry.connection_lock.unlock();
            }

            return databaseEntry;
        }
        catch (IOException exception) {
            System.out.println("{SqliteDatabase.connectToDatabase()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            return null;
        }
    }
};