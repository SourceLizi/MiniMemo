package com.memo.minimemo.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.memo.minimemo.R;

public class MemoListViewHolder extends RecyclerView.ViewHolder {
    private final TextView titleText;
    private final TextView previewText;

    public MemoListViewHolder(View itemView) {
        super(itemView);
        titleText = itemView.findViewById(R.id.item_title);
        previewText = itemView.findViewById(R.id.item_previewText);
    }

    public void bind(String title, String content_preview) {
        titleText.setText(title);
        previewText.setText(content_preview);
    }

    public static MemoListViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_layout, parent, false);
        return new MemoListViewHolder(view);
    }
}
