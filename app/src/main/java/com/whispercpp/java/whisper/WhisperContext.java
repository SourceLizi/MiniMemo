package com.whispercpp.java.whisper;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.memo.minimemo.entity.WhisperSegment;

public class WhisperContext {

    private static final String LOG_TAG = "LibWhisper";
    private long ptr;
    private final ExecutorService executorService;

    private boolean isRunning = false;

    private WhisperContext(long ptr) {
        this.ptr = ptr;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String transcribeData(float[] data, WhisperLib.callback_fn cb)
            throws ExecutionException, InterruptedException {
        return executorService.submit(new Callable<String>() {

            @Override
            public String call(){
                if (ptr == 0L) {
                    throw new IllegalStateException();
                }
                int numThreads = Runtime.getRuntime().availableProcessors();
                Log.d(LOG_TAG, "Selecting " + numThreads + " threads");

                StringBuilder result = new StringBuilder();
                isRunning = true;
                synchronized (this) {
                    WhisperLib.fullTranscribe(ptr, numThreads, data, cb);
                    int textCount = WhisperLib.getTextSegmentCount(ptr);
                    for (int i = 0; i < textCount; i++) {
                        String sentence = WhisperLib.getTextSegment(ptr, i);
                        result.append(sentence);
                    }
                }
                isRunning = false;
                return result.toString();
            }
        }).get();
    }

    public List<WhisperSegment> transcribeDataWithTime(float[] data, WhisperLib.callback_fn cb)
            throws ExecutionException, InterruptedException {
        return executorService.submit(new Callable<List<WhisperSegment>>() {

            @Override
            public List<WhisperSegment> call() {
                if (ptr == 0L) {
                    throw new IllegalStateException();
                }
                int numThreads = Runtime.getRuntime().availableProcessors();
                Log.d(LOG_TAG, "Selecting " + numThreads + " threads");

                List<WhisperSegment> segments = new ArrayList<>();
                synchronized (this) {
//          StringBuilder result = new StringBuilder();
                    WhisperLib.fullTranscribe(ptr, numThreads, data, cb);
                    int textCount = WhisperLib.getTextSegmentCount(ptr);
                    for (int i = 0; i < textCount; i++) {
                        long start = WhisperLib.getTextSegmentT0(ptr, i);
                        String sentence = WhisperLib.getTextSegment(ptr, i);
                        long end = WhisperLib.getTextSegmentT1(ptr, i);
//            result.append();
                        segments.add(new WhisperSegment(start, end, sentence));

                    }
//          return result.toString();
                }
                return segments;
            }
        }).get();
    }

    public void stopTranscribe() {
        WhisperLib.setAbort();
    }


    public String benchMemory(int nthreads) throws ExecutionException, InterruptedException {
        return executorService.submit(() -> WhisperLib.benchMemcpy(nthreads)).get();
    }


    public String benchGgmlMulMat(int nthreads) throws ExecutionException, InterruptedException {
        return executorService.submit(() -> WhisperLib.benchGgmlMulMat(nthreads)).get();
    }


    public void release() throws ExecutionException, InterruptedException {
        executorService.submit(() -> {
            if (ptr != 0L) {
                WhisperLib.freeContext(ptr);
                ptr = 0;
            }
        }).get();
    }


    public static WhisperContext createContextFromFile(String filePath) {
        long ptr = WhisperLib.initContext(filePath);
        if (ptr == 0L) {
            throw new RuntimeException("Couldn't create context with path " + filePath);
        }
        return new WhisperContext(ptr);
    }


    public static WhisperContext createContextFromInputStream(InputStream stream) {
        long ptr = WhisperLib.initContextFromInputStream(stream);
        if (ptr == 0L) {
            throw new RuntimeException("Couldn't create context from input stream");
        }
        return new WhisperContext(ptr);
    }


    public static WhisperContext createContextFromAsset(AssetManager assetManager, String assetPath) {
        long ptr = WhisperLib.initContextFromAsset(assetManager, assetPath);
        if (ptr == 0L) {
            throw new RuntimeException("Couldn't create context from asset " + assetPath);
        }
        return new WhisperContext(ptr);
    }


    public static String getSystemInfo() {
        return WhisperLib.getSystemInfo();
    }
}
