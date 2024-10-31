package com.memo.minimemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.memo.minimemo.databinding.FragmentListBinding;
import com.memo.minimemo.entity.MemoListItem;

import java.util.ArrayList;
import java.util.Objects;

public class MemoListView extends Fragment {

    private FragmentListBinding binding;
    private MemoItemAdapter mItemAdapter;

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

        binding.allMemoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                NavHostFragment.findNavController(MemoListView.this)
                        .navigate(R.id.action_OpenDetail);
            }
        });
        mItemAdapter=new MemoItemAdapter(MemoListView.this.requireContext(),
                R.layout.list_item_layout,new ArrayList<>());
        binding.allMemoList.setAdapter(mItemAdapter);
        mItemAdapter.add(new MemoListItem(0,"标题","这是一个条目"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}