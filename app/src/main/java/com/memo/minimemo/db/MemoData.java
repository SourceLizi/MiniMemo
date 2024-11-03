package com.memo.minimemo.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "memo_table")
public class MemoData {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public long uid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "time_created")
    public long createTime;

    @ColumnInfo(name = "time_updated")
    public long updateTime;

    public MemoData(){}

    @Ignore
    public MemoData(String title, String content){
        this.content = content;
        this.title = title;
        long curr_time = System.currentTimeMillis();
        this.createTime = curr_time;
        this.updateTime = curr_time;
    }
}
