package com.aliyun.svideo.editor.effects.captions.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.publish.paiya.CoverInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * 字幕-缩略图 适配器
 */
public class CaptionThumbnailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<CoverInfo> mList;

    private int mCoverItemWidth, mCoverItemHeight, mScreenWidth;

    private  double mEndIntervalWidth;

    public CaptionThumbnailAdapter(Context context, List<CoverInfo> list, int itemWidth, int itemHeight, int screenWidth,double endIntervalWidth) {
        this.mContext = context;
        this.mList = list;
        this.mCoverItemWidth = itemWidth;
        this.mCoverItemHeight = itemHeight;
        this.mScreenWidth = screenWidth;
        this.mEndIntervalWidth = endIntervalWidth;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paiya_caption_chooser_item, parent, false);
        return new CaptionViewHolder(view, viewType);
    }

    private static class CaptionViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        int viewType;
        CaptionViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CaptionViewHolder) {
            CaptionViewHolder coverViewHolder = (CaptionViewHolder) holder;
            ViewGroup.LayoutParams layoutParams = coverViewHolder.imageView.getLayoutParams();
            if (position == mList.size()-1) {
                coverViewHolder.imageView.setScaleType(ImageView.ScaleType.MATRIX);
                layoutParams.width = (int) (mEndIntervalWidth*mCoverItemWidth);
            } else {
                coverViewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                layoutParams.width = mCoverItemWidth;
            }
            layoutParams.height = mCoverItemHeight;
            coverViewHolder.imageView.setLayoutParams(layoutParams);
            if (position < mList.size() && mList.get(position) != null) {
                Bitmap bitmap = mList.get(position).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    coverViewHolder.imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

}
