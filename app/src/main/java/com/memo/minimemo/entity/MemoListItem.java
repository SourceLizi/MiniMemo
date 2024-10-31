package com.memo.minimemo.entity;

public class MemoListItem {
    private int id;
    private String title;
    private String previewText;

    public MemoListItem(int id, String title, String previewText){
        this.id = id;
        this.title = title;
        this.previewText = previewText;
    }

    public int getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getPreviewText(){return this.previewText;}

    public void setTitle(String title){this.title = title;}
    public void setPreviewText(String previewText){this.previewText = previewText;}

}
