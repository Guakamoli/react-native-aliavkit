package com.rncamerakit.utils

import android.Manifest
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import com.aliyun.svideo.common.utils.PermissionUtils
import com.aliyun.svideo.common.utils.ThreadUtils
import com.aliyun.svideo.common.utils.UriUtils

class AliFileUtils {

    companion object {

        fun saveImageToMediaStore(context: Context, imagePath: String) {
                if(PermissionUtils.checkPermissionsGroup(context, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //适配android Q
                        ThreadUtils.runOnSubThread {
                            UriUtils.saveImgToMediaStore(
                                context.applicationContext,
                                imagePath
                            )
                        }
                    } else {
                        MediaScannerConnection.scanFile(
                            context.applicationContext,
                            arrayOf(imagePath),
                            arrayOf("image/*"),
                            null
                        )
                    }
                }
        }


        fun saveVideoToMediaStore(context: Context, videoPath: String) {
            if(PermissionUtils.checkPermissionsGroup(context, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //适配android Q
                    ThreadUtils.runOnSubThread {
                        UriUtils.saveVideoToMediaStore(
                            context.applicationContext,
                            videoPath
                        )
                    }
                } else {
                    MediaScannerConnection.scanFile(
                        context.applicationContext,
                        arrayOf(videoPath),
                        arrayOf("video/*"),
                        null
                    )
                }
            }
        }

    }
}