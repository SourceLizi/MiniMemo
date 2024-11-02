package com.memo.minimemo.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MemoData {
    @PrimaryKey
    public long uid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "time_created")
    public long createTime;

    @ColumnInfo(name = "time_updated")
    public long updateTime;
}
