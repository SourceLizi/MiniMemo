package com.whispercpp.java.whisper;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class WhisperCpuConfig {
  public static int getPreferredThreadCount() {
    return Math.max(CpuInfo.getHighPerfCpuCount(), 2);
  }
}
