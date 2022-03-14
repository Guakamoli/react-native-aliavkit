package com.rncamerakit.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import com.aliyun.svideo.common.utils.PermissionUtils
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.common.utils.ThreadUtils
import com.aliyun.svideo.recorder.mixrecorder.AlivcRecorder
import com.aliyun.svideo.recorder.util.RecordCommon
import com.aliyun.svideo.recorder.view.control.RecordState
import com.aliyun.svideo.recorder.view.focus.FocusView
import com.facebook.react.ReactActivity
import com.facebook.react.bridge.Promise
import com.facebook.react.modules.core.PermissionListener
import com.facebook.react.uimanager.ThemedReactContext
import com.manwei.libs.dialog.OnDialogListener
import com.manwei.libs.utils.permission.PermissionsDialog
import com.manwei.libs.utils.permission.RxPermissionUtils
import com.rncamerakit.BaseEventListener
import com.rncamerakit.editor.manager.ColorFilterManager
import com.rncamerakit.recorder.manager.MediaPlayerManage
import com.rncamerakit.recorder.manager.RecorderManage
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.*

@SuppressLint("ViewConstructor")
class CKCamera(
    val reactContext: ThemedReactContext,
) :
    FrameLayout(reactContext.applicationContext),
    LifecycleObserver {

    //录制状态，开始、暂停、准备,只是针对UI变化
    private val mRecordState = RecordState.STOP

    private var isCopyAssets = false
    private val mContext = reactContext.applicationContext
    private var mFocusView: FocusView? = null
    private var mRecorderSurfaceView: SurfaceView? = null
    private var mVideoContainer: FrameLayout? = null

    var mRecorderManage: RecorderManage? = null
        private set
    private var mRecorder: AlivcRecorder? = null
    private var mWidth = 0
    private var mHeight = 0


    private var isInit = false

    companion object {
        var permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    init {

//        if (!isPermissions()) {
//            getPermissions()
//        } else {
        this.mWidth = ScreenUtils.getWidth(reactContext)
        this.mHeight = mWidth*16/9
        initCamera()
//        }
        initLifecycle()
        //延时器
//        Timer().schedule(object : TimerTask() {
//            override fun run() {
//                doAsync {
//                    uiThread {
//
//                    }
//                }
//            }
//        }, 1000L)
    }

    private fun initRecorder() {
        mRecorderManage = RecorderManage(reactContext)
        mRecorder = mRecorderManage?.mRecorder
        mRecorder?.setDisplayView(mRecorderSurfaceView, null)
        mRecorder?.startPreview()
        setFaceTrackModePath()
        copyAssets()
    }

    private fun initVideoContainer() {
        mVideoContainer = FrameLayout(mContext)
        val params = LayoutParams(mWidth, mHeight)
        params.gravity = Gravity.CENTER_HORIZONTAL
        addView(mVideoContainer, params)
    }

    private var lastScaleFactor = 0f
    private var scaleFactor = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun initRecorderSurfaceView() {
        mRecorderSurfaceView = SurfaceView(mContext)
        val params = LayoutParams(mWidth, mHeight)
        mVideoContainer?.addView(mRecorderSurfaceView, params)
        val scaleGestureDetector = ScaleGestureDetector(context, object : OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val factorOffset = detector.scaleFactor - lastScaleFactor
                scaleFactor += factorOffset
                lastScaleFactor = detector.scaleFactor
                if (scaleFactor < 0) {
                    scaleFactor = 0f
                }
                if (scaleFactor > 1) {
                    scaleFactor = 1f
                }
                if (mRecorder != null) {
                    //设置缩放
                    mRecorder?.setZoom(scaleFactor)
                }
                return false
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                lastScaleFactor = detector.scaleFactor
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {}
        })
        val gestureDetector = GestureDetector(
            context,
            object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (mRecorder == null) {
                        return true
                    }
                    val width = mRecorderSurfaceView?.width
                    val height = mRecorderSurfaceView?.height
                    var pointX = 0F
                    if (width != null) {
                        pointX = e.x/width.toFloat()
                    }
                    var pointY = 0F
                    if (height != null) {
                        pointY = e.y/height.toFloat()
                    }
                    //手动对焦
                    mRecorder?.setFocus(pointX, pointY)
                    mFocusView?.showView()
                    mFocusView?.setLocation(e.rawX, e.rawY)
                    return true
                }
            })
        mRecorderSurfaceView?.setOnTouchListener { _, event ->
            if (event.pointerCount >= 2) {
                scaleGestureDetector.onTouchEvent(event)
            } else if (event.pointerCount == 1) {
                gestureDetector.onTouchEvent(event)
            }
            true
        }
    }

    private fun initFocusView() {
        mFocusView = FocusView(context)
        mFocusView?.setPadding(10, 10, 10, 10)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(mFocusView, params)
        mFocusView?.showViewInVisible()
    }


    private fun setFaceTrackModePath() {
        ThreadUtils.runOnSubThread {
            val path = context.getExternalFilesDir("")
                .toString() + File.separator + RecordCommon.QU_NAME + File.separator
            mRecorder?.needFaceTrackInternal(true)
            mRecorder?.setFaceTrackInternalModelPath("$path/model")
        }
    }

    private fun copyAssets() {
        doAsync {
            RecordCommon.copyAll(reactContext)
            uiThread {
                isCopyAssets = true
                mColorFilterListPromise?.let { getRecordColorFilter(it) }
                if (mRecorderManage != null) {
                    mRecorderManage?.initColorFilterAssets()
                }
            }
        }
    }

    fun onRelease() {
        Log.e("AAA", "CKCamera onRelease")
        mFocusView?.activityStop()
        mRecorder?.release()
        mRecorder = null
        mRecorderManage?.onRelease()
        removeAllViews()
        MediaPlayerManage.instance.release()
    }


    fun isPermissions(): Boolean {
        return PermissionUtils.checkPermissionsGroup(reactContext, permissions)
    }

    fun getPermissions() {
        val reactActivity: FragmentActivity = reactContext.currentActivity as ReactActivity
        if (reactActivity is ReactActivity) {
            reactActivity.requestPermissions(
                permissions, 200, PermissionListener { requestCode, permissions, grantResults ->
                    // 0 全部同意，1 有拒绝，2 有拒绝并且不再同意
                    var isAllGranted: Int = 0
                    permissions?.let {
                        for (i in it.indices) {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                Log.e("AAA", "同意:" + permissions[i])
                            } else {

//                                break
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
                    if (isAllGranted == 0) {
                        this.mWidth = ScreenUtils.getWidth(reactContext)
                        this.mHeight = mWidth*16/9
                        initLifecycle()
                        initCamera()
                    } else if (isAllGranted == 2) {
                        PermissionsDialog.showDialogTitle(reactActivity, "“拍鸭”需要获取您的相机和麦克风权限,是否去设置？", object : OnDialogListener {
                            override fun onRightClick() {
                                super.onRightClick()
                                RxPermissionUtils.getAppDetailSettingIntent(reactActivity.applicationContext)
                            }
                        })

                    }
                    false
                }
            )
        } else {
            Objects.requireNonNull(reactContext.currentActivity)?.let {
                ActivityCompat.requestPermissions(
                    it,
                    permissions,
                    200
                )
            }
        }
    }


    private fun initLifecycle() {
        BaseEventListener(reactContext, object : BaseEventListener.LifecycleEventListener() {
            override fun onHostResume() {
                super.onHostResume()
//                Log.e("AAA", "onHostResume()")
                resumeCamera()
            }

            override fun onHostPause() {
                super.onHostPause()
//                Log.e("AAA", "onHostPause()")
                pauseCamera()
            }

            override fun onHostDestroy() {
                super.onHostDestroy()
//                Log.e("AAA", "onHostDestroy()")
                onRelease()
            }

            override fun onWindowFocusChange(hasFocus: Boolean) {
                super.onWindowFocusChange(hasFocus)
//                Log.e("AAA", "onWindowFocusChange(hasFocus)：$hasFocus")
            }
        })
    }

    /**
     * 设置滤镜
     */
    fun setColorFilter(filterPath: String?) {
        mRecorderManage?.setColorFilter(filterPath)
    }

    /**
     * 设置宽高（dp）
     */
    fun setLayout(width: Int, height: Int) {
        this.mWidth = ScreenUtils.getWidth(mContext)
        this.mHeight = this.mWidth*height/width

        if (this.isInit) {
            var params = mVideoContainer?.layoutParams
            if (params == null) {
                params = LayoutParams(mWidth, mHeight)
            } else {
                params.width = mWidth
                params.height = mHeight
            }
            mVideoContainer?.layoutParams = params
            mRecorderSurfaceView?.layoutParams = params
        }

    }

    private var mColorFilterListPromise: Promise? = null
    //获取滤镜列表
    fun getRecordColorFilter(promise: Promise) {
        //如果还没解压完成，需要解压完后返回值
        if (!isCopyAssets) {
            mColorFilterListPromise = promise
            return
        }
        ColorFilterManager.getRecordColorFilter(reactContext.applicationContext, promise)
    }

    private fun initCamera() {
        if (this.isInit) {
            return
        }
        initVideoContainer()
        initRecorderSurfaceView()
        initRecorder()
        initFocusView()
        this.isInit = true;
    }

    override fun requestLayout() {
        super.requestLayout()
        post {
            measure(
                MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY)
            );
            layout(left, top, right, bottom);
        }
    }

    fun resumeCamera() {
        mRecorderManage?.resumeCamera()
    }


    fun pauseCamera() {
        mRecorderManage?.pauseCamera()
    }

}
