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
                    mViewModel.deleteById(id);
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

        MainActivity activity = (MainActivity)getActivity();
        if(activity != null){
            this.mViewModel = new ViewModelProvider(activity).get(MemoViewModel.class);
        }else{
            this.mViewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        }
        mViewModel.setLifecycleOwner(getViewLifecycleOwner());

        RecyclerView recyclerView = view.findViewById(R.id.all_memo_list);
        final MemoListAdapter adapter = mViewModel.getAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        mViewModel.setDefaultData();

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

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.setLifecycleOwner(null);
        binding = null;
    }

}