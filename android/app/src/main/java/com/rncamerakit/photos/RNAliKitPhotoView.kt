package com.rncamerakit.photos

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliyun.svideo.common.utils.FileUtils
import com.aliyun.svideo.common.utils.ScreenUtils
import com.aliyun.svideo.media.MediaInfo
import com.aliyun.svideo.media.MediaStorage
import com.duanqu.transcode.NativeParser
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.LifecycleEventListener
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
    private var mItemWidth = 0
    private var mItemHeight = 0

    private var mInitViewLoad: Boolean = false
    private var mMultiSelect: Boolean = false
    private var mPhotoAdapter: PhotoAdapter? = null
    private var mPhotoRecyclerView: RecyclerView? = null
    private var mPhotoList: MutableList<MediaInfo> = ArrayList()

    private var mCurrentClickPosition = 0
    private var mDefaultSelectedPosition = 0
    private var mSelectedPhotoList: MutableList<MediaInfo> = ArrayList()

    private var queryMediaManager: QueryMediaManager? = null

    /**
     * key 选中图片在相册的下标，
     * value 选中图片在选中数组中的下标
     */
    private var mSelectedPhotoMap: HashMap<Int, Int> = HashMap()

    private var mEventEmitter: RCTEventEmitter? = null

    enum class EventEmitterKeys(private val mName: String) {
        EVENT_SELECTED_PHOTO_CALLBACK("onSelectedPhotoCallback"),
        EVENT_MAX_SELECT_COUNT_CALLBACK("onMaxSelectCountCallback"),
        EVENT_MAX_GET_FIRST_PHOTO_CALLBACK("onGetFirstPhotoCallback");

        override fun toString(): String {
            return mName
        }
    }

    fun setItemWidth(itemWidth: Int) {
        this.mItemWidth = itemWidth
    }

    fun setItemHeight(itemHeight: Int) {
        this.mItemHeight = itemHeight
    }

    fun setDefaultSelectedPosition(position: Int) {
        if (position >= 0) {
            mCurrentClickPosition = position
        }
        mDefaultSelectedPosition = position
    }

    init {
        mInitViewLoad = false
        mEventEmitter = reactContext.getJSModule(RCTEventEmitter::class.java)
        this.mWidth = ScreenUtils.getWidth(reactContext)
        this.mHeight = ScreenUtils.getHeight(reactContext)
        mPhotoList.clear()
        mSelectedPhotoList.clear()

        queryMediaManager = QueryMediaManager()

        reactContext.runOnUiQueueThread {
            initViews()
            initMedias()
        }

        reactContext.addLifecycleEventListener(object : LifecycleEventListener {
            override fun onHostResume() {
                //TODO 这里做刷新相册
                if (mInitViewLoad) {
//                   queryMediaManager.initLoad(mContext)
                }
            }

            override fun onHostPause() {
            }

            override fun onHostDestroy() {
            }

        })
    }

    fun setPageSize(pageSize: Int) {
    }

    fun setNumColumns(numColumns: Int) {
        this.mNumColumns = numColumns
    }

    fun setMultiSelect(multiSelect: Boolean) {
        this.mMultiSelect = multiSelect

        if (mInitViewLoad) {
            if (multiSelect) {
                //单选设置成多选，将当前选择的图片设置成选中
                mSelectedPhotoList.clear()
                mSelectedPhotoList.add(mPhotoList[mCurrentClickPosition])
                mSelectedPhotoMap[mCurrentClickPosition] = mSelectedPhotoList.size
            } else {
                //多选设置成单选，将
                mSelectedPhotoMap.clear()
                mSelectedPhotoList.clear()
                mSelectedPhotoList.add(mPhotoList[mCurrentClickPosition])
            }

            mPhotoAdapter?.setMultiSelect(multiSelect)
            mPhotoAdapter?.notifyItemRangeChanged(0, mPhotoList.size, "MultiSelectChanged")

            //回调一次RN
            sendRNSelectedPhotos(mCurrentClickPosition, mSelectedPhotoList)
        }
    }


    private fun initViews() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.view_ali_kit_photos, this, true)
        mPhotoRecyclerView = view.findViewById<RecyclerView>(R.id.photosRecyclerView)
        filAdapter()

        mPhotoAdapter?.setOnPhotoItemListener(object : PhotoAdapter.OnPhotoItemListener() {
            override fun onAddPhotoClick(position: Int, info: MediaInfo) {
                super.onAddPhotoClick(position, info)
                mCurrentClickPosition = position
                if (mMultiSelect) {

                    if (info.type == MediaStorage.TYPE_VIDEO) {
                        mSelectedPhotoMap.clear()
                        mSelectedPhotoList.clear()
                    }

                    if (mSelectedPhotoMap[position] == null) {
                        //多选
                        mSelectedPhotoList.add(info)
                        mSelectedPhotoMap[position] = mSelectedPhotoList.size
                    }
                    val selectPosition: Int? = mSelectedPhotoMap[position]
                    selectPosition?.let { sendRNSelectedPhotos(it - 1, mSelectedPhotoList) }
                } else {
                    //单选
                    mSelectedPhotoList.clear()
                    mSelectedPhotoList.add(info)
                    sendRNSelectedPhotos(0, mSelectedPhotoList)
                }
                Log.e("AAA", "mCurrentClickPosition：$mCurrentClickPosition")
            }

            override fun onRemovePhotoClick(position: Int, info: MediaInfo) {
                super.onRemovePhotoClick(position, info)
                val removeListPosition = mSelectedPhotoMap[position]
                removeListPosition?.let {
                    mSelectedPhotoList.removeAt(it - 1)
                    mSelectedPhotoMap.remove(position)

                    var maxValue: Int = 0
                    var maxKey: Int = 0
                    for ((key, value) in mSelectedPhotoMap) {
                        if (value > removeListPosition) {
                            mSelectedPhotoMap[key] = value - 1
                        }
                        mPhotoAdapter?.notifyItemChanged(key)

                        if (value > maxValue) {
                            maxValue = value
                            maxKey = key
                        }
                        Log.e("AAA", "key:$key" + "；value:" + mSelectedPhotoMap[key])
                    }

                    if (position == mCurrentClickPosition) {
                        mCurrentClickPosition = maxKey
                    }

                    if (mSelectedPhotoMap.isEmpty()) {
                        mPhotoAdapter?.notifyItemRangeChanged(0, mPhotoList.size, "MultiSelectChanged")
                    } else {
                        mPhotoAdapter?.notifyItemChanged(position)
                    }

                    if (mSelectedPhotoList.isNotEmpty()) {
                        mPhotoAdapter?.setCurrentClickPosition(mCurrentClickPosition)
                    }

                    Log.e("AAA", "mCurrentClickPosition：$mCurrentClickPosition")
                    sendRNSelectedPhotos(mCurrentClickPosition, mSelectedPhotoList)
                }
            }

            override fun onMaxSelectCountCallback() {
                super.onMaxSelectCountCallback()
                mEventEmitter?.receiveEvent(id, EventEmitterKeys.EVENT_MAX_SELECT_COUNT_CALLBACK.toString(), Arguments.createMap())
            }
        })

        mInitViewLoad = true
    }


    private fun filAdapter() {
        if (mPhotoAdapter == null) {

            val itemWidth = if (mItemWidth == 0) {
                this.mWidth/mNumColumns
            } else {
                this.mItemWidth
            }
            val itemHeight = if (mItemHeight == 0) {
                this.mWidth/mNumColumns
            } else {
                this.mItemHeight
            }
            mPhotoAdapter = PhotoAdapter(context, mPhotoList, mSelectedPhotoMap, itemWidth, itemHeight, mDefaultSelectedPosition)
            mPhotoAdapter?.setMultiSelect(mMultiSelect)
            mPhotoRecyclerView?.layoutManager = GridLayoutManager(context, mNumColumns)
            mPhotoRecyclerView?.addItemDecoration(GridSpacingItemDecoration(mNumColumns, 2, false))
            (mPhotoRecyclerView?.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            mPhotoRecyclerView?.adapter = mPhotoAdapter
        } else {
            mPhotoAdapter?.setMultiSelect(mMultiSelect)
            mPhotoAdapter?.notifyDataSetChanged()
        }
    }

    private fun initMedias() {

        queryMediaManager?.initLoad(mContext.applicationContext)
        queryMediaManager?.setOnQueryMediaListener(object : QueryMediaManager.OnQueryMediaListener() {
            override fun onDataUpdate(photoList: MutableList<MediaInfo>, baseCount: Int) {
                super.onDataUpdate(photoList, baseCount)
                updatePhotoList(photoList, baseCount)
            }

            override fun onCompletion() {
                super.onCompletion()
                if (mPhotoList.isNotEmpty()) {
                    if (mDefaultSelectedPosition >= 0) {
                        val info = mPhotoList[mDefaultSelectedPosition];
                        mCurrentClickPosition = mDefaultSelectedPosition
                        if (mMultiSelect) {
                            if (info.type == MediaStorage.TYPE_VIDEO) {
                                mSelectedPhotoMap.clear()
                                mSelectedPhotoList.clear()
                            }
                            if (mSelectedPhotoMap[mDefaultSelectedPosition] == null) {
                                //多选
                                mSelectedPhotoList.add(info)
                                mSelectedPhotoMap[mDefaultSelectedPosition] = mSelectedPhotoList.size
                            }
                            val selectPosition: Int? = mSelectedPhotoMap[mDefaultSelectedPosition]
                            selectPosition?.let { sendRNSelectedPhotos(it - 1, mSelectedPhotoList) }
                        } else {
                            //单选
                            mSelectedPhotoList.clear()
                            mSelectedPhotoList.add(info)
                            sendRNSelectedPhotos(0, mSelectedPhotoList)
                        }
                        filAdapter()
                    }
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

    fun sendFirstPhoto(info: MediaInfo) {
        val map: WritableMap = Arguments.createMap()
        map.putString("uri", info.fileUri)
        mEventEmitter?.receiveEvent(id, EventEmitterKeys.EVENT_MAX_GET_FIRST_PHOTO_CALLBACK.toString(), map)
    }

    fun sendRNSelectedPhotos(selectPosition: Int, photoList: MutableList<MediaInfo>) {
        val arrayList = WritableNativeArray()

        for (i in photoList.indices) {
            val info = photoList[i]
            val filePath = info.filePath

            if (!FileUtils.fileIsExists(filePath)) {
                continue
            }

            val videoFile = File(filePath)

            var rotation: Int = 0
            var videoWidth: Int = 0
            var videoHeight: Int = 0

            if (info.type == MediaStorage.TYPE_VIDEO) {
                val nativeParser = NativeParser()
                nativeParser.init(filePath)
                rotation = nativeParser.getValue(NativeParser.VIDEO_ROTATION).toInt()
                videoWidth = nativeParser.getValue(NativeParser.VIDEO_WIDTH).toDouble().toInt()
                videoHeight = nativeParser.getValue(NativeParser.VIDEO_HEIGHT).toDouble().toInt()
                nativeParser.release()
            } else {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val bitmap = BitmapFactory.decodeFile(filePath, options)
                options.inSampleSize = 1
                options.inJustDecodeBounds = false;
                videoWidth = options.outWidth
                videoHeight = options.outHeight
//                if (bitmap?.isRecycled != true) {
//                    bitmap.recycle()
//                }
            }



            if (rotation == 90 || rotation == 270) {
                val temp = videoHeight
                videoHeight = videoWidth
                videoWidth = temp
            }

            val map: WritableMap = Arguments.createMap()
            map.putInt("index", i)
            map.putInt("width", videoWidth.toInt())
            map.putInt("height", videoHeight.toInt())

            map.putString("path", "file://" + info.filePath)
            map.putString("uri", info.fileUri)

            map.putDouble("fileSize", videoFile.length().toDouble())
            map.putString("filename", videoFile.name)
            map.putString("type", info.mimeType)
            map.putInt("playableDuration", info.duration)
            map.putInt("rotation", rotation)

            arrayList.pushMap(map)
        }

        val map: WritableMap = Arguments.createMap()
        map.putInt("selectedIndex", selectPosition)
        map.putArray("data", arrayList)
        mEventEmitter?.receiveEvent(id, EventEmitterKeys.EVENT_SELECTED_PHOTO_CALLBACK.toString(), map)
    }

    fun onDestroy() {
        queryMediaManager?.onDestroy()
        mPhotoAdapter?.onDestroy()
    }

}