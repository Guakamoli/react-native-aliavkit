package com.rncamerakit.photos

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions
import com.aliyun.svideo.media.MediaInfo
import com.aliyun.svideo.media.MediaStorage
import com.aliyun.svideo.media.ThumbnailGenerator
import com.aliyun.svideo.media.ThumbnailGenerator.OnThumbnailGenerateListener
import com.rncamerakit.R
import java.io.File
import kotlin.math.roundToInt

class PhotoAdapter(private val mContext: Context, private val mList: List<MediaInfo>, private val mItemWidth: Int, private val mItemHeight: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mItemListener: OnPhotoItemListener? = null

    fun setOnPhotoItemListener(listener: OnPhotoItemListener) {
        mItemListener = listener
    }

    abstract class OnPhotoItemListener {
        open fun onItemClick(position: Int, info: MediaInfo) {}
    }

    private var mMultiSelect: Boolean = false

    fun setMultiSelect(multiSelect: Boolean) {
        this.mMultiSelect = multiSelect
    }

    private var mThumbnailGenerator: ThumbnailGenerator? = null

    private var mCurrentClickPosition = 0
    private var mOldCurrentClickPosition = 0

    private fun setCurrentClickPosition(position: Int) {
        mCurrentClickPosition = position
        notifyItemChanged(mOldCurrentClickPosition)
        notifyItemChanged(mCurrentClickPosition)
        mOldCurrentClickPosition = mCurrentClickPosition
    }

    init {
        mThumbnailGenerator = ThumbnailGenerator(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.view_ali_kit_photos_item, parent, false)
        return PhotoViewHolder(view, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PhotoViewHolder) {
            val info: MediaInfo = mList[position]
            //每一个imageView都需要设置tag，video异步生成缩略图，需要对应最后设置给imageView的info key
            holder.thumbnailImage.setTag(R.id.tag_first, ThumbnailGenerator.generateKey(info.type, info.id))

            if (info.thumbnailPath != null && onCheckFileExists(info.thumbnailPath)) {
                val uri: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    info.fileUri
                } else {
                    "file://" + info.thumbnailPath
                }
                ImageLoaderImpl().loadImage(
                    holder.thumbnailImage.context, uri,
                    ImageLoaderOptions.Builder().override(mItemWidth, mItemHeight)
                        .skipMemoryCache()
                        .placeholder(ColorDrawable(Color.GRAY))
                        .build()
                ).into(holder.thumbnailImage)
                holder.durationText.text = ""
            } else if (info.type == MediaStorage.TYPE_PHOTO) {
                val uri: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    info.fileUri
                } else {
                    "file://" + info.filePath
                }
                ImageLoaderImpl().loadImage(
                    holder.thumbnailImage.context, uri,
                    ImageLoaderOptions.Builder().override(mItemWidth, mItemHeight)
                        .skipMemoryCache()
                        .placeholder(ColorDrawable(Color.GRAY))
                        .build()
                ).into(holder.thumbnailImage)
                holder.durationText.text = ""
            } else {
                holder.thumbnailImage.setImageDrawable(ColorDrawable(Color.GRAY))
                mThumbnailGenerator?.generateThumbnail(info.type, info.id, 0,
                    OnThumbnailGenerateListener { key, thumbnail ->
                        if (key == holder.thumbnailImage.getTag(R.id.tag_first) as Int) {
                            holder.thumbnailImage.setImageBitmap(thumbnail)
                        }
                    })
                holder.durationText.text = convertDuration2Text(info.duration.toLong())
            }

            if (mCurrentClickPosition == position) {
                holder.selectedView.visibility = View.VISIBLE
            } else {
                holder.selectedView.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (position == mOldCurrentClickPosition) {
                    return@setOnClickListener
                }
                setCurrentClickPosition(position)
                mItemListener?.onItemClick(position, info)
            }
        }
    }


    private class PhotoViewHolder internal constructor(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnailImage)
        var durationText: TextView = itemView.findViewById(R.id.durationText)
        var selectedView: View = itemView.findViewById(R.id.selectedView)
    }

    override fun getItemCount(): Int {
        return mList.size
    }


    private fun onCheckFileExists(path: String?): Boolean {
        var res = false
        if (path == null) {
            return false
        }
        val file = File(path)
        if (file.exists()) {
            res = true
        }
        return res
    }

    private fun convertDuration2Text(duration: Long): String? {
        var sec = (duration.toFloat()/1000).roundToInt()
        val hour = sec/3600
        val min = sec%3600/60
        sec %= 60
        return String.format(
            mContext.getString(R.string.alivc_media_video_duration_mmmss),
            hour,
            min,
            sec
        )
    }

    fun onDestroy() {
        mThumbnailGenerator?.cancelAllTask()
    }

}
