package com.aliyun.svideo.editor.effects.captions.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.publish.paiya.CoverInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 时间轴适配器
 */
public class CaptionTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<CoverInfo> mList;

    public CaptionTimelineAdapter(Context context, List<CoverInfo> list) {
        this.mContext = context;
        this.mList = list;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paiya_time_line_item, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TimelineViewHolder) {
            TextView tvTimeLine = ((TimelineViewHolder) holder).tvTimeLine;
            tvTimeLine.setText(convertDuration2Text(position));
        }
    }


    private static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimeLine;

        TimelineViewHolder(View itemView) {
            super(itemView);
            tvTimeLine = itemView.findViewById(R.id.tvTimeLine);
        }
    }


    private String convertDuration2Text(long position) {
        if (position % 2 != 0) {
            return "·";
        }

        StringBuilder stringBuilder = new StringBuilder();


        int min = (int) (position  / 60);
        if (min >= 10) {
            stringBuilder.append(min);
        } else {
            stringBuilder.append("0").append(min);
        }
        stringBuilder.append(":");

        int sec = 0;
        sec = (int) (position % 60);
        if (sec >= 10) {
            stringBuilder.append(sec);
        } else {
            stringBuilder.append("0").append(sec);
        }
        return stringBuilder.toString();
    }


    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size() + 1;
    }
}
