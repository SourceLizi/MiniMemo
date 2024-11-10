package com.memo.minimemo.transcribe;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.memo.minimemo.entity.WhisperSegment;
import com.memo.minimemo.transcribe.LocalWhisper;
import com.memo.minimemo.transcribe.WaveEncoder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WhisperService {
  private boolean isLoad = false;
  private final ExecutorService executor;

  public WhisperService(Application context) {
    this.executor = Executors.newFixedThreadPool(2);
    loadModel(context);
  }

  public void loadModel(Application context) {
    this.executor.submit(() -> {
      if(isLoad) return;
      String modelFilePath = LocalWhisper.modelFilePath;
      Log.i("Whisper", "load model from :" + modelFilePath + "\n");

      long start = System.currentTimeMillis();
      LocalWhisper.INSTANCE.init(context);
      long end = System.currentTimeMillis();
      Log.i("Whisper", "model load successful:" + (end - start) + "ms");
      isLoad = true;
    });
  }


  public CompletableFuture<String> transcribeSample(File sampleFile) {
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
      if (!isLoad) return null;

      float[] audioData = new float[0];  // 读取音频样本
      try {
        audioData = WaveEncoder.decodeWaveFile(sampleFile);
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
//      start = System.currentTimeMillis();
      String transcription = null;
      try {
        transcription = LocalWhisper.INSTANCE.transcribeData(audioData);
        //transcription = LocalWhisper.INSTANCE.transcribeDataWithTime(audioData);
        return transcription;
      } catch (ExecutionException | InterruptedException e) {
        e.printStackTrace();
      }

//      end = System.currentTimeMillis();
//      if (transcription != null) {
//        Log.i("Whisper", transcription.toString());
//        msg = "Transcript successful:" + (end - start) + "ms";
//        Log.i("Whisper", msg);
//      } else {
//        msg = "Transcript failed:" + (end - start) + "ms";
//        Log.i("Whisper", msg);
//      }
      return null;
    },this.executor);
    return  future;
  }

  public void stopAll(){
    executor.shutdownNow();
  }


  @Override
  protected void finalize() throws Throwable {
    executor.shutdownNow();
    LocalWhisper.INSTANCE.release();
    super.finalize();
  }
}
