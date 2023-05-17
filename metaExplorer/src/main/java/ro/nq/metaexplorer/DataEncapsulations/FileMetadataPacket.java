package ro.nq.metaexplorer.DataEncapsulations;

import ro.nq.metaexplorer.Utilities.Filesystem;
import ro.nq.metaexplorer.Utilities.Queries;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

public class FileMetadataPacket implements Serializable {
public String fileName;
public String fileType;
public long fileSize;
public String tags;
    public FileMetadataPacket(File file, String tags) throws IOException {
        this.fileName = file.getName();
        this.fileType = Filesystem.extractFileExtension(file);
        this.fileSize = Files.size(file.toPath());
        this.tags = Queries.normalizedTagsMerged(tags);
    }
    public static boolean equalsByFileAttributes (FileMetadataPacket left, FileMetadataPacket right) {
        return  left.fileName.equals(right.fileName) &&
                left.fileType.equals(right.fileType) &&
                left.fileSize == right.fileSize;
    }
}
