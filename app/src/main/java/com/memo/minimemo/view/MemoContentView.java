package com.memo.minimemo.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.OnReceiveContentListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.memo.minimemo.MainActivity;
import com.memo.minimemo.MemoViewModel;
import com.memo.minimemo.R;
import com.memo.minimemo.databinding.FragmentContentBinding;
import com.memo.minimemo.db.MemoData;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoContentView extends Fragment {

    private FragmentContentBinding binding;
    private MemoViewModel mViewModel;
    private String formatStr;

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
        MainActivity activity = (MainActivity)getActivity();
        if(activity != null){
            this.mViewModel = new ViewModelProvider(activity).get(MemoViewModel.class);
        }else{
            this.mViewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        }

        this.formatStr = getResources().getString(R.string.info_text);
        this.mViewModel.setContent_binding(binding);

        if(getArguments() != null) {
            this.mViewModel.getById(getArguments().getLong("id")).observe(getViewLifecycleOwner(),
                    new Observer<MemoData>() {
                        @Override
                        public void onChanged(MemoData memoData) {

                            mViewModel.setCurrEditing(memoData);

                            binding.textTitle.setText(memoData.title);
                            binding.textContent.setText(memoData.content);
                            MainActivity activity = (MainActivity)getActivity();
                            if(activity != null){
                                activity.setDoneVisible(false);
                            }

                            SimpleDateFormat format;
                            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time_str = format.format(new Date(memoData.createTime));
                            binding.textInfo.setText(String.format(formatStr,time_str,memoData.content.length()));
                        }
                    });
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                MainActivity activity = (MainActivity)getActivity();
                if(activity != null){
                    activity.setDoneVisible(true);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        };
        binding.textTitle.addTextChangedListener(watcher);
        binding.textContent.addTextChangedListener(watcher);
//        binding.buttonSecond.setOnClickListener(v ->
//                NavHostFragment.findNavController(MemoContentView.this)
//                        .navigate(R.id.action_Back2List)
//        );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mViewModel.setContent_binding(null);
        this.mViewModel.setCurrEditing(null);
        binding = null;
    }

}