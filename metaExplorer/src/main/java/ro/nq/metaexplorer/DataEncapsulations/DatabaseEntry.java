package ro.nq.metaexplorer.DataEncapsulations;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseEntry {
    public SimpleStringProperty customName;
    public Lock customName_lock;
    public SimpleObjectProperty<File> file;
    public Lock file_lock;
    public SimpleObjectProperty<Connection> connection;
    public Lock connection_lock;
    public SimpleLongProperty fileCount;
    public Lock fileCount_lock;
    public DatabaseEntry(String customName, File file, Connection connection) {
        this.customName = new SimpleStringProperty(customName);
            this.customName_lock = new ReentrantLock();
        this.file = new SimpleObjectProperty<>(file);
            this.file_lock = new ReentrantLock();
        this.connection_lock = new ReentrantLock();
            this.connection = new SimpleObjectProperty<>(connection);
        this.fileCount = new SimpleLongProperty(-1);
            this.fileCount_lock = new ReentrantLock();
    }

    public boolean isDatabaseFileValid() {
        try {
            String canonicalPath = this.file.getValue().getCanonicalPath();
                return true;
        }
        catch (IOException exception) {
            System.out.println("{DatabaseEntry.isDatabaseFileValid()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public static boolean equalsCanonicalPath(DatabaseEntry left, DatabaseEntry right) {
        try {
            boolean validity = left.isDatabaseFileValid();
                if (!validity) {
                    System.out.println("{DatabaseEntry.equals()} [] (DatabaseEntry.isDatabaseFileValid()) - Returned false for ::left");
                }

            validity = right.isDatabaseFileValid();
            if (!validity) {
                System.out.println("{DatabaseEntry.equals()} [] (DatabaseEntry.isDatabaseFileValid()) - Returned false for ::right");
            }

            String leftCanonicalPath = left.file.getValue().getCanonicalPath();
            String rightCanonicalPath = right.file.getValue().getCanonicalPath();
                return leftCanonicalPath.equals(rightCanonicalPath);
        }
        catch (IOException exception) {
            System.out.println("{DatabaseEntry.equals()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public void closeConnection () {
        if (this.connection.getValue() != null) {
            try {
                this.connection_lock.lock();
                    this.connection.getValue().close();
                    this.connection.setValue(null);
            }
            catch (SQLException exception) {
                System.out.println("{DatabaseEntry} [SQLException] (Connection.close()) - " + exception.getMessage() + "\n" + exception.getCause());
            }
            finally {
                this.connection_lock.unlock();
            }
        }
    }
}
