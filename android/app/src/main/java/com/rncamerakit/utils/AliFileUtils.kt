package com.rncamerakit.utils

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.aliyun.svideo.common.utils.PermissionUtils
import com.aliyun.svideo.common.utils.ThreadUtils
import com.aliyun.svideo.common.utils.UriUtils
import com.blankj.utilcode.util.FileUtils
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class AliFileUtils {

    companion object {

        fun saveImageToMediaStore(context: Context, imagePath: String, promise: Promise?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                saveToAlbumR(context, File(imagePath), promise)
                return
            }
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
                promise?.resolve(imagePath)
            }
        }


        fun saveVideoToMediaStore(context: Context, videoPath: String, promise: Promise?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                saveToAlbumR(context, File(videoPath), promise)
                return
            }

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
                promise?.resolve(videoPath)
            }
        }

        /**
         * 保存图片到沙盒
         */
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


        /**
         * 保存到系统相册
         */
        private fun saveToAlbumR(context: Context, saveFile: File, promise: Promise?) {
            if (!checkStoragePermissions(context)) {
                promise?.reject("E_UNABLE_TO_SAVE", "No Permissions")
                return
            }
            val environment = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val saveDir = File(environment, "Revo")
            com.aliyun.svideo.common.utils.FileUtils.createDir(saveDir.path);
            val fileName: String = saveFile.name
            doAsync {
                val input = FileInputStream(saveFile).channel
                val outputFile = File(saveDir, fileName)
                val output = FileOutputStream(outputFile).channel
                output.transferFrom(input, 0, input.size())
                input.close()
                output.close()
                uiThread {
                    MediaScannerConnection.scanFile(
                        context.applicationContext,
                        arrayOf(outputFile.absolutePath),
                        null
                    ) { path, uri ->
                        if (uri != null) {
                            val promiseMap = Arguments.createMap()
                            promiseMap.putString("uri", uri.toString())
                            promiseMap.putString("path", path)
                            promise?.resolve(promiseMap)
                        } else {
                            promise?.reject("E_UNABLE_TO_SAVE", "Could not add image to gallery")
                        }
                    }
                }
            }
        }


        /**
         * 检测是否有存储权限
         */
        private fun checkStoragePermissions(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                PermissionUtils.checkPermissionsGroup(
                    context, arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }


    }
}