package com.memo.minimemo.transcribe;

import android.app.Application;
import android.media.AudioRecord;
import android.util.Log;

import com.whispercpp.java.whisper.WhisperContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class WhisperService {
    private boolean isLoad = false;
    private final ExecutorService executor;

    public static final String modelFilePath = "models/ggml-tiny-q8_0.bin";
    public static final int audioSampleRate = 16000; //pcm 16bit mono
    public static final int audioMaxSec = 50;
    private WhisperContext whisperContext;

    public WhisperService(Application context) {
        this.executor = Executors.newFixedThreadPool(2);
        loadModel(context);
    }

    public void loadModel(Application context) {
        this.executor.submit(() -> {
            if (isLoad) return;
            Log.i("Whisper", "load model from :" + modelFilePath + "\n");

            long start = System.currentTimeMillis();

            File filesDir = context.getFilesDir();
            File modelFile = AssetUtils.copyFileIfNotExists(context, filesDir, modelFilePath);
            String realModelFilePath = modelFile.getAbsolutePath();
            whisperContext = WhisperContext.createContextFromFile(realModelFilePath);

            long end = System.currentTimeMillis();
            Log.i("Whisper", "model load successful:" + (end - start) + "ms");
            isLoad = true;
        });
    }


    public CompletableFuture<String> transcribeSample(AudioRecord audioRecord) {
        if(whisperContext==null){
            Log.w("Whisper","please wait for model loading");
            return null;
        }
        if(whisperContext.isRunning()) {
            Log.w("Whisper","please wait for model finish running");
            return null;
        }
        return CompletableFuture.supplyAsync(() -> {
            if (!isLoad) return null;

            int max_size = audioSampleRate * audioMaxSec;
            short[] buff = new short[max_size];
            int real_size = audioRecord.read(buff, 0, max_size);
            if (real_size > 0) {
                float[] buff_float = new float[real_size];
                for (int i = 0; i < real_size; i++) {
                    buff_float[i] = Math.max(-1f, Math.min(1f, buff[i] / 32767.0f));
                }
                //start = System.currentTimeMillis();
                try {
                    return whisperContext.transcribeData(buff_float);
                } catch (ExecutionException | InterruptedException e) {
                    return null;
                }
//                end = System.currentTimeMillis();
//                if (transcription != null) {
//                    Log.i("Whisper", transcription.toString());
//                    msg = "Transcript successful:" + (end - start) + "ms";
//                    Log.i("Whisper", msg);
//                } else {
//                    msg = "Transcript failed:" + (end - start) + "ms";
//                    Log.i("Whisper", msg);
//                }
            } else {
                Log.i("Whisper", "Error reading buffer, code=" + String.valueOf(real_size));
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
