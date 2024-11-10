package com.memo.minimemo.transcribe;

import android.app.Application;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;
import com.memo.minimemo.entity.WhisperSegment;

import com.whispercpp.java.whisper.WhisperContext;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

public enum LocalWhisper {
  INSTANCE;

  public static final String modelFilePath = "models/ggml-tiny-q8_0.bin";
  private WhisperContext whisperContext;

  LocalWhisper() {}

  public void release(){
      try {
        if(whisperContext != null) {
          whisperContext.release();
        }
      } catch (ExecutionException e) {
          throw new RuntimeException(e);
      } catch (InterruptedException e) {
          throw new RuntimeException(e);
      }
  }

  public synchronized String transcribeData(float[] data) throws ExecutionException, InterruptedException {
    if(whisperContext==null){
      Log.w("Whisper","please wait for model loading");
        return null;
    }else{
      return whisperContext.transcribeData(data);
    }
  }

  public List<WhisperSegment> transcribeDataWithTime(float[] audioData) throws ExecutionException, InterruptedException {
    if(whisperContext==null){
      Log.w("Whisper","please wait for model loading");
      return null;
    }else{
      return whisperContext.transcribeDataWithTime(audioData);
    }
  }

  public void init(Application context) {
    //noting to do.but init
    File filesDir = context.getFilesDir();
    File modelFile = AssetUtils.copyFileIfNotExists(context, filesDir, modelFilePath);
    String realModelFilePath = modelFile.getAbsolutePath();
    whisperContext = WhisperContext.createContextFromFile(realModelFilePath);
  }


}
