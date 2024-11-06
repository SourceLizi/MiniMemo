package com.memo.minimemo.transcribe;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetUtils {

  public static File copyFileIfNotExists(Context context, File distDir, String filename) {
    File dstFile = new File(distDir, filename);
    if (dstFile.exists()) {
      return dstFile;
    } else {
      File parentFile = dstFile.getParentFile();
      if (!parentFile.exists()) {
        parentFile.mkdirs();
      }
      AssetUtils.copyFileFromAssets(context, filename, dstFile);
    }
    return dstFile;
  }

  public static void copyDirectoryFromAssets(Context appCtx, String srcDir, String dstDir) {
    if (srcDir.isEmpty() || dstDir.isEmpty()) {
      return;
    }
    try {
      if (!new File(dstDir).exists()) {
        new File(dstDir).mkdirs();
      }
      for (String fileName : appCtx.getAssets().list(srcDir)) {
        String srcSubPath = srcDir + File.separator + fileName;
        String dstSubPath = dstDir + File.separator + fileName;
        if (new File(srcSubPath).isDirectory()) {
          copyDirectoryFromAssets(appCtx, srcSubPath, dstSubPath);
        } else {
          copyFileFromAssets(appCtx, srcSubPath, dstSubPath);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void copyFileFromAssets(Context appCtx, String srcPath, String dstPath) {
    File dstFile = new File(dstPath);
    copyFileFromAssets(appCtx, srcPath, dstFile);
  }

  public static void copyFileFromAssets(Context appCtx, String srcPath, File dstFile) {
    if (srcPath.isEmpty()) {
      return;
    }
    InputStream is = null;
    OutputStream os = null;
    try {
      is = new BufferedInputStream(appCtx.getAssets().open(srcPath));

      os = new BufferedOutputStream(new FileOutputStream(dstFile));
      byte[] buffer = new byte[1024];
      int length = 0;
      while ((length = is.read(buffer)) != -1) {
        os.write(buffer, 0, length);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        os.close();
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}