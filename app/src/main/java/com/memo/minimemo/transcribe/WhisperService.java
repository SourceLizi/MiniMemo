package com.memo.minimemo.transcribe;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioRecord;
import android.util.Log;

import com.whispercpp.java.whisper.WhisperContext;
import com.whispercpp.java.whisper.WhisperLib;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.ExecutionException;

public class WhisperService {
    private final ExecutorService executor;

    public static final String modelFilePath = "models/ggml-base-q5_1.bin";
    public static final int audioSampleRate = 16000; //pcm 16bit mono
    public static final int audioMaxSec = 50;
    private WhisperContext whisperContext;

    private final Context app_context;

    public WhisperService(Application context) {
        this.executor = Executors.newFixedThreadPool(1);
        this.app_context = context;
    }

    public boolean isRunning(){ return (whisperContext != null && whisperContext.isRunning()); }

    private void loadModel() {
        long start = System.currentTimeMillis();

//        File filesDir = app_context.getFilesDir();
//        File modelFile = AssetUtils.copyFileIfNotExists(app_context, filesDir, modelFilePath);
//        String realModelFilePath = modelFile.getAbsolutePath();
//        whisperContext = WhisperContext.createContextFromFile(realModelFilePath);
        AssetManager assetManager = app_context.getAssets();
        whisperContext = WhisperContext.createContextFromAsset(assetManager, modelFilePath);

        long end = System.currentTimeMillis();
        Log.i("Whisper", "model load successful:" + (end - start) + "ms");
    }


    public CompletableFuture<String> transcribeSample(AudioRecord audioRecord, WhisperLib.callback_fn cb) {
        if(whisperContext != null && whisperContext.isRunning()) {
            Log.w("Whisper","please wait for model finish running");
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            loadModel();

            int max_size = audioSampleRate * audioMaxSec;
            short[] buff = new short[max_size];
            int real_size = audioRecord.read(buff, 0, max_size);
            if (real_size > 0) {
                float[] buff_float = new float[real_size];
                for (int i = 0; i < real_size; i++) {
                    buff_float[i] = Math.max(-1f, Math.min(1f, buff[i] / 32767.0f));
                }
                long start = System.currentTimeMillis();
                try {
                    String result = whisperContext.transcribeData(buff_float,cb);
                    whisperContext.stopTranscribe();
                    whisperContext.release();
                    whisperContext = null;
                    long end = System.currentTimeMillis();
                    Log.i("Whisper", "Transcript successful:" + (end - start) + "ms");
                    return result;
                } catch (ExecutionException | InterruptedException e) {
                    return null;
                }
            } else {
                Log.i("Whisper", "Error reading buffer, code=" + (real_size));
                return null;
            }

//     try {
//         Thread.sleep(2000);
//     } catch (InterruptedException e) {
//         throw new RuntimeException(e);
//     }
//     return "And so my fellow Americans ask not what your country can do for you ask what you can do for your country.";
        }, this.executor);
    }

    public void stopAll() {
        if(whisperContext != null){
            whisperContext.stopTranscribe();
        }
    }


    @Override
    protected void finalize() throws Throwable {
        executor.shutdownNow();
        try {
            if(whisperContext != null) {
                whisperContext.stopTranscribe();
                whisperContext.release();
            }
        } catch (ExecutionException | InterruptedException ignored) {}
        super.finalize();
    }
}
