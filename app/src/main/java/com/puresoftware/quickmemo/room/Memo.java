package com.puresoftware.quickmemo.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Memo {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "star")
    public boolean star;

    @ColumnInfo(name = "lock")
    public boolean lock;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "trash")
    public boolean isTrash;

    @ColumnInfo(name = "folder_uid")
    public String folder;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setTrash(boolean trash) {
        isTrash = trash;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public String toString() {
        return "Memo{" +
                "uid=" + uid +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", star=" + star +
                ", lock=" + lock +
                ", timestamp=" + timestamp +
                ", isTrash=" + isTrash +
                ", folder='" + folder + '\'' +
                '}';
    }
}
