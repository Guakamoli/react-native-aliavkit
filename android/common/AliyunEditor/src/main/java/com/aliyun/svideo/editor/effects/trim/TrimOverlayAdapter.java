package com.aliyun.svideo.editor.effects.trim;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;

import org.jetbrains.annotations.NotNull;


public class TrimOverlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private int mItemWidth, mMinTimeWidth;
    private long mDuration;

    public TrimOverlayAdapter(Context context, int itemWidth, int minTimeWidth,long duration) {
        this.mContext = context;
        this.mItemWidth = itemWidth;
        this.mMinTimeWidth = minTimeWidth;
        this.mDuration = duration;
        this.mStartTime = 0;
        this.mEndTime = mDuration;
    }

    private TrimLineOverlay.OnSelectorPlayTimeListener mSelectorPlayTimeListener;

    public void setOnSelectorPlayTimeListener(TrimLineOverlay.OnSelectorPlayTimeListener listener){
        mSelectorPlayTimeListener = listener;
    }

    private long mStartTime, mEndTime;
    public void setTrimTime(long startTime,long endTime){
        mStartTime = startTime;
        mEndTime = endTime;
        notifyItemChanged(0);
    }


    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paiya_trim_overlay_item, parent, false);
        return new ViewHolder(view, viewType);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TrimLineOverlay trimLineOverlay;
        View view;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            trimLineOverlay = itemView.findViewById(R.id.mTrimLineOverlay);
            view = itemView.findViewById(R.id.mView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            View view = ((ViewHolder) holder).view;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = mItemWidth;
            view.setLayoutParams(layoutParams);

            TrimLineOverlay trimLineOverlay = ((ViewHolder) holder).trimLineOverlay;
            trimLineOverlay.setOnSelectorPlayTimeListener(mSelectorPlayTimeListener);
            trimLineOverlay.setData(mItemWidth,mMinTimeWidth,mDuration,mStartTime,mEndTime);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
