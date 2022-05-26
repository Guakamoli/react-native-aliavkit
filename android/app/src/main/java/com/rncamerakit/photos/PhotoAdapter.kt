package com.rncamerakit.photos

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliyun.svideo.media.MediaInfo
import com.aliyun.svideo.media.MediaStorage
import com.aliyun.svideo.media.ThumbnailGenerator
import com.aliyun.svideo.media.ThumbnailGenerator.OnThumbnailGenerateListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.rncamerakit.R
import java.io.File
import kotlin.math.roundToInt

class PhotoAdapter(
    private val mContext: Context,
    private val mList: List<MediaInfo>,
    private val mSelectedPhotoMap: HashMap<Int, Int>,
    private val mItemWidth: Int,
    private val mItemHeight: Int,
    private val mDefaultSelectedPosition: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var mItemListener: OnPhotoItemListener? = null

    fun setOnPhotoItemListener(listener: OnPhotoItemListener) {
        mItemListener = listener
    }

    abstract class OnPhotoItemListener {
        open fun onAddPhotoClick(position: Int, info: MediaInfo) {}
        open fun onRemovePhotoClick(position: Int, info: MediaInfo) {}
        open fun onMaxSelectCountCallback() {}
    }

    private var mMultiSelect: Boolean = false
    private var mMultiFileType: Int = MediaStorage.TYPE_PHOTO

    fun setMultiSelect(multiSelect: Boolean) {
        this.mMultiSelect = multiSelect
        if (mList.size > mCurrentClickPosition) {
            mMultiFileType = mList[mCurrentClickPosition].type
        }
    }

    private var mThumbnailGenerator: ThumbnailGenerator? = null

    private var mCurrentClickPosition = 0
    private var mOldCurrentClickPosition = 0

    fun setCurrentClickPosition(position: Int) {
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any?>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            //这里只刷新
            setMultiSelectChanged(mList[position], holder as PhotoViewHolder, position)
        }
    }


    fun setMultiSelectChanged(info: MediaInfo, holder: PhotoViewHolder, position: Int) {
        if (!mMultiSelect) {
            holder.tvCheckView.visibility = View.GONE
            holder.unSelectedBgView.visibility = View.GONE

            holder.flCheckView.visibility = View.GONE
            holder.tvCheckView.text = ""
        } else {

            holder.flCheckView.visibility = View.VISIBLE
            holder.tvCheckView.visibility = View.VISIBLE

            if (mSelectedPhotoMap[position] == null) {
                holder.tvCheckView.setBackgroundResource(R.drawable.bg_post_photo_unselected)
                holder.tvCheckView.text = ""
            } else {
                if (info.type == MediaStorage.TYPE_PHOTO) {
                    holder.tvCheckView.setBackgroundResource(R.drawable.bg_post_photo_selected)
                    holder.tvCheckView.text = (mSelectedPhotoMap[position]).toString()
                } else {
                    holder.tvCheckView.text = ""
                    holder.tvCheckView.setBackgroundResource(R.drawable.bg_post_photo_selected_video)
                }
            }

            if (mMultiFileType == info.type || mSelectedPhotoMap.isEmpty()) {
                holder.unSelectedBgView.visibility = View.GONE
            } else {
                holder.unSelectedBgView.visibility = View.VISIBLE
            }
        }


        //取消多选选中
        holder.flCheckView.setOnClickListener {
            if (mMultiSelect && (mMultiFileType != info.type && mSelectedPhotoMap.isNotEmpty())) {
                return@setOnClickListener
            }
            if (mSelectedPhotoMap[position] != null) {
                mItemListener?.onRemovePhotoClick(position, info)
            } else {
                if (mSelectedPhotoMap.size >= 10) {
                    mItemListener?.onMaxSelectCountCallback()
                    return@setOnClickListener
                }
                setCurrentClickPosition(position)
                if (mSelectedPhotoMap.isEmpty()) {
                    mMultiFileType = info.type
                    notifyItemRangeChanged(0, mList.size, "MultiSelectChanged")
                } else {
                    notifyItemChanged(position)
                }
                mItemListener?.onAddPhotoClick(position, info)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PhotoViewHolder) {

            var params = holder.thumbnailImage.layoutParams
            params.width = this.mItemWidth
            params.height = this.mItemHeight
            holder.thumbnailImage.layoutParams = params

            val info: MediaInfo = mList[position]
            //每一个imageView都需要设置tag，video异步生成缩略图，需要对应最后设置给imageView的info key
            holder.thumbnailImage.setTag(R.id.tag_first, ThumbnailGenerator.generateKey(info.type, info.id))
            val viewDrawable: Drawable = ColorDrawable(Color.GRAY)
            val uri: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                info.fileUri
            } else if (info.thumbnailPath != null && onCheckFileExists(info.thumbnailPath)) {
                "file://" + info.thumbnailPath
            } else {
                "file://" + info.filePath
            }
            Glide.with(mContext).load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .thumbnail()
                .placeholder(viewDrawable)
                .into(holder.thumbnailImage)
            if (info.type == MediaStorage.TYPE_PHOTO) {
                holder.durationText.text = ""
            } else {
                holder.durationText.text = convertDuration2Text(info.duration.toLong())
                mThumbnailGenerator?.generateThumbnail(info.type, info.id, 0,
                    OnThumbnailGenerateListener { key, thumbnail ->
                        if (key == holder.thumbnailImage.getTag(R.id.tag_first) as Int) {
                            holder.thumbnailImage.setImageBitmap(thumbnail)
                        }
                    })
            }

            if (mDefaultSelectedPosition >= 0) {
                if (mCurrentClickPosition == position) {
                    holder.selectedBgView.visibility = View.VISIBLE
                } else {
                    holder.selectedBgView.visibility = View.GONE
                }
            }
            setMultiSelectChanged(info, holder as PhotoViewHolder, position)

            holder.itemView.setOnClickListener {

                if (mDefaultSelectedPosition == -1) {
                    mItemListener?.onAddPhotoClick(position, info)
                    return@setOnClickListener
                }

                if (position == mOldCurrentClickPosition && mSelectedPhotoMap[position] != null) {
                    return@setOnClickListener
                }

                if (mMultiSelect && (mMultiFileType != info.type && mSelectedPhotoMap.isNotEmpty())) {
                    return@setOnClickListener
                }

                if (mSelectedPhotoMap.size >= 10) {
                    mItemListener?.onMaxSelectCountCallback()
                    return@setOnClickListener
                }

                if (mSelectedPhotoMap.isEmpty()) {
                    mMultiFileType = info.type
                    notifyItemRangeChanged(0, mList.size, "MultiSelectChanged")
                }

                setCurrentClickPosition(position)
                if (mMultiSelect) {
                    notifyItemChanged(position)
                }
                mItemListener?.onAddPhotoClick(position, info)
            }
        }
    }


    class PhotoViewHolder internal constructor(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {
        var thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnailImage)
        var durationText: TextView = itemView.findViewById(R.id.durationText)
        var selectedBgView: View = itemView.findViewById(R.id.selectedBgView)
        var unSelectedBgView: View = itemView.findViewById(R.id.unSelectedBgView)
        var flCheckView: ViewGroup = itemView.findViewById(R.id.flCheckView)
        var tvCheckView: TextView = itemView.findViewById(R.id.checkView)
    }

    override fun getItemCount(): Int {
        return mList.size
    }


    override fun getItemId(position: Int): Long {
//        return super.getItemId(position)
        return position.toLong()
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
