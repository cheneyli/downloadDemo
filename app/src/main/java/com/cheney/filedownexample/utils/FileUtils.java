package com.cheney.filedownexample.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.cheney.filedownexample.MainApp;

import java.io.File;

/**
 * Created by cheney on 2017/5/20.
 */

public class FileUtils {
    public static String getFileDir() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File file = MainApp.getInstance().getExternalFilesDir(null);

            if (file != null) {
                String fileDir = file.getAbsolutePath();
                return fileDir;
            }
        }
        String fileDir = MainApp.getInstance().getFilesDir().getAbsolutePath();
        return fileDir;
    }

    public static String getCacheDir(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File file = context.getExternalCacheDir();

            if (file != null) {
                String fileDir = file.getAbsolutePath();
                return fileDir;
            }
        }
        String fileDir = context.getCacheDir().getAbsolutePath();
        return fileDir;
    }

    public static String getCacheDirByType(CacheDir dir, String rootPath) {
        String path = rootPath + dir;

        File file = new File(path);

        if (!file.exists()) {
            file.mkdirs();
        }

        return path;
    }

    public static String getLocalFilePath(String fileUrl, String rootPath) {
        String appendix = ".file";
        return getLocalFilePathBySpecifiedName(Utils.getMD5(fileUrl) + appendix, rootPath);
    }

    public static String getLocalFilePathBySpecifiedName(String fileName, String rootPath) {
        return getCacheDirByType(CacheDir.FILE, rootPath) + fileName;
    }

    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return file.exists();
    }

    public static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            File tmpFile = new File(file.getAbsolutePath() + "_delete");
            file.renameTo(tmpFile);
            tmpFile.delete();
        } else {
            file.delete();
        }
    }

    public static boolean renameFile(File src, File dst) {
        deleteFile(dst);
        return src.renameTo(dst);
    }

    public static boolean renameFile(File src, String dstPath) {
        File dst = new File(dstPath);
        return renameFile(src, dst);
    }
}
