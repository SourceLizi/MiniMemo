package com.memo.minimemo.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MemoDao {
    @Query("SELECT * FROM memo_table")
    LiveData<List<MemoData>> selectAll();

    @Query("SELECT * FROM memo_table WHERE uid = (:uid)")
    LiveData<MemoData> selectById(long uid);

    @Query("SELECT * FROM memo_table WHERE content LIKE :f OR title LIKE :f")
    LiveData<List<MemoData>> findByString(String f);

    @Insert
    void insertAll(MemoData... data);

    @Query("DELETE FROM memo_table")
    void deleteAll();

    @Query("DELETE FROM memo_table WHERE uid = (:uid)")
    void deleteById(long uid);

    @Update
    void update(MemoData... data);
}
