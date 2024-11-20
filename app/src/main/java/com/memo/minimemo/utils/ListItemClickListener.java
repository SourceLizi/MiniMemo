package com.memo.minimemo.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ListItemClickListener extends RecyclerView.SimpleOnItemTouchListener {

    private final GestureDetectorCompat mGestureDetectorCompat;
    private final RecyclerView mRecyclerView;

    /**
     * 通过构造传入我们的RecyclerView,并初始化GestureDetectorCompat
     */
    public ListItemClickListener(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        mGestureDetectorCompat = new GestureDetectorCompat(
                mRecyclerView.getContext(),new ListGestureListener());
    }

    /**
     * 将事件交给GestureDetectorCompat处理
     *   并将MotionEvent 传入GestureDetectorCompat使得可以获取触摸的坐标
     */
    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        mGestureDetectorCompat.onTouchEvent(e);
    }

    /**
     * 不拦截触摸事件,将事件交给GestureDetectorCompat处理
     */
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        mGestureDetectorCompat.onTouchEvent(e);
        return false;
    }

    /**
     * 定义一个抽象回调方法
     */
    public abstract void onItemClick(RecyclerView.ViewHolder vh, int pos);

    public abstract void onItemLongClick(RecyclerView.ViewHolder vh, int pos);

    private class ListGestureListener extends GestureDetector.SimpleOnGestureListener{

        /**
         * 这个方法在简单的点击屏幕时执行
         *
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (childView != null) {
                RecyclerView.ViewHolder viewHolder =
                        mRecyclerView.getChildViewHolder(childView);
                int pos = viewHolder.getAdapterPosition();
                onItemClick(viewHolder,pos);//触发回调
            }
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (childView != null) {
                RecyclerView.ViewHolder viewHolder =
                        mRecyclerView.getChildViewHolder(childView);
                int pos = viewHolder.getAdapterPosition();
                onItemLongClick(viewHolder,pos);
            }
        }
    }
}