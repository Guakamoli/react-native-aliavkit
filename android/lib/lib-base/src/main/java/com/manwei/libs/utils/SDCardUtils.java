package com.manwei.libs.utils;

import android.os.Environment;

//import com.manwei.libs.app.AppConfig;

import java.io.File;
import java.util.Objects;

/**
 * @author : wuyq
 * Time : 2020/9/19 0:27
 * Description : SD卡相关
 */
public final class SDCardUtils {

//    /**
//     * 获取 APP 的 files 路径
//     *
//     * @return /storage/emulated/0/Android/data/packageName/files
//     */
//    public static String getSDCardFilesPath() {
//        String filePath;
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
//            //外部存储可用
//            filePath = AppConfig.getContext().getExternalFilesDir(null).getPath();
//        } else {
//            //外部存储不可用
//            filePath = AppConfig.getContext().getFilesDir().getPath();
//        }
//        return filePath;
//    }


//    /**
//     * 获取 APP 的 cache 路径
//     *
//     * @return /storage/emulated/0/Android/data/packageName/cache
//     */
//    public static String getSDCardCachePath() {
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
//            // /storage/emulated/0/Android/data/packageName/cache   不会在空间少时被自动清除
//            return Objects.requireNonNull(AppConfig.getContext().getExternalCacheDir()).getPath();
//        } else {
//            // /data/data/<应用包名>/cache   用来存储临时数据。因此在系统空间较少时有可能会被自动清除。
//            return AppConfig.getContext().getCacheDir().getPath();
//        }
//    }

//    public static void deleteAllCache() {
//        File cacheFile = new File(SDCardUtils.getSDCardCachePath());
//        FileUtils.deleteFiles(cacheFile);
//    }

//    /**
//     * 文件复制的缓存目录
//     *
//     * @return
//     */
//    public static String getCopyCreatePath() {
//        File downloadFile = new File(getSDCardCachePath(), "copy/");
//        if (!downloadFile.exists()) {
//            boolean isMkdirs = downloadFile.mkdirs();  //创建文件夹
//            if (isMkdirs) {
//                return downloadFile.toString();
//            }
//        } else {
//            return downloadFile.toString();
//        }
//        return "";
//    }

//    /**
//     * 文件下载目录
//     *
//     * @return
//     */
//    public static String getDownloadCreatePath() {
//        File downloadFile = new File(getSDCardCachePath(), "download/");
//        if (!downloadFile.exists()) {
//            boolean isMkdirs = downloadFile.mkdirs();  //创建文件夹
//            if (isMkdirs) {
//                return downloadFile.toString();
//            }
//        } else {
//            return downloadFile.toString();
//        }
//        return "";
//    }

}
