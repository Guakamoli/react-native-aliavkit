package com.aliyun.svideo.editor.effects.captions.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;

import java.util.List;

public class CaptionOverlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private int mBaseWidth;
    private List<CaptionOverlayInfo> mList;

    private FrameLayout mCaptionLineOverlays;

    public FrameLayout getCaptionLineOverlays() {
        return mCaptionLineOverlays;
    }

    public CaptionOverlayAdapter(Context context, int baseWidth) {
        this.mContext = context;
        this.mBaseWidth = baseWidth;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paiya_caption_overlay_item, parent, false);
        return new CaptionOverlayAdapter.ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            mCaptionLineOverlays = ((ViewHolder) holder).captionLineOverlays;
            ViewGroup.LayoutParams layoutParams = mCaptionLineOverlays.getLayoutParams();
            layoutParams.width = mBaseWidth;
            mCaptionLineOverlays.setLayoutParams(layoutParams);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout captionLineOverlays;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            captionLineOverlays = itemView.findViewById(R.id.mCaptionLineOverlays);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
