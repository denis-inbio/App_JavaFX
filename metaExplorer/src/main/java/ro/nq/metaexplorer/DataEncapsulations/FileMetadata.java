package ro.nq.metaexplorer.DataEncapsulations;

import ro.nq.metaexplorer.Utilities.Filesystem;
import ro.nq.metaexplorer.Utilities.Queries;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileMetadata {
public Connection connection;
public Socket sourceSocket;
public File file;
public long id;
public String fileName;
public String fileType;
public long fileSize;
public String tags;
public byte[] content;
    // <NOTE> this is the constructor for when adding observableQueuedFiles
    public FileMetadata(File file, String tags) throws IOException {
        this.connection = null;
        this.sourceSocket = null;
        this.id = -1;   // this means it has no known correspondent in the database
        this.file = file;
        this.fileName = this.file.getName();
        this.fileType = Filesystem.extractFileExtension(this.file);
        this.fileSize = Files.size(this.file.toPath());
        this.tags = Queries.normalizedTagsMerged(tags);
        this.content = null;
    }
    // <NOTE> this is the constructor for when extracting data from the database
    public FileMetadata(Connection connection, long id, String fileName, String fileType, long fileSize, String tags, byte[] content) {
        this.connection = connection;
        this.sourceSocket = null;
        this.id = id;
        this.file = null;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.tags = tags;
        this.content = content;
    }
    // <NOTE> this is the constructor based on a FileMetadataPacket
    public FileMetadata(Socket sourceSocket, FileMetadataPacket fileMetadataPacket) {
        this.connection = null;
        this.sourceSocket = sourceSocket;
        this.id = -1;
        this.file = null;
        this.fileName = fileMetadataPacket.fileName;
        this.fileType = fileMetadataPacket.fileType;
        this.fileSize = fileMetadataPacket.fileSize;
        this.tags = fileMetadataPacket.tags;
        this.content = null;
    }
    // <NOTE> this is the constructor for when downloading from a peer
    public FileMetadata(Socket sourceSocket, String fileName, String fileType, long fileSize, String tags, byte[] content) {
        this.connection = null;
        this.sourceSocket = sourceSocket;
        this.id = -1;
        this.file = null;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.tags = tags;
        this.content = content;
    }

    public static boolean equalsByCanonicalPath(FileMetadata left, FileMetadata right) {
        try {
            String leftCanonicalPath = left.file.getCanonicalPath();
            String rightCanonicalPath = right.file.getCanonicalPath();

            return leftCanonicalPath.equals(rightCanonicalPath);
        }
        catch (IOException exception) {
            System.out.println("{FileMetadata.equalsByCanonicalPath()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public static boolean equalsByContent(FileMetadata left, FileMetadata right) {
        try {
            long contentMismatch = Files.mismatch(left.file.toPath(), right.file.toPath());
            return (contentMismatch == -1L);
        }
        catch (IOException exception) {
            System.out.println("{FileMetadata.equalsByCanonicalPath()} [IOException] (File.getCanonicalPath()) - " + exception.getMessage() + "\n" + exception.getCause());
            return false;
        }
    }
    public boolean readContentFromDatabase () {
            if (this.content != null) {
                return true;
            }

        if (this.id != -1 && this.connection != null) {
            PreparedStatement preparedStatement = Queries.prepareSelectByIdStatement(this.connection, this.id);
                if (preparedStatement == null) {
                    return false;
                }

            try {
                ResultSet result = preparedStatement.executeQuery();
                if (result.next()) {
                    this.content = result.getBytes(6);
                    return true;
                }
                else {
                    return false;
                }
            }
            catch(SQLException exception) {
                System.out.println("{FileMetadata.readContentFromDatabase()} [SQLException] (PreparedStatement.executeQuery()) - " + exception.getMessage() + "\n" + exception.getCause());
                return false;
            }
        }
        else {
            return false;
        }
    }
    public static boolean equalsByFileAttributes (FileMetadata left, FileMetadata right) {
        return  left.fileName.equals(right.fileName) &&
                left.fileType.equals(right.fileType) &&
                left.fileSize == right.fileSize;
    }
}
