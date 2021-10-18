package com.rncamerakit.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleObserver
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.editor.util.FixedToastUtils
import com.aliyun.svideo.editor.view.EditorVideHelper
import com.aliyun.svideosdk.common.AliyunErrorCode
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.R
import com.rncamerakit.RNEventEmitter
import com.rncamerakit.editor.manager.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.net.FileNameMap
import java.net.URLConnection

@SuppressLint("ViewConstructor")
class CKEditor(val reactContext: ThemedReactContext) :
    FrameLayout(reactContext.applicationContext),
    LifecycleObserver,
    CKPlayCallBack.Callbacks {

    private val mContext: Context = reactContext.applicationContext

    private var mWidth = 0
    private var mHeight = 0

    private var isCopyAssets = false

    //导出视频时是否保存到相册
    private var isSaveToPhotoLibrary = false

    //是否是视频 true:视频文件 ；false: 图片文件
    private var isVideo = false

    /**
     * 编辑接口，包含特效使用，视频预览
     */
    private var mAliyunIEditor: AliyunIEditor? = null

    private var mDisposableObserver: DisposableObserver<String>? = null

    private var mVideoContainer: FrameLayout? = null
    private var mSurfaceView: SurfaceView? = null

    private lateinit var mProjectConfigure: String

    //视频导入
    private var mImportManager: ImportManager? = null

    //滤镜管理
    private var mColorFilterManager: ColorFilterManager? = null

    //视频合成
    private var mComposeManager: ComposeManager? = null

    init {
        mWidth = ScreenUtils.getWidth(mContext)
        mHeight = mWidth * 16 / 9
        initVideoContainer()
        initSurfaceView()
        mImportManager = ImportManager(reactContext)
        mColorFilterManager = ColorFilterManager(reactContext)
        mComposeManager = ComposeManager(reactContext)
        copyAssets()
    }

    private fun initVideoContainer() {
        mVideoContainer = FrameLayout(mContext)
        val params = LayoutParams(mWidth, mHeight)
        params.gravity = Gravity.CENTER_HORIZONTAL
        addView(mVideoContainer, params)
    }

    private fun initSurfaceView() {
        mSurfaceView = SurfaceView(mContext)
        val layoutParams = LayoutParams(mWidth, mHeight)
        mVideoContainer!!.addView(mSurfaceView, layoutParams)
    }

    private fun copyAssets() {
        mDisposableObserver = object : DisposableObserver<String>() {
            override fun onNext(s: String) {
                isCopyAssets = true
            }

            override fun onError(e: Throwable?) {
            }

            override fun onComplete() {
            }
        }
        Observable.create<String> { emitter ->
            try {
                EditorCommon.copyAll(mContext, View(mContext))
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

    private fun initEditor(uri: Uri, isVideo: Boolean) {
        //设置onTextureRender能够回调
        mAliyunIEditor =
            AliyunEditorFactory.creatAliyunEditor(uri, CKPlayCallBack(mContext, this, isVideo))
        val ret = mAliyunIEditor?.init(mSurfaceView, mContext.applicationContext)
        mAliyunIEditor?.setDisplayMode(VideoDisplayMode.FILL)
        mAliyunIEditor?.setVolume(50)
        mAliyunIEditor?.setFillBackgroundColor(Color.BLACK)
        if (ret != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            FixedToastUtils.show(
                mContext,
                mContext.resources.getString(R.string.alivc_editor_edit_tip_init_failed) + ", $ret"
            )
            return
        }
        mAliyunIEditor?.play()
    }

    override fun onPlayProgress(currentPlayTime: Long, currentStreamPlayTime: Long) {
        RNEventEmitter.startVideoEditor(reactContext, currentPlayTime)
    }

    override fun onEnd(state: Int?, isVideo: Boolean) {
        reactContext.runOnUiQueueThread {
            if (isVideo) {
                mAliyunIEditor!!.replay()
            }
        }
    }

    /**
     * 导入视频 \ 导入图片
     */
    fun importVideo(filePath: String?, isVideo: Boolean) {
        this.isVideo = isVideo
        mProjectConfigure = mImportManager?.importVideo(filePath).toString()
        initEditor(Uri.fromFile(File(mProjectConfigure)), isVideo)
    }


    /**
     * 导出视频时是否同时保存到相册
     */
    fun isSaveToPhotoLibrary(isSave: Boolean?) {
        if (isSave != null) {
            this.isSaveToPhotoLibrary = isSave
        }
    }

    /**
     * 设置静音
     */
    fun setVideoMute(isSilence: Boolean?) {
        if (isSilence == true) {
            mAliyunIEditor?.setVolume(0)
        } else {
            mAliyunIEditor?.setVolume(50)
        }
    }

    /**
     * 获取滤镜列表
     */
    fun getColorFilterList(promise: Promise) {
        if (!isCopyAssets) {
            promise.reject("getColorFilterList", "ColorFilter is empty")
            return
        }
        mColorFilterManager?.getColorFilter(promise)
    }

    /**
     * 设置滤镜
     */
    fun setColorFilter(filterName: String?) {
        mColorFilterManager?.setColorFilter(filterName, mAliyunIEditor)
    }

    /**
     * 视频裁剪
     */
    fun videoTrim(readableMap: ReadableMap, promise: Promise) {
        val duration = mAliyunIEditor?.streamDuration
        val startTime = if (readableMap.hasKey("startTime")) readableMap.getInt("startTime") else 0
        val endTime = if (readableMap.hasKey("endTime")) readableMap.getInt("endTime") else duration
        val isApply =
            if (readableMap.hasKey("isApply")) readableMap.getBoolean("isApply") else false
        if (endTime != null) {
            EditorVideHelper.resetVideoTimes(
                mAliyunIEditor,
                startTime.toLong(),
                endTime.toLong(),
                isApply
            )
            promise.resolve(true)
        }
    }

    /**
     * 获取视频封面
     */
    fun videoCover(seekTime: Int, promise: Promise) {
        VideoCoverManager.getVideoCover(mContext, mProjectConfigure, seekTime.toLong(), promise)
    }

    /**
     * 导出视频 \ 导出图片
     */
    fun exportVideo(promise: Promise?) {
        if (mAliyunIEditor?.isPlaying == true) {
            mAliyunIEditor?.stop()
        }
        mAliyunIEditor?.stop()
        mAliyunIEditor?.saveEffectToLocal()
        mAliyunIEditor?.applySourceChange()
        mComposeManager?.startCompose(
            mProjectConfigure,
            promise,
            this.isVideo,
            isSaveToPhotoLibrary
        )
    }

    /**
     * 暂停播放
     */
    fun pause(promise: Promise) {
        mAliyunIEditor?.pause()
        promise.resolve(true)
    }

    /**
     * 停止播放
     */
    fun stop(promise: Promise) {
        mAliyunIEditor?.stop()
        promise.resolve(true)
    }

    /**
     * 重新播放
     */
    fun play(promise: Promise) {
        replay()
        promise.resolve(true)
    }

    private fun replay() {
        if (!mAliyunIEditor?.isPlaying!!) {
            if (mAliyunIEditor?.isPaused == true) {
                mAliyunIEditor?.resume()
            } else {
                mAliyunIEditor?.play()
            }
        }
    }

    /**
     *
     */
    fun seek(seekTime: Int, promise: Promise) {
        // time 时间，单位：微秒
        mAliyunIEditor?.seek(seekTime.toLong() * 1000)
    }

    /**
     *
     */
    fun onRelease() {
        mAliyunIEditor?.onDestroy()
        mDisposableObserver?.dispose()
        mComposeManager?.onRelease()
    }

}