package com.aliyun.svideo.common.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * @author cross_ly DATE 2019/01/24
 * <p>描述:
 */
public class FileUtils {

    /**
     * 获取sdcard剩余内存
     *
     * @return 单位b
     */
    public static long getSdcardAvailableSize() {

        File directory = Environment.getExternalStorageDirectory();

        StatFs statFs = new StatFs(directory.getPath());
        //获取可供程序使用的Block数量
        long blockAvailable = statFs.getAvailableBlocks();
        //获得Sdcard上每个block的size
        long blockSize = statFs.getBlockSize();

        return blockAvailable * blockSize;
    }

    /**
     * 获取sdcard总内存大小
     *
     * @return 单位b
     */
    public static long getSdcardTotalSize() {

        File directory = Environment.getExternalStorageDirectory();

        StatFs statFs = new StatFs(directory.getPath());
        //获得sdcard上 block的总数
        long blockCount = statFs.getBlockCount();
        //获得sdcard上每个block 的大小
        long blockSize = statFs.getBlockSize();

        return blockCount * blockSize;
    }

    public static File getApplicationSdcardPath(Context context) {
        File var1 = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (var1 == null) {
            var1 = context.getFilesDir();
        }

        return var1;
    }

    public static boolean deleteFD(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        } else {
            File var1 = new File(path);
            File var2 = new File(var1.getAbsolutePath() + System.currentTimeMillis());
            var1.renameTo(var2);
            return deleteFD(var2);
        }
    }

    public static boolean deleteFD(File fd) {
        if (!fd.exists()) {
            return false;
        } else {
            return fd.isDirectory() ? deleteDirectory(fd) : fd.delete();
        }
    }

    public static boolean deleteDirectory(File dir) {
        clearDirectory(dir);
        return dir.delete();
    }

    public static void clearDirectory(File dir) {
        File[] var1 = dir.listFiles();
        if (var1 != null) {
            File[] var2 = var1;
            int var3 = var1.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                File var5 = var2[var4];
                if (var5.isDirectory()) {
                    deleteDirectory(var5);
                } else {
                    var5.delete();
                }
            }

        }
    }


    /**
     * 创建文件夹
     */
    public static File createDir(String sdcardDir) {
        File file = new File(sdcardDir);
        if (!file.exists()) {
            boolean isMkdirs = file.mkdirs();
            if (isMkdirs) {
                return file;
            } else {
                return null;
            }
        }
        return file;
    }

    /**
     * 创建文件
     */
    public static File createFile(String sdcardDirName, String fileName) {
        File destDir = new File(sdcardDirName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File file = new File(sdcardDirName + File.separator + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    //flie：要删除的文件夹的所在位置
    public static void deleteFileDirectory(File file) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFileDirectory(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }


    /**
     * 判断文件是否存在
     */
    public static boolean fileIsExists(String strFile) {
        if (TextUtils.isEmpty(strFile)) {
            return false;
        }
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param path
     */
    public static void deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        deleteFile(new File(path));
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists() && file.isFile()) {
            boolean isDelete = file.delete();
            if (isDelete) {
                Log.d("FileUtil", "deleted " + file.toString());
            }
        }
    }


    /**
     * 删除文件
     *
     * @param path
     */
    public static void deleteFileOnExit(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        deleteFileOnExit(new File(path));
    }

    public static void deleteFileOnExit(File file) {
        if (file != null && file.exists() && file.isFile()) {
            file.deleteOnExit();
        }
    }


    /**
     * 获取APP沙盒路径
     *
     * @param context
     * @return /storage/emulated/0/Android/data/packageName/files
     */
    public static String getFilesPath(Context context) {
        String filePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            //外部存储可用
            File exFile = context.getExternalFilesDir(null);
            if (exFile != null) {
                return exFile.getPath();
            }
            return context.getFilesDir().getPath();
        } else {
            //外部存储不可用
            return context.getFilesDir().getPath();
        }
    }


    /**
     * 获取 APP 的 cache 路径
     *
     * @param context
     * @return /storage/emulated/0/Android/data/包名/cache
     */
    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            // /storage/emulated/0/Android/data/packageName/cache   不会在空间少时被自动清除
            File exFile = context.getExternalCacheDir();
            if (exFile != null) {
                return exFile.getPath();
            }
            return context.getCacheDir().getPath();
        } else {
            // /data/data/<应用包名>/cache   用来存储临时数据。因此在系统空间较少时有可能会被自动清除。
            return context.getCacheDir().getPath();
        }
    }
}
