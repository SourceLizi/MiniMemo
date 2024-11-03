package com.memo.minimemo.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.memo.minimemo.MemoViewModel;
import com.memo.minimemo.databinding.FragmentContentBinding;
import com.memo.minimemo.db.MemoData;

public class MemoContentView extends Fragment {

    private FragmentContentBinding binding;
    private MemoViewModel mViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentContentBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //binding.textTitle.setText(String.valueOf(getArguments().getLong("id")));
        this.mViewModel = new ViewModelProvider(this).get(MemoViewModel.class);

        if(getArguments() != null) {
            this.mViewModel.getById(getArguments().getLong("id")).observe(getViewLifecycleOwner(),
                    new Observer<MemoData>() {
                        @Override
                        public void onChanged(MemoData memoData) {
                            binding.textTitle.setText(memoData.title);
                            binding.textContent.setText(memoData.content);
                        }
                    });
        }
//        binding.buttonSecond.setOnClickListener(v ->
//                NavHostFragment.findNavController(MemoContentView.this)
//                        .navigate(R.id.action_Back2List)
//        );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}