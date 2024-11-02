package com.memo.minimemo.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.memo.minimemo.databinding.FragmentContentBinding;

public class MemoContentView extends Fragment {

    private FragmentContentBinding binding;

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
        binding.textTitle.setText(String.valueOf(getArguments().getLong("id")));
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