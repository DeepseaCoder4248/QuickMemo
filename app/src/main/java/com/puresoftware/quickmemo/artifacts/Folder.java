package com.puresoftware.quickmemo.artifacts;

import com.puresoftware.quickmemo.room.UserFolder;

public class Folder {
    public UserFolder getFolder() {
        return folder;
    }

    public int getFolderCnt() {
        return folderCnt;
    }

    final UserFolder folder;
   final int folderCnt;

    public Folder(UserFolder folder, int folderCnt) {
        this.folder = folder;
        this.folderCnt = folderCnt;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "folder=" + folder +
                ", folderCnt=" + folderCnt +
                '}';
    }
}
