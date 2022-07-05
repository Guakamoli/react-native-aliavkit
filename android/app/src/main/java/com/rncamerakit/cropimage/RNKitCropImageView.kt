package com.rncamerakit.cropimage

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.setPadding
import com.aliyun.svideo.common.utils.FileUtils
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.rncamerakit.R
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.UCropView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class RNKitCropImageView(val reactContext: ThemedReactContext) : FrameLayout(reactContext.applicationContext) {

    private val mContext: Context = reactContext.applicationContext

    private var mImageUri: String? = null
    private var mImageAngle: Float = 0F

    private var mEventEmitter: RCTEventEmitter? = null

    private var mUCropView: UCropView? = null
    private var mGestureCropImageView: GestureCropImageView? = null
    private var mOverlayView: OverlayView? = null

    enum class EventEmitterKeys(private val mName: String) {
        EVENT_EMITTER_CROPPED("onCropped"),
        EVENT_EMITTER_CROP_ERROR("onCropError");

        override fun toString(): String {
            return mName
        }
    }

    init {
        mEventEmitter = reactContext.getJSModule(RCTEventEmitter::class.java)
        reactContext.runOnUiQueueThread {
            initViews()
        }
    }

    private fun getSaveImageOutputFile(): File {
        val outputFileName = "save_cache_img_" + System.currentTimeMillis() + ".jpg"
        val outPathDirs: String = FileUtils.getDiskCachePath(context) + File.separator + "media/image" + File.separator
        //创建目录
        FileUtils.createDir(outPathDirs)
        return FileUtils.createFile(outPathDirs, outputFileName)
    }

    private fun getCropImageOutputUri(): Uri {
        val outPathDirs = FileUtils.getDiskCachePath(mContext) + File.separator + "media/image" + File.separator
        val fileName = "crop_head_image_" + System.currentTimeMillis() + ".jpg"
        val outputPath = FileUtils.createFile(outPathDirs, fileName).path
        return Uri.parse("file://$outputPath")
    }

    private fun initViews() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_crop_image, this, true)
        mUCropView = view.findViewById(R.id.mUCropView)
        mGestureCropImageView = mUCropView?.cropImageView
        mOverlayView = mUCropView?.overlayView
        initGestureCropImageView()
        initOverlayView()
    }

    private fun initGestureCropImageView() {
        mGestureCropImageView?.setPadding(0)
        mGestureCropImageView?.isScaleEnabled = true
        mGestureCropImageView?.isRotateEnabled = true
        mGestureCropImageView?.isGestureEnabled = true
        var imageUri = Uri.parse(mImageUri)
        try {
            //兼容WEBP的图片
            val optionsType = BitmapFactory.Options()
            optionsType.inJustDecodeBounds = true
            val inputStream = mContext.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, optionsType)
            optionsType.inSampleSize = 1
            optionsType.inJustDecodeBounds = false
            val fileType = optionsType.outMimeType
            if (!fileType.contains("png") && !fileType.contains("jpg") && !fileType.contains("jpeg")) {
                val saveInputStream = mContext.contentResolver.openInputStream(imageUri)
                val outBitmap = BitmapFactory.decodeStream(saveInputStream)
                val saveFilePath: String? = saveImageToJPEG(mContext, outBitmap)
                if (!TextUtils.isEmpty(saveFilePath)) {
                    mImageUri = "file://$saveFilePath"
                    imageUri = Uri.parse("file://$saveFilePath")
                }
                saveInputStream?.close()
            }
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace();
        }
        mGestureCropImageView?.setImageUri(imageUri, getCropImageOutputUri())
    }

    private fun saveImageToJPEG(context: Context, bitmap: Bitmap?): String? {
        val outputFile = getSaveImageOutputFile()
        try {
            val fileOutStream = FileOutputStream(outputFile)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream) //把位图输出到指定的文件中
            fileOutStream.flush()
            fileOutStream.close()
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            return outputFile.path
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun initOverlayView() {
        mOverlayView?.setPadding(0)
        mOverlayView?.freestyleCropMode = OverlayView.FREESTYLE_CROP_MODE_DISABLE
        //裁剪区域横宽比例 1.77 for 16:9
        mOverlayView?.setTargetAspectRatio(1F)
        //区域外阴影颜色
        mOverlayView?.setDimmedColor(Color.argb(128, 0, 0, 0))
        //显示圆
        mOverlayView?.setCircleDimmedLayer(true)
        //裁剪区域矩形边框
        mOverlayView?.setShowCropFrame(false)
        //裁剪区域网格
        mOverlayView?.setShowCropGrid(false)
    }

    /**
     * 裁剪图片地址
     */
    fun setImageUri(imageUri: String?) {
        this.mImageUri = imageUri
    }

    /**
     *  设置图片旋转角度：angle：0；90；180；270
     */
    fun setAngle(angle: Float) {
        val currentAngle = mGestureCropImageView?.currentAngle
        if (currentAngle != null) {
            mGestureCropImageView?.postRotate(-currentAngle)
            mGestureCropImageView?.postRotate(angle)
        } else {
            mGestureCropImageView?.postRotate(90F)
        }
        this.mImageAngle = angle
    }

    /**
     * 开始裁剪
     */
    fun setStartCrop() {
        mGestureCropImageView?.cropAndSaveImage(Bitmap.CompressFormat.JPEG, 90, object : BitmapCropCallback {
            override fun onBitmapCropped(resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
                onCropped(resultUri.path, offsetX, offsetY, imageWidth, imageHeight)
            }

            override fun onCropFailure(throwable: Throwable) {
                onCropError(throwable.message)
            }
        })
    }

    /**
     * 裁剪完成,回调到RN
     */
    fun onCropped(imagePath: String?, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int) {
        val map: WritableMap = Arguments.createMap()
        map.putString("path", imagePath)
        map.putString("uri", "file://$imagePath")
        map.putInt("offsetX", offsetX)
        map.putInt("offsetY", offsetY)
        map.putInt("imageWidth", imageWidth)
        map.putInt("imageHeight", imageHeight)
        mEventEmitter?.receiveEvent(id, EventEmitterKeys.EVENT_EMITTER_CROPPED.toString(), map)
    }

    fun onCropError(errorMsg: String?) {
        val map: WritableMap = Arguments.createMap()
        map.putString("msg", errorMsg)
        mEventEmitter?.receiveEvent(id, EventEmitterKeys.EVENT_EMITTER_CROP_ERROR.toString(), map)
    }

    /**
     * 重置图片
     */
    fun setReset() {
        mGestureCropImageView?.imageMatrix = Matrix()
        mGestureCropImageView?.setImageToWrapCropBounds()
    }

    override fun requestLayout() {
        super.requestLayout()
        post {
            measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            );
            layout(left, top, right, bottom);
        }
    }

    /**
     * 组件销毁，资源回收
     */
    fun onDestroy() {
        mGestureCropImageView?.cancelAllAnimations()
    }

}