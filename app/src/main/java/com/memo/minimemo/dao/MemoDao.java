package com.memo.minimemo.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.memo.minimemo.entity.MemoData;

import java.util.List;

@Dao
public interface MemoDao {
    @Query("SELECT * FROM memo_table")
    List<MemoData> selectAll();

    @Query("SELECT * FROM memo_table WHERE uid = (:uid)")
    MemoData selectById(long uid);

    @Query("SELECT * FROM memo_table WHERE content LIKE :f OR title LIKE :f")
    List<MemoData> findByString(String f);

    @Insert
    void insertAll(MemoData... data);

    @Delete
    void delete(MemoData data);

    @Update
    void update(MemoData... data);
}
