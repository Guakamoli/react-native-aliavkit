package com.rncamerakit.editor

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.text.TextUtils
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleObserver
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.editor.util.FixedToastUtils
import com.aliyun.svideosdk.common.AliyunErrorCode
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory
import com.facebook.react.bridge.Promise
import com.facebook.react.uimanager.ThemedReactContext
import com.rncamerakit.R
import com.rncamerakit.RNEventEmitter
import com.rncamerakit.editor.manager.CKPlayCallBack
import com.rncamerakit.editor.manager.ColorFilterManager
import com.rncamerakit.editor.manager.ComposeManager
import com.rncamerakit.editor.manager.ImportManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class CKEditor(val reactContext: ThemedReactContext) :
    FrameLayout(reactContext.applicationContext),
    LifecycleObserver,
    CKPlayCallBack.Callbacks {

    private val mContext: Context = reactContext.applicationContext

    private var mWidth = 0
    private var mHeight = 0

    private var isCopyAssets = false

    /**
     * 编辑接口，包含特效使用，视频预览
     */
    private var mAliyunIEditor: AliyunIEditor? = null

    private var mDisposableObserver: DisposableObserver<String>? = null

    private var mVideoContainer: FrameLayout? = null
    private var mSurfaceView: SurfaceView? = null

    private var mProjectConfigure: String? = null

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
                mContext, mContext.resources.getString(R.string.alivc_editor_edit_tip_init_failed)+", $ret"
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
     * 导入图片
     */
    fun importImage(filePath: String?) {
        mProjectConfigure = mImportManager?.importImage(filePath)
        if (mProjectConfigure != null) {
            initEditor(Uri.fromFile(File(mProjectConfigure)), false)
        }
    }

    /**
     * 导入视频
     */
    fun importVideo(filePath: String?) {
        mProjectConfigure = mImportManager?.importVideo(filePath)
        if (mProjectConfigure != null) {
            initEditor(Uri.fromFile(File(mProjectConfigure)), true)

        }
    }

    /**
     * 设置静音
     */
    fun setAudioSilence(isSilence: Boolean?) {
       if(isSilence == true){
           mAliyunIEditor?.setVolume(0)
       }else{
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
        mColorFilterManager?.setColorFilter(filterName,mAliyunIEditor)
    }

    /**
     * 导出视频
     */
    fun exportVideo(promise: Promise) {
        if(mAliyunIEditor?.isPlaying == true){
            mAliyunIEditor?.stop()
        }
        mAliyunIEditor?.applySourceChange()
        mAliyunIEditor?.saveEffectToLocal()
        mComposeManager?.startCompose(mProjectConfigure,promise,true)
    }

    /**
     * 导出图片
     */
    fun exportImage(promise: Promise) {
        if(mAliyunIEditor?.isPlaying == true){
            mAliyunIEditor?.stop()
        }
        mAliyunIEditor?.applySourceChange()
        mAliyunIEditor?.saveEffectToLocal()
        mComposeManager?.startCompose(mProjectConfigure,promise,false)
    }

    /**
     * 释放
     */
    fun onRelease() {
        mAliyunIEditor?.onDestroy()
        mDisposableObserver?.dispose()
        mComposeManager?.onRelease()
    }

}