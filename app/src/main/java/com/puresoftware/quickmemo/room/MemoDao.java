package com.puresoftware.quickmemo.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemoDao {

//    @Query("SELECT * FROM user")
//    List<User> getAll();
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    List<User> loadAllByIds(int[] userIds);
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    User findByName(String first, String last);
//
//    @Insert
//    void insert(User user);
//
//    @Insert
//    void insertAll(User... users);
//
//    @Delete
//    void delete(User user);
//
//    @Query("DELETE FROM user where first_name LIKE :firstName")
//    void deleteByName(String firstName);

    @Query("SELECT * FROM Memo")
    List<Memo> getAll();

    @Query("SELECT * FROM Memo where trash=:isTrash")
    List<Memo> getNotTrashAll(boolean isTrash);

    @Insert
    void insert(Memo memo);

    @Delete
    void delete(Memo memo);

    @Query("UPDATE Memo set title=:title,content=:content,star=:star,lock=:lock, folder_uid=:folder where timestamp=:timestamp")
    void updateData(String title, String content, boolean star, boolean lock, long timestamp, String folder);

    @Query("UPDATE Memo set trash=:isTransh where uid=:uid")
    void updateTrash(boolean isTransh, int uid);

    @Query("UPDATE Memo set star=:star where uid=:uid")
    void updateStar(boolean star, int uid);

    @Insert
    void insertFolder(UserFolder userFolder);

    @Query("UPDATE UserFolder set title=:title where uid=:uid")
    void updateFolder(String title, int uid);

    @Query("SELECT COUNT(*) FROM memo WHERE folder_uid = :folderId")
    int getFolderCount(String folderId);

    @Delete
    void deleteFolder(UserFolder userFolder);

    @Query("SELECT * FROM UserFolder")
    List<UserFolder> getFolderAll();
}
