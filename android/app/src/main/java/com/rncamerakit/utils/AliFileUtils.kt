package com.rncamerakit.utils

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.aliyun.svideo.common.utils.PermissionUtils
import com.aliyun.svideo.common.utils.ThreadUtils
import com.aliyun.svideo.common.utils.UriUtils
import com.blankj.utilcode.util.FileUtils
import java.io.File
import java.io.FileOutputStream


class AliFileUtils {

    companion object {

        fun saveImageToMediaStore(context: Context, imagePath: String) {
            if (PermissionUtils.checkPermissionsGroup(
                    context, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            ) {
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
            if (PermissionUtils.checkPermissionsGroup(
                    context, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            ) {
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

        fun saveToSandBox(context: Context, fileUri: String?): String {
            if (fileUri == null || "" == fileUri) {
                return ""
            }
            val file: File = if (fileUri.startsWith("content://") || fileUri.startsWith("file://")) {
                com.blankj.utilcode.util.UriUtils.uri2File(Uri.parse(fileUri))
            } else {
                File(fileUri)
            }
            val outputFileName = "img_cache_" + System.nanoTime() + ".jpg"
            val outputPath = com.aliyun.svideo.common.utils.FileUtils.getDiskCachePath(context) +
                    File.separator + "media/image" + File.separator + outputFileName
            val isSave = saveImageToJPEG(file, File(outputPath))
            return if (isSave) {
                outputPath
            } else {
                file.path
            }
        }

        private fun saveImageToJPEG(file: File, outputFile: File): Boolean {
            val documentFile = DocumentFile.fromFile(file)
            if (documentFile.type?.startsWith("image") == false) {
                return false
            }
            //
            val fileParent = outputFile.parentFile
            //创建输出目录
            FileUtils.createOrExistsDir(fileParent)

            //解析原始图片数据
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)

            val fileOutStream = FileOutputStream(outputFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fileOutStream) //把位图输出到指定的文件中
            fileOutStream.flush()
            fileOutStream.close()

            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            return true
        }


    }
}