package com.rncamerakit.editor

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.aliyun.svideo.editor.util.FixedToastUtils
import com.aliyun.svideosdk.common.AliyunErrorCode
import com.aliyun.svideosdk.editor.EditorCallBack
import com.rncamerakit.R

class CKEditorCallBack(private val mContext: Context, private val mCallbacks: Callbacks?,private var isVideo:Boolean) :
    EditorCallBack() {

    init {
//        mNeedRenderCallback = RENDER_CALLBACK_TEXTURE
    }


    interface Callbacks {
        fun onPlayProgress(currentPlayTime: Long, currentStreamPlayTime: Long)
        fun onEnd(state: Int?,isVideo:Boolean)
    }

    override fun onEnd(state: Int) {
        mCallbacks?.onEnd(state,isVideo)
    }

    override fun onError(errorCode: Int) {
        Handler(Looper.getMainLooper()).post {
            when (errorCode) {
                AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_WRONG_STATE, AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_PROCESS_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_NO_FREE_DISK_SPACE, AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CREATE_DECODE_GOP_TASK_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_AUDIO_STREAM_DECODER_INIT_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_VIDEO_STREAM_DECODER_INIT_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_SPS_PPS_NULL, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_H264_PARAM_SET_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_HEVC_PARAM_SET_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_DECODER_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_STATE, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INPUT, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_NO_BUFFER_AVAILABLE, AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_DECODE_SPS, AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_CREATE_DECODER_FAILED, AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_STATE, AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_INPUT, AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_NO_BUFFER_AVAILABLE -> {
                    FixedToastUtils.show(
                        mContext,
                        errorCode.toString() + ""
                    )
//                    (getContext() as Activity).finish()
                }
                AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CACHE_DATA_SIZE_OVERFLOW -> {
                    FixedToastUtils.show(
                        mContext,
                        errorCode.toString() + ""
                    )
//                    mThumbLineBar.restart()
//                    mAliyunIEditor.play()
                }
                AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO -> {
                    FixedToastUtils.show(
                        mContext,
                        mContext.resources
                            .getString(R.string.alivc_editor_error_tip_not_supported_audio)
                    )
//                    (getContext() as Activity).finish()
                }
                AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO -> {
                    FixedToastUtils.show(
                        mContext,
                        mContext.resources
                            .getString(R.string.alivc_editor_error_tip_not_supported_video)
                    )
//                    (getContext() as Activity).finish()
                }
                AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_STREAM_NOT_EXISTS, AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_PIXEL_FORMAT -> {
                    FixedToastUtils.show(
                        mContext,
                        mContext.resources
                            .getString(R.string.alivc_editor_error_tip_not_supported_pixel_format)
                    )
//                    (getContext() as Activity).finish()
                }
                AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INTERRUPT -> {
                    FixedToastUtils.show(
                        mContext,
                        mContext.resources
                            .getString(R.string.alivc_editor_edit_tip_decoder_error_interrupt)
                    )
//                    (getContext() as Activity).finish()
                }
                else -> {
                    FixedToastUtils.show(
                        mContext,
                        mContext.resources
                            .getString(R.string.alivc_editor_error_tip_play_video_error)
                    )
//                    (getContext() as Activity).finish()
                }
            }
        }

    }

    override fun onCustomRender(srcTextureID: Int, width: Int, height: Int): Int {
        return srcTextureID
    }

    override fun onTextureRender(srcTextureID: Int, width: Int, height: Int): Int {
        return 0
    }

    override fun onPlayProgress(currentPlayTime: Long, currentStreamPlayTime: Long) {
        mCallbacks?.onPlayProgress(currentPlayTime,currentStreamPlayTime)
    }

    override fun onDataReady() {
    }


}