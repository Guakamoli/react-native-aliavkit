package com.rncamerakit.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleObserver
import com.aliyun.svideo.common.utils.PermissionUtils
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.common.utils.ThreadUtils
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface
import com.aliyun.svideo.recorder.util.RecordCommon
import com.aliyun.svideo.recorder.view.focus.FocusView
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.recorder.manager.RecorderManage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.*

@SuppressLint("ViewConstructor")
class CKCamera(private val reactContext: ThemedReactContext) :
    FrameLayout(reactContext.applicationContext),
    LifecycleObserver {

    private var mFocusView: FocusView? = null
    private var mRecorderSurfaceView: SurfaceView? = null
    private var mVideoContainer: FrameLayout? = null
    var mRecorderManage: RecorderManage? = null
        private set
    private var mRecorder: AlivcIMixRecorderInterface? = null
    private var mDisposableObserver: DisposableObserver<String>? = null
    private var mWidth = 0
    private var mHeight = 0

    private fun initRecorder() {
        mRecorderManage = RecorderManage(reactContext)
        mRecorder = mRecorderManage!!.mRecorder
        mRecorder?.setDisplayView(mRecorderSurfaceView, null)
        mRecorder?.startPreview()
    }

    private fun initVideoContainer() {
        mVideoContainer = FrameLayout(context)
        val params = LayoutParams(mWidth, mHeight)
        params.gravity = Gravity.CENTER_HORIZONTAL
        addView(mVideoContainer, params)
    }

    private var lastScaleFactor = 0f
    private var scaleFactor = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun initRecorderSurfaceView() {
        mRecorderSurfaceView = SurfaceView(context)
        val container = FrameLayout(context)
        val slp = LayoutParams(mWidth, mHeight)
        slp.gravity = Gravity.CENTER
        container.addView(mRecorderSurfaceView, slp)
        val layoutParams = LayoutParams(slp.width, slp.height)
        mVideoContainer!!.addView(container, layoutParams)
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
                    mRecorder!!.setZoom(scaleFactor)
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
                    val x = e.x / mRecorderSurfaceView!!.width
                    val y = e.y / mRecorderSurfaceView!!.height
                    //手动对焦
                    mRecorder!!.setFocus(x, y)
                    mFocusView!!.showView()
                    mFocusView!!.setLocation(e.rawX, e.rawY)
                    return true
                }
            })
        mRecorderSurfaceView!!.setOnTouchListener { _, event ->
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
        mFocusView!!.setPadding(10, 10, 10, 10)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(mFocusView, params)
        mFocusView!!.showViewInVisible()
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
        mDisposableObserver = object : DisposableObserver<String>() {
            override fun onNext(s: String) {
                if (mRecorderManage != null) {
                    mRecorderManage!!.initColorFilterAssets()
                }
                setFaceTrackModePath()
            }

            override fun onError(e: Throwable?) {
            }

            override fun onComplete() {
            }
        }
        Observable.create<String> { emitter ->
            try {
                RecordCommon.copyAll(reactContext)
                emitter.onNext("")
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(mDisposableObserver)
    }

    fun onRelease() {
        if (mFocusView != null) {
            mFocusView!!.activityStop()
        }
        if (mRecorder != null) {
            mRecorder!!.release()
            mRecorder = null
        }
        if (mRecorderManage != null) {
            mRecorderManage!!.onRelease()
        }
        if (mDisposableObserver != null) {
            mDisposableObserver!!.dispose()
        }
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
        ActivityCompat.requestPermissions(
            Objects.requireNonNull(reactContext.currentActivity)!!,
            permissions,
            0
        )
    }

    init {
        onRelease()
        if (!isPermissions()) {
            getPermissions()
        }
        mWidth = ScreenUtils.getWidth(context)
        mHeight = mWidth * 16 / 9
        initVideoContainer()
        initRecorderSurfaceView()
        initRecorder()
        initFocusView()
        copyAssets()
    }
}
