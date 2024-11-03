package com.memo.minimemo.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.memo.minimemo.MainActivity;
import com.memo.minimemo.MemoListAdapter;
import com.memo.minimemo.MemoViewModel;
import com.memo.minimemo.R;
import com.memo.minimemo.databinding.FragmentListBinding;
import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.utils.ListItemClickListener;

public class MemoListView extends Fragment {

    private FragmentListBinding binding;
    private MemoViewModel mViewModel;

    private void showPopupMenu(View v,long id){
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_list_popup, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menu_id = item.getItemId();
                if(menu_id == R.id.action_delete){
                    Log.i("TAG","onDelClick,id=" + String.valueOf(id));
                }else if(menu_id == R.id.action_rename_title){
                    Log.i("TAG","onRenameClick,id=" + String.valueOf(id));
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
            Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        RecyclerView recyclerView = view.findViewById(R.id.all_memo_list);
        final MemoListAdapter adapter = new MemoListAdapter(new MemoListAdapter.MemoDataDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));

        recyclerView.addOnItemTouchListener(new ListItemClickListener(recyclerView){
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh, int pos) {
                MemoData curr = adapter.getItemByPos(pos);
                Bundle bundle = new Bundle();
                bundle.putLong("id",curr.uid);
                NavHostFragment.findNavController(MemoListView.this)
                        .navigate(R.id.action_OpenDetail,bundle);
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh, int pos) {
                MemoData curr = adapter.getItemByPos(pos);
                showPopupMenu(vh.itemView,curr.uid);
            }
        });

        this.mViewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        // Update the cached copy of the words in the adapter.
        this.mViewModel.getAll().observe(getViewLifecycleOwner(), adapter::submitList);

//        Activity main_activity = getActivity();
//        if (main_activity instanceof MainActivity ){
//            this.m_mainActivity = (MainActivity)main_activity;
//            this.m_mainActivity.createItemAdapter();
//            this.mItemAdapter = this.m_mainActivity.getItemAdapter();
//
//            binding.allMemoList.setAdapter(this.mItemAdapter);
//            for (int i = 0; i < 20; i++) {
//                this.mItemAdapter.add(new MemoListItem(i,"标题"+i,"这是一个条目"));
//            }
//        }
//
//        binding.allMemoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
//                Bundle bundle = new Bundle();
//                bundle.putLong("id",id);
//                NavHostFragment.findNavController(MemoListView.this)
//                        .navigate(R.id.action_OpenDetail,bundle);
//
//            }
//        });
//        binding.allMemoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                showPopupMenu(view,position);
//                return true;
//            }
//        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}