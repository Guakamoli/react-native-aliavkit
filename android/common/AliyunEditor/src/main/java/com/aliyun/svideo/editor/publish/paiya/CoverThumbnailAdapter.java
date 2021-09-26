package com.aliyun.svideo.editor.publish.paiya;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.aliyun.svideo.editor.R;
import java.util.List;

public class CoverThumbnailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<CoverInfo> mList;

    private int mCoverItemWidth, mCoverItemHeight;

    public CoverThumbnailAdapter(Context context, List<CoverInfo> list, int itemWidth, int itemHeight){
        this.mContext = context;
        this.mList = list;
        this.mCoverItemWidth = itemWidth;
        this.mCoverItemHeight = itemHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paiya_cover_edit_item, parent, false);
        return new  CoverViewHolder(view);
    }

    private class CoverViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CoverViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof CoverViewHolder){
            CoverViewHolder coverViewHolder = (CoverViewHolder) holder;
            ViewGroup.LayoutParams  layoutParams =  coverViewHolder.imageView.getLayoutParams();
            layoutParams.width = mCoverItemWidth;
            layoutParams.height = mCoverItemHeight;
            coverViewHolder.imageView.setLayoutParams(layoutParams);
            if(mList.get(position)!=null){
                Bitmap bitmap =   mList.get(position).getBitmap();
                if(bitmap !=null && !bitmap.isRecycled()){
                    coverViewHolder.imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null){
            return 0;
        }
        return mList.size();
    }

}
