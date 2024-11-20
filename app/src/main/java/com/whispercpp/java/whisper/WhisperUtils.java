package com.whispercpp.java.whisper;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class WhisperUtils {
    private static final String LOG_TAG = "LibWhisper";


    public static boolean isArmEabiV7a() {
        return Build.SUPPORTED_ABIS[0].equals("armeabi-v7a");
    }

    public static boolean isArmEabiV8a() {
        return Build.SUPPORTED_ABIS[0].equals("arm64-v8a");
    }


    public static String cpuInfo() {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Path path = new File("/proc/cpuinfo").toPath();
                return new String(Files.readAllBytes(path));
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "Couldn't read /proc/cpuinfo", e);
            return null;
        }

    }
}