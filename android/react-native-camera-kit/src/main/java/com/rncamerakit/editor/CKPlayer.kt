package com.rncamerakit.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleObserver
import com.aliyun.svideo.base.Constants
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.editor.util.AlivcResUtil
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.editor.util.FixedToastUtils
import com.aliyun.svideosdk.common.AliyunErrorCode
import com.aliyun.svideosdk.common.AliyunIClipConstructor
import com.aliyun.svideosdk.common.struct.common.*
import com.aliyun.svideosdk.common.struct.effect.EffectBean
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory
import com.aliyun.svideosdk.importer.AliyunIImport
import com.aliyun.svideosdk.importer.impl.AliyunImportCreator
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.R
import com.rncamerakit.RNEventEmitter
import com.rncamerakit.editor.manager.CKPlayCallBack
import com.rncamerakit.editor.manager.ComposeManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.*

@SuppressLint("ViewConstructor")
class CKPlayer(val reactContext: ThemedReactContext) :
    FrameLayout(reactContext.applicationContext),
    LifecycleObserver,
    CKPlayCallBack.Callbacks {

    private val mContext: Context = reactContext.applicationContext

    private var mWidth = 0
    private var mHeight = 0

    private var mAliyunIImport: AliyunIImport? = null

    /**
     * 编辑接口，包含特效使用，视频预览
     */
    private var mAliyunIEditor: AliyunIEditor? = null

    /**
     * 获取编辑的流片段管理，可以实现对流片段的修改，删除，添加，获取操作
     */
    private var mAliyunIClipConstructor: AliyunIClipConstructor? = null
    private var mDisposableObserver: DisposableObserver<String>? = null

    private var mVideoContainer: FrameLayout? = null
    private var mSurfaceView: SurfaceView? = null
    private var mPasterView: FrameLayout? = null


    private val mColorFilterList: MutableList<String> = ArrayList()


    private var mProjectConfigure: String? = null
    private var mComposeManager: ComposeManager? = null


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

    private fun initPasterView() {
        mPasterView = FrameLayout(mContext)
        val layoutParams = LayoutParams(mWidth, mHeight)
        mVideoContainer!!.addView(mPasterView, layoutParams)
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
                mContext, mContext.resources.getString(R.string.alivc_editor_edit_tip_init_failed)
            )
            return
        }
        mAliyunIClipConstructor = mAliyunIEditor?.sourcePartManager
    }


    private fun copyAssets() {
        mDisposableObserver = object : DisposableObserver<String>() {
            override fun onNext(s: String) {
                initColorFilter()
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

    private fun initColorFilter() {
        mColorFilterList.clear()
        mColorFilterList.addAll(EditorCommon.getColorFilterList(mContext))
    }

    private fun getVideoParam(): AliyunVideoParam {
        return AliyunVideoParam.Builder()
            .bitrate(10 * 1000)
            .frameRate(30)
            .gop(30)
            .crf(23)
            .scaleRate(1.0f)
            .outputWidth(720)
            .outputHeight(1280)
            .videoQuality(VideoQuality.SSD)
            .scaleMode(VideoDisplayMode.FILL)
            .videoCodec(VideoCodecs.H264_HARDWARE)
            .build()
    }


    private var colorFilterPosition = 0

    /**
     * 设置颜色滤镜
     */
    fun setColorFilter(position: Int?) {
        if (mColorFilterList.isEmpty() || colorFilterPosition >= mColorFilterList.size) {
            colorFilterPosition = 0
            return
        }
        val source = Source(mColorFilterList[colorFilterPosition])
        val effect = EffectBean()
        effect.id = colorFilterPosition
        effect.source = source
        if (source.path != null && source.path.contains(File.separator)) {
            val name = source.path.substring(source.path.lastIndexOf(File.separator) + 1)
            source.url = AlivcResUtil.getAppResUri(AlivcResUtil.TYPE_FILTER, name)
        }
        effect.path = source.path
        mAliyunIEditor?.applyFilter(effect)
        colorFilterPosition++
        if (colorFilterPosition > mColorFilterList.size) {
            colorFilterPosition = 0
        }
    }

    /**
     * 导入视频
     */
    fun importVideo(filePath: String?) {
        val filePath =
            Constants.SDCardConstants.getDir(mContext.applicationContext) + File.separator + "paiya-record.mp4"
        val aliyunCrop = AliyunCropCreator.createCropInstance(mContext)
        val duration = aliyunCrop.getVideoDuration(filePath)

        Log.e("AAA", "duration：$duration")
        mAliyunIImport?.setVideoParam(getVideoParam())
        mAliyunIImport?.addMediaClip(
            AliyunVideoClip.Builder()
                .source(filePath)
                .startTime(0)
                .endTime(duration / 1000)
                .build()
        )
        mProjectConfigure = mAliyunIImport?.generateProjectConfigure()
        mAliyunIImport?.release()
        if (mProjectConfigure != null) {
            initEditor(Uri.fromFile(File(mProjectConfigure)), true)
            mAliyunIEditor?.play()
        }
    }


    /**
     * 导入图片
     */
    fun importImage(filePath: String?) {
        mAliyunIImport?.setVideoParam(getVideoParam())
        mAliyunIImport?.addMediaClip(
            AliyunImageClip.Builder()
                .source(filePath)
                .duration(1)
                .build()
        )
        mProjectConfigure = mAliyunIImport?.generateProjectConfigure()
        mAliyunIImport?.release()
        if (mProjectConfigure != null) {
            initEditor(Uri.fromFile(File(mProjectConfigure)), false)
            mAliyunIEditor?.play()
        }
    }

    fun onRelease() {
        mAliyunIEditor?.onDestroy()
        mDisposableObserver?.dispose()

        mComposeManager?.onRelease()
    }


    init {
        mWidth = ScreenUtils.getWidth(mContext)
        mHeight = mWidth * 16 / 9
        initVideoContainer()
        initSurfaceView()
        initPasterView()
        mAliyunIImport = AliyunImportCreator.getImportInstance(mContext)
        mComposeManager = ComposeManager(reactContext)
        copyAssets()
    }

    override fun onPlayProgress(currentPlayTime: Long, currentStreamPlayTime: Long) {
        Log.e("AAA", "onPlayProgress：$currentPlayTime")
        RNEventEmitter.startVideoPlay(reactContext, currentPlayTime)
    }

    override fun onEnd(state: Int?, isVideo: Boolean) {
        reactContext.runOnUiQueueThread {
            if (isVideo) {
                mAliyunIEditor!!.replay()
                setColorFilter(0)
            }
        }
    }


}