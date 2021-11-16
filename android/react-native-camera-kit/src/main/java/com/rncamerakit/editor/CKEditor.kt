package com.rncamerakit.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleObserver
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.editor.util.FixedToastUtils
import com.aliyun.svideo.editor.view.EditorVideHelper
import com.aliyun.svideosdk.common.AliyunErrorCode
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.common.struct.effect.EffectBean
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.editor.AliyunIEditor
import com.aliyun.svideosdk.editor.EffectType
import com.aliyun.svideosdk.editor.OnPasterRestored
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ThemedReactContext
import com.liulishuo.filedownloader.BaseDownloadTask
import com.rncamerakit.BaseEventListener
import com.rncamerakit.R
import com.rncamerakit.RNEventEmitter
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.editor.manager.*
import com.rncamerakit.utils.DownloadUtils
import com.rncamerakit.utils.MyFileDownloadCallback
import kotlinx.coroutines.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File

@DelicateCoroutinesApi
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

    var isInit = false

    /**
     * 编辑接口，包含特效使用，视频预览
     */
    private var mAliyunIEditor: AliyunIEditor? = null

    private var mVideoContainer: FrameLayout? = null
    private var mSurfaceView: SurfaceView? = null

    private lateinit var mProjectConfigure: String

    //视频导入
    private var mImportManager: ImportManager? = null

    //滤镜管理
    private var mColorFilterManager: ColorFilterManager? = null

    //字幕管理
    private var mCaptionManager: CaptionManager? = null

    //视频合成
    private var mComposeManager: ComposeManager? = null


    private fun initVideoContainer() {
        mVideoContainer = FrameLayout(mContext)
        val params = LayoutParams(mWidth, mHeight)
//        val params = LayoutParams(ScreenUtils.getWidth(context), ScreenUtils.getHeight(context))
        params.gravity = Gravity.CENTER_HORIZONTAL
        mVideoContainer?.setBackgroundColor(Color.BLUE)
        addView(mVideoContainer, params)
    }

    private fun initSurfaceView() {
        mSurfaceView = SurfaceView(mContext)
        val params = LayoutParams(mWidth, mHeight)
        mVideoContainer?.addView(mSurfaceView,params)
    }

    private fun copyAssets() {
        doAsync {
            EditorCommon.copyAll(mContext, View(mContext))
            uiThread {
                isCopyAssets = true
            }
        }
    }

    private fun initEditor(uri: Uri, isVideo: Boolean) {
        //设置onTextureRender能够回调
        mAliyunIEditor =
            AliyunEditorFactory.creatAliyunEditor(uri, CKPlayCallBack(mContext, this, isVideo))
//        mWidth = ScreenUtils.getWidth(mContext)
//        mHeight = try {
//            mWidth*mAliyunIEditor!!.videoHeight/mAliyunIEditor!!.videoWidth
//        } catch (e: Exception) {
//            e.printStackTrace()
//            mWidth*16/9
//        }
        initVideoContainer()
        initSurfaceView()

        //该代码块中的操作必须在AliyunIEditor.init之前调用，否则会出现动图、动效滤镜的UI恢复回调不执行，开发者将无法恢复动图、动效滤镜UI
        val pasterManager = mAliyunIEditor?.createPasterManager()
        pasterManager?.setDisplaySize(mWidth, mHeight)
        pasterManager?.setOnPasterRestoreListener(OnPasterRestored {

        })
        mCaptionManager = CaptionManager(reactContext, pasterManager)
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

        initControllerManager()
    }

    override fun onPlayProgress(currentPlayTime: Long, currentStreamPlayTime: Long) {
        RNEventEmitter.startVideoEditor(reactContext, currentPlayTime,currentStreamPlayTime)
    }

    override fun onEnd(state: Int?, isVideo: Boolean) {
        reactContext.runOnUiQueueThread {
//            if (isVideo) {
            mAliyunIEditor?.replay()
//            }
        }
    }

    /**
     * 导入视频 \ 导入图片
     */
    fun importVideo(filePath: String?, isVideo: Boolean) {
        if(this.isInit){
            return
        }
        this.isVideo = isVideo
        mProjectConfigure = if (isVideo) {
            mImportManager?.importVideo(filePath).toString()
        } else {
            mImportManager?.importImage(filePath).toString()
        }
        initEditor(Uri.fromFile(File(mProjectConfigure)), isVideo)

        this.isInit = true;

//        //TODO 测试音频
//        val list = MusicFileInfoDao.instance.queryAll()
//        val bean = list?.get(0)
//        setMusicInfo(bean)
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
        ColorFilterManager.getColorFilter(mContext,promise)
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

//        mCaptionManager?.addDefaultStyleCaption("添加视频的测试字幕，要长一点，\n再长一点，这下差不多够了吧！", mAliyunIEditor, mWidth, mHeight)

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
    fun pause(promise: Promise?) {
        mAliyunIEditor?.pause()
        promise?.resolve(true)
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
        if (mAliyunIEditor?.isPlaying == false) {
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
        mAliyunIEditor?.seek(seekTime.toLong()*1000)
        replay()
    }

    private var lastMusicBean: EffectBean? = null

    /**
     * 背景音乐
     */
    fun setBackgroundMusic(bgmPath: String?) {
        //重制mv和混音的音效
        mAliyunIEditor?.resetEffect(EffectType.EFFECT_TYPE_MIX)
        mAliyunIEditor?.resetEffect(EffectType.EFFECT_TYPE_MV_AUDIO)

        if (TextUtils.isEmpty(bgmPath) && lastMusicBean != null) {
            //清空背景音乐
            mAliyunIEditor?.removeMusic(lastMusicBean)
            mAliyunIEditor?.setVolume(50)
            lastMusicBean = null
            return
        }

        if (lastMusicBean != null) {
            mAliyunIEditor?.removeMusic(lastMusicBean)
        }

        val musicEffect = EffectBean()
        musicEffect.id = 1
        musicEffect.source = Source(bgmPath)

        //切换音乐seek到0清音乐缓存，避免响一声
        musicEffect.startTime = 0*1000 //单位是us所以要x1000
        musicEffect.streamStartTime = 0*1000

        //设置为最大时长
        musicEffect.duration = mAliyunIEditor?.duration ?: Int.MAX_VALUE.toLong()
        musicEffect.streamDuration = mAliyunIEditor?.duration ?: Int.MAX_VALUE.toLong()

        musicEffect.weight = 50

        mAliyunIEditor?.applyMusic(musicEffect)

        mAliyunIEditor?.setVolume(50)
        mAliyunIEditor?.seek(0)
        // 重新播放
        replay()

        lastMusicBean = musicEffect
    }

    fun setMusicInfo(bean: MusicFileBean?) {
        if (bean?.isDbContain == 1 && FileUtils.fileIsExists((bean.localPath))) {
            setBackgroundMusic(bean.localPath)
        } else {
            if (bean != null) {
                DownloadUtils.downloadMusic(
                    reactContext,
                    bean.songID,
                    bean.url,
                    null,
                    object : MyFileDownloadCallback() {
                        override fun completed(task: BaseDownloadTask) {
                            super.completed(task)
                            val filePath = task.targetFilePath
                            setBackgroundMusic(filePath)
                        }
                    })
            }
        }
    }


    private fun initControllerManager() {

    }

    init {
//        mWidth = ScreenUtils.getWidth(mContext)
//        mHeight = mWidth*16/9
//        initVideoContainer()
//        initSurfaceView()

        mImportManager = ImportManager(reactContext)
        mColorFilterManager = ColorFilterManager(reactContext)
        mComposeManager = ComposeManager(reactContext)
        copyAssets()
        initLifecycle()
        DownloadUtils.getMusicJsonInfo()

    }


    private fun initLifecycle() {
        BaseEventListener(reactContext, object : BaseEventListener.LifecycleEventListener() {
            override fun onHostResume() {
                super.onHostResume()
                Log.e("AAA", "onHostResume()")
                replay()
            }

            override fun onHostPause() {
                super.onHostPause()
                Log.e("AAA", "onHostPause()")
                pause(null)
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
//        var params = mVideoContainer?.layoutParams
//        if (params == null) {
//            params = LayoutParams(dip(width), dip(height))
//        } else {
//            params.width = dip(width)
//            params.height = dip(height)
//        }
//        mVideoContainer?.layoutParams = params
//        mSurfaceView?.layoutParams = params
    }

    /**
     *
     */
    fun onRelease() {
        mAliyunIEditor?.stop()
        mAliyunIEditor?.onDestroy()
        mComposeManager?.onRelease()
    }

}