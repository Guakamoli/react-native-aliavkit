package com.rncamerakit.photos

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.media.MediaInfo
import com.duanqu.transcode.NativeParser
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.rncamerakit.R
import java.io.File


class RNAliKitPhotoView(val reactContext: ThemedReactContext) : FrameLayout(reactContext.applicationContext) {

    private val mContext: Context = reactContext.applicationContext
    private var mWidth = 0
    private var mHeight = 0
    private var mNumColumns = 4
    private var mMultiSelect: Boolean = false
    private var mPhotoAdapter: PhotoAdapter? = null
    private var mPhotoRecyclerView: RecyclerView? = null
    private var mPhotoList: MutableList<MediaInfo> = ArrayList()

    private var mEventEmitter: RCTEventEmitter? = null

    enum class EventEmitterKeys(private val mName: String) {
        EVENT_SELECTED_PHOTOS("onSelectedPhotos");

        override fun toString(): String {
            return mName
        }
    }

    init {
        mEventEmitter = reactContext.getJSModule(RCTEventEmitter::class.java)
        this.mWidth = ScreenUtils.getWidth(reactContext)
        this.mHeight = ScreenUtils.getHeight(reactContext)
        mPhotoList.clear()

        reactContext.runOnUiQueueThread {
            initViews()
            initMedias()
        }

    }

    fun setPageSize(pageSize: Int) {
    }

    fun setNumColumns(numColumns: Int) {
        this.mNumColumns = numColumns
    }

    fun setMultiSelect(multiSelect: Boolean) {
        this.mMultiSelect = multiSelect
    }

    fun setItemWidth(itemWidth: Int) {
    }

    fun setItemHeight(itemHeight: Int) {
    }

    private fun initViews() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_ali_kit_photos, this, true)
        mPhotoRecyclerView = view.findViewById<RecyclerView>(R.id.photosRecyclerView)
        filAdapter()

        mPhotoAdapter?.setOnPhotoItemListener(object : PhotoAdapter.OnPhotoItemListener() {
            override fun onItemClick(position: Int, info: MediaInfo) {
                super.onItemClick(position, info)
                if (mMultiSelect) {
                    val photoList: MutableList<MediaInfo> = ArrayList()
                    photoList.add(info)
                    sendRNSelectedPhotos(0, photoList)
                }
            }
        })
    }


    private fun filAdapter() {
        if (mPhotoAdapter == null) {
            mPhotoAdapter = PhotoAdapter(context, mPhotoList, this.mWidth/mNumColumns, this.mWidth/mNumColumns)
            mPhotoRecyclerView?.layoutManager = GridLayoutManager(context, mNumColumns)
            mPhotoRecyclerView?.addItemDecoration(GridSpacingItemDecoration(mNumColumns, 2, false))
            mPhotoRecyclerView?.itemAnimator = DefaultItemAnimator()
            mPhotoRecyclerView?.adapter = mPhotoAdapter
        } else {
            mPhotoAdapter?.notifyDataSetChanged()
        }
    }

    private fun initMedias() {
        QueryMediaManager.instance.initLoad(mContext)
        QueryMediaManager.instance.setOnQueryMediaListener(object : QueryMediaManager.OnQueryMediaListener() {
            override fun onDataUpdate(photoList: MutableList<MediaInfo>, baseCount: Int) {
                super.onDataUpdate(photoList, baseCount)
                updatePhotoList(photoList, baseCount)
            }

            override fun onCompletion() {
                super.onCompletion()
                if (mPhotoList.isNotEmpty()) {
                    val photoList: MutableList<MediaInfo> = ArrayList()
                    photoList.add(mPhotoList[0])
                    sendRNSelectedPhotos(0, photoList)
                }
            }
        })
    }


    private fun updatePhotoList(photoList: MutableList<MediaInfo>, baseCount: Int) {
        mPhotoList.addAll(photoList)
        val size: Int = photoList.size
        val insert = baseCount - size
        mPhotoAdapter?.notifyItemRangeInserted(insert, size)
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


    fun sendRNSelectedPhotos(selectPosition: Int, photoList: MutableList<MediaInfo>) {
        val arrayList = WritableNativeArray()

        for (i in photoList.indices) {
            val info = photoList[i]
            val filePath = info.filePath

            val videoFile = File(filePath)
            val nativeParser = NativeParser()
            nativeParser.init(filePath)
            val rotation = nativeParser.getValue(NativeParser.VIDEO_ROTATION).toInt()
            val videoWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toDouble()
            val videoHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toDouble()

            val map: WritableMap = Arguments.createMap()
            map.putInt("index", i)
            map.putInt("width", videoWidth.toInt())
            map.putInt("height", videoHeight.toInt())
            map.putString("url", info.fileUri)
            map.putDouble("fileSize", videoFile.length().toDouble())
            map.putString("filename", videoFile.name)
            map.putString("type", info.mimeType)
            map.putInt("playableDuration", info.duration)
            map.putInt("rotation", rotation)
            nativeParser.release()
            arrayList.pushMap(map)
        }

        val map: WritableMap = Arguments.createMap()
        map.putInt("selectPosition", selectPosition)
        map.putArray("data", arrayList)
        mEventEmitter?.receiveEvent(id, EventEmitterKeys.EVENT_SELECTED_PHOTOS.toString(), map)
    }

    fun onDestroy() {
        QueryMediaManager.instance.onDestroy()
        mPhotoAdapter?.onDestroy()
    }

}