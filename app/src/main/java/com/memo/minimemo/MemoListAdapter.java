package com.memo.minimemo;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.view.MemoListViewHolder;

import java.util.List;

public class MemoListAdapter extends ListAdapter<MemoData, MemoListViewHolder> {

    public MemoListAdapter(@NonNull DiffUtil.ItemCallback<MemoData> diffCallback) {
        super(diffCallback);
    }


    @NonNull
    @Override
    public MemoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MemoListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(MemoListViewHolder holder, int position) {
        MemoData current = getItem(position);
        holder.bind(current.title,current.content);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).uid;
    }

    public MemoData getItemByPos(int position){
        return getItem(position);
    }

    public static class MemoDataDiff extends DiffUtil.ItemCallback<MemoData> {

        @Override
        public boolean areItemsTheSame(@NonNull MemoData oldItem, @NonNull MemoData newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MemoData oldItem, @NonNull MemoData newItem) {
            return oldItem.title.equals(newItem.title) && oldItem.content.equals(newItem.content);
        }
    }
}
