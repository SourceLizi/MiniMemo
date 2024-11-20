package com.memo.minimemo.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.android.material.snackbar.Snackbar;
import com.memo.minimemo.MainActivity;
import com.memo.minimemo.MemoViewModel;
import com.memo.minimemo.R;
import com.memo.minimemo.databinding.FragmentContentBinding;
import com.memo.minimemo.db.MemoData;
import com.memo.minimemo.transcribe.WhisperService;
import com.whispercpp.java.whisper.WhisperLib;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;


public class MemoContentView extends Fragment {

    private FragmentContentBinding binding;
    private MemoViewModel mViewModel;
    private String formatStr;
    private boolean isRecording = false;

    private CircularProgressDrawable circularProgressDrawable;
    private Drawable voice_off, voice_on;
    private AudioRecord audioRecord;
    private CompletableFuture<String> future_recog = null;

    private Handler handler;
    private static final int MSG_TRANSCRIBE_DONE = 1;
    private static final int MSG_START_REC = 2;
    private static final int MSG_STOP_REC = 3;
    private static final int MSG_NEW_SEGMENT = 4;

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
        binding.textContent.setEnabled(true);
        binding.textTitle.setEnabled(true);

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

                            DateFormat format;
                            Locale locale = getResources().getConfiguration().getLocales().get(0);
                            format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM
                                    , locale);
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

        this.circularProgressDrawable = new CircularProgressDrawable(requireContext());
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
        this.audioRecord = mViewModel.getAudioRecord();

         this.handler = new Handler(msg -> {
             if (msg.what == MSG_TRANSCRIBE_DONE){
                 binding.buttonVoice.setImageResource(R.drawable.ic_action_voice_off);
//                 if (msg.obj != null) {
//                     binding.textContent.append(msg.obj.toString());
//                 }
                 binding.buttonVoice.setEnabled(true);
                 this.future_recog = null;
             }else if(msg.what == MSG_NEW_SEGMENT){
                 if (msg.obj != null) {
                     binding.textContent.append(msg.obj.toString());
                 }
             }
             else if(msg.what == MSG_START_REC){
                 binding.buttonVoice.setImageDrawable(voice_on);
             }else if(msg.what == MSG_STOP_REC){
                 this.future_recog = mViewModel.getWhisperService().transcribeSample(audioRecord,
                         new WhisperLib.callback_fn() {
                     @Override
                     public void onNewSegment(String new_seg, int seg_idx) {
                         if(handler != null){
                             Message.obtain(handler,MSG_NEW_SEGMENT,new_seg).sendToTarget();
                         }
                     }
                 });
                 if(this.future_recog == null){
                     binding.buttonVoice.setImageResource(R.drawable.ic_action_voice_off);
                     binding.buttonVoice.setEnabled(true);
                     String msg_text = getResources().getString(R.string.whisper_failed_msg);
                     Snackbar.make(binding.getRoot(), msg_text, Snackbar.LENGTH_LONG).show();
                 }else{
                     if(activity != null){
                         InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                         imm.hideSoftInputFromWindow(binding.textContent.getWindowToken(),0);
                         imm.hideSoftInputFromWindow(binding.textTitle.getWindowToken(),0);
                     }
                     this.future_recog.thenAccept(result -> {
                         if(handler != null){
                             //Log.i("Whisper", result);
                             Message.obtain(handler,MSG_TRANSCRIBE_DONE,result).sendToTarget();
                         }
                     });
                     //binding.buttonVoice.setImageDrawable(voice_off);
                     binding.buttonVoice.setImageDrawable(circularProgressDrawable);
                     binding.buttonVoice.setEnabled(false);
                 }

             }
            return true;
        });


        if(audioRecord != null){
            audioRecord.setNotificationMarkerPosition(WhisperService.audioSampleRate * WhisperService.audioMaxSec);
            audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioRecord audioRecord) {
                    audioRecord.stop();
                    isRecording = false;
                    Message.obtain(handler,MSG_STOP_REC).sendToTarget();
                }
                @Override
                public void onPeriodicNotification(AudioRecord audioRecord) {}
            });
        }


        binding.buttonVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity)getActivity();
                if(activity == null) return;
                if (ActivityCompat.checkSelfPermission(
                        activity.getApplication(), Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED) {
                    String msg_text = getResources().getString(R.string.voice_permission_msg);
                    String msg_btn_text = getResources().getString(R.string.voice_permission_btn);
                    Snackbar.make(binding.getRoot(), msg_text, Snackbar.LENGTH_LONG)
                            .setAction(msg_btn_text, views -> activity.requestPermissionLauncher.launch(
                                    Manifest.permission.RECORD_AUDIO)).show();
                }else{
                    if(!isRecording){
                        if(mViewModel.getWhisperService().isRunning()){
                            String msg_text = getResources().getString(R.string.whisper_running_msg);
                            Snackbar.make(binding.getRoot(), msg_text, Snackbar.LENGTH_LONG).show();
                        }else{
                            isRecording = true;
                            if(audioRecord != null) {
                                audioRecord.startRecording();
                            }
                            Message.obtain(handler,MSG_START_REC).sendToTarget();
                        }
                    }else{
                        isRecording = false;
                        if(audioRecord != null) {
                            audioRecord.stop();
                        }
                        Message.obtain(handler,MSG_STOP_REC).sendToTarget();
                    }
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if(this.audioRecord != null){
            if(this.audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
                this.audioRecord.stop();
            }
        }
        if(this.future_recog != null && !this.future_recog.isDone()){
            future_recog.cancel(true);
            mViewModel.getWhisperService().stopAll();
            Log.i("Whisper", "Cancelled running whisper");
        }

        this.mViewModel.setContent_binding(null);
        this.mViewModel.setCurrEditing(null);
        binding = null;
    }

}