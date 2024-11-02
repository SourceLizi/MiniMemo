package com.memo.minimemo.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.memo.minimemo.entity.MemoData;

@Database(entities = {MemoData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MemoDao userDao();
}