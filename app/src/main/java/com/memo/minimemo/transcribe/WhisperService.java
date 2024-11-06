package com.memo.minimemo.transcribe;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.memo.minimemo.entity.WhisperSegment;
import com.memo.minimemo.transcribe.LocalWhisper;
import com.memo.minimemo.transcribe.WaveEncoder;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WhisperService {
  private final Object lock = new Object();

  public void loadModel(Application context) {
    String modelFilePath = LocalWhisper.modelFilePath;
    String msg = "load model from :" + modelFilePath + "\n";
    Log.i("Whisper",msg);

    long start = System.currentTimeMillis();
    LocalWhisper.INSTANCE.init(context);
    long end = System.currentTimeMillis();
    msg = "model load successful:" + (end - start) + "ms";
    Log.i("Whisper",msg);
  }


  public void transcribeSample(File sampleFile) {
    String msg = "";
    msg = "transcribe file from :" + sampleFile.getAbsolutePath();
    Log.i("Whisper",msg);

    Long start = System.currentTimeMillis();
    float[] audioData = new float[0];  // 读取音频样本
    try {
      audioData = WaveEncoder.decodeWaveFile(sampleFile);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    long end = System.currentTimeMillis();
    msg = "decode wave file:" + (end - start) + "ms";
    Log.i("Whisper",msg);

    start = System.currentTimeMillis();
    List<WhisperSegment> transcription = null;
    try {
      //transcription = LocalWhisper.INSTANCE.transcribeData(audioData);
      transcription = LocalWhisper.INSTANCE.transcribeDataWithTime(audioData);
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    end = System.currentTimeMillis();
    if(transcription!=null){
      Log.i("Whisper",transcription.toString());
      msg = "Transcript successful:" + (end - start) + "ms";
      Log.i("Whisper",msg);

      Log.i("Whisper",transcription.toString());

    }else{
      msg = "Transcript failed:" + (end - start) + "ms";
      Log.i("Whisper",msg);
    }

  }


  public void release() {
    //noting to do
  }
}
