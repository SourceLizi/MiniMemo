package com.memo.minimemo.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.memo.minimemo.MainActivity;
import com.memo.minimemo.MemoViewModel;
import com.memo.minimemo.R;
import com.memo.minimemo.databinding.FragmentContentBinding;
import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.transcribe.AssetUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;




public class MemoContentView extends Fragment {

    private FragmentContentBinding binding;
    private MemoViewModel mViewModel;
    private String formatStr;

    private boolean isRecording = false;
    private CircularProgressDrawable circularProgressDrawable;
    private Drawable voice_off, voice_on;

    private Handler handler;

    static final int MSG_TRANSCRIBE_DONE = 1;

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

        this.circularProgressDrawable = new CircularProgressDrawable(getContext());
        this.circularProgressDrawable.setStartEndTrim(0,300);
        this.circularProgressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
        this.circularProgressDrawable.setStrokeWidth(8f);
        this.circularProgressDrawable.setStrokeCap(Paint.Cap.ROUND);
        this.circularProgressDrawable.setCenterRadius(20f);
        this.circularProgressDrawable.setArrowEnabled(false);
        circularProgressDrawable.start();

        Context context = getActivity().getBaseContext();
        this.voice_off = AppCompatResources.getDrawable(context,R.drawable.ic_action_voice_off);
        this.voice_on = AppCompatResources.getDrawable(context,R.drawable.ic_action_voice);

         this.handler = new Handler(msg -> {
             switch (msg.what){
                 case MSG_TRANSCRIBE_DONE:
                     binding.buttonVoice.setImageResource(R.drawable.ic_action_voice_off);
                     binding.textContent.append(msg.obj.toString());
                     binding.buttonVoice.setEnabled(true);
                     break;
                 default:
                     break;
             }
            return true;
        });

        binding.buttonVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording == false){
                    isRecording = true;
                    binding.buttonVoice.setImageDrawable(voice_on);
                }else{
                    isRecording = false;
                    //binding.buttonVoice.setImageDrawable(voice_off);
                    binding.buttonVoice.setImageDrawable(circularProgressDrawable);
                    binding.buttonVoice.setEnabled(false);

                    String sampleFilePath = "samples/jfk.wav";
                    Context context = getActivity().getBaseContext();
                    File filesDir = context.getFilesDir();
                    File sampleFile = AssetUtils.copyFileIfNotExists(context, filesDir, sampleFilePath);
                    mViewModel.getWhisperService().transcribeSample(sampleFile).thenAccept(result -> {
                        if(result != null){
                            Log.i("Whisper", result);
                            Message msg = Message.obtain();
                            msg.what = MSG_TRANSCRIBE_DONE; msg.obj = result;
                            handler.sendMessage(msg);
                        }
                    });

                }
            }
        });
//        binding.buttonSecond.setOnClickListener(v ->
//                NavHostFragment.findNavController(MemoContentView.this)
//                        .navigate(R.id.action_Back2List)
//        );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        this.mViewModel.setContent_binding(null);
        this.mViewModel.setCurrEditing(null);
        binding = null;
    }

}