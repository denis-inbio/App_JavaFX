package ro.nq.metaexplorer.DataEncapsulations;

import java.util.LinkedList;

public class QueuedFilesFilters {
public boolean enableFileName;
public String fileName;
public boolean enableFileType;
public String fileType;
public boolean enableFileSizeMinimum;
public Integer fileSizeMinimum;
public boolean enableFileSizeMaximum;
public Integer fileSizeMaximum;
public Integer enableTags;
public boolean tagsCompositionAnd;
public LinkedList<String> tags;

    public boolean positiveFilterDecision (FileMetadata fileMetadata) {
        boolean positiveFilter = true;

        if (this.enableFileName) {
            positiveFilter = fileMetadata.fileName.contains(this.fileName);
        }
        if (positiveFilter && this.enableFileType) {
            positiveFilter = fileMetadata.fileType.contains(this.fileType);
        }
        if (positiveFilter && this.enableFileSizeMinimum) {
            positiveFilter = (this.fileSizeMinimum <= fileMetadata.fileSize);
        }
        if (positiveFilter && this.enableFileSizeMaximum) {
            positiveFilter = (fileMetadata.fileSize <= this.fileSizeMaximum);
        }
        if (positiveFilter && this.enableTags > 0) {
            boolean positiveFilterTags = this.tagsCompositionAnd;

            for (String tag : this.tags) {
                if (this.tagsCompositionAnd) {
                    positiveFilterTags &= fileMetadata.tags.contains(tag);
                } else {
                    positiveFilterTags |= fileMetadata.tags.contains(tag);
                }
            }

            positiveFilter = positiveFilterTags;
        }

        return positiveFilter;
    }
}
