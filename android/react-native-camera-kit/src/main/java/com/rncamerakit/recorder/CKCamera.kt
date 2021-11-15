package com.rncamerakit.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.aliyun.svideo.common.utils.*
import com.aliyun.svideo.downloader.DownloaderManager
import com.aliyun.svideo.recorder.mixrecorder.AlivcRecorder
import com.aliyun.svideo.recorder.util.RecordCommon
import com.aliyun.svideo.recorder.view.focus.FocusView
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.BaseEventListener
import com.rncamerakit.R
import com.rncamerakit.recorder.manager.EffectPasterManage
import com.rncamerakit.recorder.manager.MediaPlayerManage
import com.rncamerakit.recorder.manager.RecorderManage
import com.rncamerakit.utils.DownloadUtils
import org.jetbrains.anko.dip
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

    private val mContext = reactContext.applicationContext
    private var mFocusView: FocusView? = null
    private var mRecorderSurfaceView: SurfaceView? = null
    private var mVideoContainer: FrameLayout? = null
    private var testLayout: TextView? = null
    var mRecorderManage: RecorderManage? = null
        private set
    private var mRecorder: AlivcRecorder? = null
    private var mWidth = 0
    private var mHeight = 0

    private fun initRecorder() {
        mRecorderManage = RecorderManage(reactContext)
        mRecorder = mRecorderManage?.mRecorder
        mRecorder?.setDisplayView(mRecorderSurfaceView, null)
        mRecorder?.startPreview()

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
        mVideoContainer?.addView(mRecorderSurfaceView,params)
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
                if (mRecorderManage != null) {
                    mRecorderManage?.initColorFilterAssets()
                }
                setFaceTrackModePath()
                EffectPasterManage.instance.init(reactContext)
            }
        }
    }

    fun onRelease() {
        mFocusView?.activityStop()
        mRecorder?.release()
        mRecorder = null
        mRecorderManage?.onRelease()
        MediaPlayerManage.instance.release()
    }

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun isPermissions(): Boolean {
        return PermissionUtils.checkPermissionsGroup(reactContext, permissions)
    }

    fun getPermissions() {
        Objects.requireNonNull(reactContext.currentActivity)?.let {
            ActivityCompat.requestPermissions(
                it,
                permissions,
                0
            )
        }
    }


    init {
        if (!isPermissions()) {
            getPermissions()
        }
        DownloaderManager.getInstance().init(reactContext.applicationContext)

        copyAssets()

        DownloadUtils.getMusicJsonInfo()
        initLifecycle()

//        EffectPasterManage.instance.getPasterInfos(null)
    }

    private fun initLifecycle() {
        BaseEventListener(reactContext, object : BaseEventListener.LifecycleEventListener() {
            override fun onHostResume() {
                super.onHostResume()
                Log.e("AAA", "onHostResume()")
            }

            override fun onHostPause() {
                super.onHostPause()
                Log.e("AAA", "onHostPause()")
            }

            override fun onHostDestroy() {
                super.onHostDestroy()
                Log.e("AAA", "onHostDestroy()")
                onRelease()
            }

            override fun onWindowFocusChange(hasFocus: Boolean) {
                super.onWindowFocusChange(hasFocus)
                Log.e("AAA", "onWindowFocusChange(hasFocus)：$hasFocus")
            }
        })
    }

    /**
     * 设置宽高（dp）
     */
    fun setLayout(width: Int, height: Int) {
        this.mWidth = dip(width)
        this.mHeight = dip(height)
        initVideoContainer()
        initRecorderSurfaceView()
        initRecorder()
        initFocusView()
    }

}
