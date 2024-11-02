package com.memo.minimemo.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.memo.minimemo.MainActivity;
import com.memo.minimemo.MemoItemAdapter;
import com.memo.minimemo.R;
import com.memo.minimemo.databinding.FragmentListBinding;
import com.memo.minimemo.entity.MemoListItem;

public class MemoListView extends Fragment {

    private FragmentListBinding binding;
    private MemoItemAdapter mItemAdapter;
    private MainActivity m_mainActivity = null;

    private void showPopupMenu(View v,int position){
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_list_popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.action_delete){
                    Log.i("TAG","onDelClick");
                }else if(id == R.id.action_rename_title){
                    Log.i("TAG","onRenameClick");
                }
                return true;
            }
        });
        //显示菜单
        popupMenu.show();

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Activity main_activity = getActivity();
        if (main_activity instanceof MainActivity ){
            this.m_mainActivity = (MainActivity)main_activity;
            this.m_mainActivity.createItemAdapter();
            this.mItemAdapter = this.m_mainActivity.getItemAdapter();

            binding.allMemoList.setAdapter(this.mItemAdapter);
            for (int i = 0; i < 20; i++) {
                this.mItemAdapter.add(new MemoListItem(i,"标题"+i,"这是一个条目"));
            }
        }

        binding.allMemoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putLong("id",id);
                NavHostFragment.findNavController(MemoListView.this)
                        .navigate(R.id.action_OpenDetail,bundle);

            }
        });
        binding.allMemoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showPopupMenu(view,position);
                return true;
            }
        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}