package com.rncamerakit.photos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.aliyun.svideo.common.utils.PermissionUtils
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.PermissionListener
import com.manwei.libs.utils.permission.RxPermissionUtils
import com.rncamerakit.recorder.CKCamera
import java.util.*

class RNAliKitPhotoViewModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        var mView: RNAliKitPhotoView? = null
    }

    override fun getName(): String {
        return "RNAliKitPhotoViewModule"
    }

    //取消选中
    @ReactMethod
    fun uncheckPhoto(options: ReadableMap, promise: Promise) {
        if (options.toHashMap().size > 0) {
            if (options.hasKey("index")) {
                val uncheckIndex = options.getInt("index")
                mView?.uncheckPhoto(uncheckIndex)
            }
        }
    }


    //检测存储权限
    @ReactMethod
    fun checkStorage(promise: Promise) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            promise.resolve("granted")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                promise.resolve("denied")
                return
            } else {
                promise.resolve("granted")
                return
            }
        }
        val activity = reactContext.currentActivity as FragmentActivity
        val isPermissions = RxPermissionUtils.getInstance().isPermissions(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (isPermissions) {
            promise.resolve("granted")
        } else {
            promise.resolve("denied")
        }
    }

    //获取存储权限适配 android 11
    @ReactMethod
    fun getStorage(promise: Promise) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            promise.resolve("granted")
            return
        }
        val reactActivity: FragmentActivity = reactContext.currentActivity as ReactActivity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + reactContext.applicationContext.packageName)
                reactActivity.startActivityForResult(intent, 1024)
                promise.resolve("denied")
                return
            } else {
                promise.resolve("granted")
                return
            }
        }
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (reactActivity is ReactActivity) {
            reactActivity.requestPermissions(permissions, 200, object : PermissionListener {
                override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?): Boolean {
                    // 0 全部同意，1 有拒绝，2 有拒绝并且不再同意
                    var isAllGranted: Int = 0
                    if (grantResults != null) {
                        permissions?.let {
                            for (i in it.indices) {
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    Log.e("AAA", "同意:" + permissions[i])
                                } else {
                                    if (PermissionUtils.isNeverAgainPermission(reactActivity, permissions[i])) {
                                        Log.e("AAA", "拒绝且不再提示:" + permissions[i])
                                        isAllGranted = 2
                                        return@let
                                    } else {
                                        Log.e("AAA", "拒绝:" + permissions[i])
                                        isAllGranted = 1
                                        return@let
                                    }
                                }
                            }
                        }
                    }

                    if (isAllGranted == 0) {
                        promise.resolve("granted")
                    } else if (isAllGranted == 1) {
                        promise.resolve("denied")
                    } else if (isAllGranted == 2) {
                        promise.resolve("blocked")
                    }
                    return false
                }
            })
        } else {
            Objects.requireNonNull(reactContext.currentActivity)?.let {
                ActivityCompat.requestPermissions(
                    it,
                    CKCamera.permissions,
                    200
                )
            }
        }
    }


    @ReactMethod
    fun release(promise: Promise) {

    }

}


