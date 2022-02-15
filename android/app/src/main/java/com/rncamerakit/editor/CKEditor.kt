package com.rncamerakit.editor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import com.aliyun.svideo.downloader.FileDownloaderModel
import com.aliyun.svideo.editor.util.EditorCommon
import com.aliyun.svideo.editor.util.FixedToastUtils
import com.aliyun.svideo.editor.view.EditorVideHelper
import com.aliyun.svideosdk.common.AliyunErrorCode
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode
import com.aliyun.svideosdk.common.struct.effect.EffectBean
import com.aliyun.svideosdk.common.struct.project.Source
import com.aliyun.svideosdk.editor.*
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ThemedReactContext
import com.liulishuo.filedownloader.BaseDownloadTask
import com.rncamerakit.BaseEventListener
import com.rncamerakit.R
import com.rncamerakit.RNEventEmitter
import com.rncamerakit.db.MusicFileBean
import com.rncamerakit.editor.manager.*
import com.rncamerakit.font.FontManager
import com.rncamerakit.font.IFontCallback
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

//    //贴图底层渲染接口，添加贴图到底层渲染
//    private var mAliyunPasterRender: AliyunPasterRender? = null
//    //字幕管理
//    private var mCaptionManager: CaptionManager? = null

    //贴图、文字控制类
    private var mAliyunPasterManager: AliyunPasterManager? = null
    private var mCaptionManager2: CaptionManager2? = null

    //视频合成
    private var mComposeManager: ComposeManager? = null


    private fun initVideoContainer() {
        mVideoContainer = FrameLayout(mContext)
        val params = LayoutParams(mWidth, mHeight)
        params.gravity = Gravity.CENTER_HORIZONTAL
        addView(mVideoContainer, params)
    }

    private fun initSurfaceView() {
        mSurfaceView = SurfaceView(mContext)
        val params = LayoutParams(mWidth, mHeight)
        mVideoContainer?.addView(mSurfaceView, params)
    }

    private fun copyAssets() {
        doAsync {
            EditorCommon.copyAll(mContext, View(mContext))
            uiThread {
                isCopyAssets = true
                mColorFilterListPromise?.let { getColorFilterList(it) }
            }
        }
    }

    private var mColorFilterListPromise: Promise? = null

    //获取滤镜列表
    fun getColorFilterList(promise: Promise) {
        //如果还没解压完成，需要解压完后返回值
        if (!isCopyAssets) {
            mColorFilterListPromise = promise
            return
        }
        ColorFilterManager.getColorFilter(reactContext.applicationContext, promise)
    }

    private fun initEditor(uri: Uri, isVideo: Boolean) {
        //设置onTextureRender能够回调
        mAliyunIEditor =
            AliyunEditorFactory.creatAliyunEditor(uri, CKPlayCallBack(mContext, this, isVideo))
        initVideoContainer()
        initSurfaceView()

        //该代码块中的操作必须在AliyunIEditor.init之前调用，否则会出现动图、动效滤镜的UI恢复回调不执行，开发者将无法恢复动图、动效滤镜UI
        mAliyunPasterManager = mAliyunIEditor?.createPasterManager()
        mAliyunPasterManager?.setDisplaySize(mWidth, mHeight)
        mAliyunPasterManager?.setOnPasterRestoreListener(OnPasterRestored {

        })
        mCaptionManager2 = CaptionManager2(reactContext, mAliyunPasterManager, mAliyunIEditor)


        val ret = mAliyunIEditor?.init(mSurfaceView, mContext.applicationContext)


//        //新字幕
//        mAliyunPasterRender = mAliyunIEditor?.pasterRender
//        mAliyunPasterRender?.setDisplaySize(mWidth, mHeight)
//        //动图恢复回调
//        mAliyunPasterRender?.setOnPasterResumeAndSave {
//
//        }
//        mCaptionManager = CaptionManager(reactContext, mAliyunPasterRender, mAliyunIEditor)

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
        RNEventEmitter.startVideoEditor(reactContext, currentPlayTime, currentStreamPlayTime)
    }

    override fun onEnd(state: Int?, isVideo: Boolean) {
        reactContext.runOnUiQueueThread {
            mAliyunIEditor?.replay()
        }
    }

    /**
     * 导入视频 \ 导入图片
     */
    fun importVideo(filePath: String?, isVideo: Boolean) {
        if (this.isInit) {
            return
        }
        mImportManager = ImportManager(reactContext, this.mWidth, this.mHeight)
        this.isVideo = isVideo
        mProjectConfigure = if (isVideo) {
            mImportManager?.importVideo(filePath).toString()
        } else {
            mImportManager?.importImage(filePath).toString()
        }
        initEditor(Uri.fromFile(File(mProjectConfigure)), isVideo)
        this.isInit = true;
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
     * 设置滤镜
     */
    fun setColorFilter(filterName: String?) {
        mColorFilterManager?.setColorFilter(filterName, mAliyunIEditor)
    }

    /**
     * 视频裁剪
     */
    fun trimVideo(readableMap: ReadableMap, promise: Promise) {
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
//        mCaptionManager?.addDefaultStyleCaption("添加视频的测试字幕，要长一点，\n再长一点，这下差不多够了吧！")
//        mCaptionManager2?.addDefaultStyleCaption("添加视频的测试字幕，要长一点，\n再长一点，这下差不多够了吧！",mWidth,mHeight)
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


    init {
        this.mWidth = ScreenUtils.getWidth(reactContext)
        this.mHeight = mWidth*16/9
        initVideoContainer()
        initSurfaceView()
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
            mSurfaceView?.layoutParams = params
            mAliyunPasterManager?.setDisplaySize(mWidth, mHeight)
//            mAliyunPasterRender?.setDisplaySize(mWidth, mHeight)
        }
    }

    fun clearCaptionInfo() {
        mCaptionManager2?.removeCaption()
    }

    /**
     * 设置字幕
     */
    fun setCaptionInfo(readableMap: ReadableMap) {
        clearCaptionInfo()
        // 添加 \ 修改字幕
        val text = if (readableMap.hasKey("text")) readableMap.getString("text") else null
        var rotate = if (readableMap.hasKey("rotate")) readableMap.getDouble("rotate") else 0.0

        val scale = if (readableMap.hasKey("scale")) readableMap.getDouble("scale") else 1.0
        val center = if (readableMap.hasKey("center")) readableMap.getMap("center") else null
        var x = 0F
        var y = 0F
        if (center != null && center.toHashMap().size > 0) {
            x = if (center.hasKey("x")) center.getDouble("x").toFloat() else 0F
            y = if (center.hasKey("y")) center.getDouble("y").toFloat() else 0F
        }
        x = (mWidth/2).toFloat() + dip(x).toFloat()
        y = (mHeight/2).toFloat() + dip(y).toFloat()
        if (text != null && text != "" && scale > 0) {
            mCaptionManager2?.addCaption(text, scale.toFloat(), rotate.toFloat(), x, y)
            val source = Source(FontManager.FONT_PATH)
            mCaptionManager2?.setFontPath(source)
            mCaptionManager2?.apply()
            Log.e("AAA", "设置字体：" + source.path)
        }


        //TODO 测试 随机设置一个字体，如果不存在则下载
        val fonts = FontManager.instance.getDownloadFontList()
        val randoms = (0 until fonts?.size!!).random()
        FontManager.instance.setFont(context, fonts?.get(randoms), object : IFontCallback() {
            override fun onFontSource(source: Source) {
                super.onFontSource(source)
                FontManager.FONT_PATH = source.path
//                mCaptionManager2?.setFontPath(source)
//                mCaptionManager2?.apply()
            }
        })
        //TODO 测试 随机设置一个字体，如果不存在则下载
    }


    /**
     * 设置字幕字体
     */
    fun setCaptionFont(fontModel: FileDownloaderModel) {
        FontManager.instance.setFont(context, fontModel, object : IFontCallback() {
            override fun onFontSource(source: Source) {
                super.onFontSource(source)
                FontManager.FONT_PATH = source.path
                mCaptionManager2?.setFontPath(source)
//                Log.e("AAA", "设置字体：" + source.path)
                mCaptionManager2?.apply()
            }
        })
    }

    /**
     *
     */
    fun onRelease() {
        mAliyunIEditor?.stop()
        mAliyunIEditor?.onDestroy()
        mComposeManager?.onRelease()
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

}