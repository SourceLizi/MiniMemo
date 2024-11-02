package com.memo.minimemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.memo.minimemo.entity.MemoListItem;

import java.util.List;

public class MemoItemAdapter extends ArrayAdapter<MemoListItem> {

    private List<MemoListItem> memoList;
    private Context mContext;

    //用于将上下文、listview 子项布局的 id 和数据都传递过来
    public MemoItemAdapter(@NonNull Context context, int resource, @NonNull List<MemoListItem> list) {
        super(context, resource, list);
        this.mContext = context;
        this.memoList = list;
    }

    @Override
    public int getCount() {
        return this.memoList.size();
    }

    @Override
    public MemoListItem getItem(int position) {
        return this.memoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.memoList.get(position).getId();
    }


    //增加一个方法添加动态数据
    public void add(MemoListItem item) {
        this.memoList.add(item);
        notifyDataSetChanged();
    }


    //每个子项被滚动到屏幕内的时候会被调用
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //为每一个子项加载设定的布局
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(this.mContext).inflate(R.layout.list_item_layout,parent,false);
        } else {
            view = convertView;
        }
        MemoListItem curr_item = getItem(position);
        if(curr_item != null){
            TextView titleTextBox=view.findViewById(R.id.item_title);
            TextView previewTextBox=view.findViewById(R.id.item_previewText);
            // 设置要显示内容

            titleTextBox.setText(curr_item.getTitle());
            previewTextBox.setText(curr_item.getPreviewText());
        }


        return view;

    }
}
