package com.puresoftware.quickmemo.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class UserFolder {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "time")
    public long timestamp;

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserFolder{" +
                "uid=" + uid +
                ", title='" + title + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
