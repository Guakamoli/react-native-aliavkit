package com.aliyun.svideo.editor.effects.captions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.editor.R;
import com.blankj.utilcode.util.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 底部滚动分词器
 */
public class CaptionView extends TouchRecyclerView {

    private int firstItemHeight = 62;
    private int secondItemHeight = 30;

    private int firstItemTextSize = 15;
    private int secondItemTextSize = 13;

    public CaptionView(@NonNull @NotNull Context context) {
        super(context);
        initView(context);
    }

    public CaptionView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CaptionView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private Context mContext;

    private CaptionChooserFragment mFragment;

    public void setCaptionChooserFragment(CaptionChooserFragment fragment) {
        this.mFragment = fragment;
    }

    private void initView(Context context) {
        this.mContext = context;
        firstItemHeight = DensityUtils.dip2px(mContext, firstItemHeight);
        secondItemHeight = DensityUtils.dip2px(mContext, secondItemHeight);
        setPadding(0, 0, 0, firstItemHeight + secondItemHeight * 2);
        setClipToPadding(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        setLayoutManager(layoutManager);
        setItemAnimator(new DefaultItemAnimator());

        addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        });

        initRecyclerView();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_MOVE) {
            requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void initRecyclerView() {
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = firstItemHeight + secondItemHeight * 2;
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
        setLayoutParams(layoutParams);
    }

    private CaptionViewAdapter mAdapter;

    private List<String> mList = new ArrayList<>();

    public void fillAdapter(List<String> list, int mInitPosition) {
        mList.clear();
        mList.addAll(list);
        if (mAdapter == null) {
            mAdapter = new CaptionViewAdapter(this, mFragment, mList);
            mAdapter.setInitPosition(mInitPosition);
            this.setAdapter(mAdapter);
        } else {
            mAdapter.setInitPosition(mInitPosition);
            mAdapter.notifyDataSetChanged();
        }
        scrollToPosition(mInitPosition);
        if (mInitPosition >= mList.size() - 1) {
            scrollTo(0, secondItemHeight * 2);
        }
    }

    public static class CaptionViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<String> mList;
        private CaptionView captionView;
        private CaptionChooserFragment fragment;
        private int mInitPosition;


        public void setInitPosition(int initPosition) {
            this.mInitPosition = initPosition;
        }

        CaptionViewAdapter(CaptionView captionView, CaptionChooserFragment fragment, List<String> list) {
            this.captionView = captionView;
            this.fragment = fragment;
            this.mList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.paiya_caption_tools_blessing_item, parent, false);
            return new CaptionViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
            if (holder instanceof CaptionViewHolder) {
                CaptionViewHolder coverViewHolder = (CaptionViewHolder) holder;
                if (position <= mInitPosition) {
                    ViewGroup.LayoutParams nextParams = coverViewHolder.itemLayout.getLayoutParams();
                    nextParams.height = captionView.firstItemHeight;
                    coverViewHolder.itemLayout.setLayoutParams(nextParams);
                    coverViewHolder.tvBlessing.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                } else {
                    ViewGroup.LayoutParams nextParams = coverViewHolder.itemLayout.getLayoutParams();
                    nextParams.height = captionView.secondItemHeight;
                    coverViewHolder.itemLayout.setLayoutParams(nextParams);
                    coverViewHolder.tvBlessing.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                }
                coverViewHolder.tvBlessing.setText(mList.get(position));

                if (TextUtils.isEmpty(mList.get(position))) {
                    coverViewHolder.tvAddCaption.setVisibility(GONE);
                } else {
                    coverViewHolder.tvAddCaption.setVisibility(VISIBLE);
                }

                coverViewHolder.tvAddCaption.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mInitPosition == position) {
                            if (fragment.isAddCaptionText()) {
                                captionView.setSelectPositionNext(position, coverViewHolder.itemLayout, mList.get(position));
                                mInitPosition++;
                            } else {
                                ToastUtils.showShort(R.string.paiya_add_caption_out);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        private static class CaptionViewHolder extends RecyclerView.ViewHolder {
            LinearLayout itemLayout;
            TextView tvBlessing;
            TextView tvAddCaption;
            int viewType;

            CaptionViewHolder(View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                tvBlessing = itemView.findViewById(R.id.tvBlessing);
                tvAddCaption = itemView.findViewById(R.id.tvAddCaption);
                itemLayout = itemView.findViewById(R.id.itemLayout);
            }
        }
    }


    /**
     * 向下滑动一个position
     */
    public void setSelectPositionNext(int position, LinearLayout itemLayout, String blessing) {
        if (mAddCaptionTextCallback != null) {
            mAddCaptionTextCallback.onAddText(position, blessing);
        }
        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("Alpha", 255, 80, 30, 0);
        PropertyValuesHolder heightHolder = PropertyValuesHolder.ofInt("Height", secondItemHeight, firstItemHeight);
        PropertyValuesHolder offsetYHolder = PropertyValuesHolder.ofInt("OffsetY", 0, firstItemHeight);
        PropertyValuesHolder textSizeHolder = PropertyValuesHolder.ofFloat("TextSize", secondItemTextSize, firstItemTextSize);
        ValueAnimator animator = ObjectAnimator.ofPropertyValuesHolder(alphaHolder, heightHolder, offsetYHolder, textSizeHolder);
        animator.setDuration(250);
        View view = Objects.requireNonNull(getLayoutManager()).findViewByPosition(position + 1);

        LinearLayout nextItemLayout;
        if (view instanceof LinearLayout) {
            nextItemLayout = (LinearLayout) view;
        } else {
            nextItemLayout = null;
        }

        TextView tvBlessing;
        if (nextItemLayout != null) {
            tvBlessing = nextItemLayout.findViewById(R.id.tvBlessing);
        } else {
            tvBlessing = null;
        }
        final int[] offsetY = {0};

//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation, boolean isReverse) {
////                smoothScrollToPosition(position + 1);
////                scrollToPosition(position);
//                Objects.requireNonNull(getAdapter()).notifyDataSetChanged();
//            }
//        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (tvBlessing != null) {
                    float textSizeValue = (float) animation.getAnimatedValue("TextSize");
                    tvBlessing.setTextSize(textSizeValue);
                }
                if (nextItemLayout != null) {
                    int heightValue = (int) animation.getAnimatedValue("Height");
                    ViewGroup.LayoutParams nextParams = nextItemLayout.getLayoutParams();
                    nextParams.height = heightValue;
                    nextItemLayout.setLayoutParams(nextParams);
                }

                int offsetYValue = (int) animation.getAnimatedValue("OffsetY");
                int currScroll = offsetYValue - offsetY[0];
                scrollBy(0, currScroll);
                offsetY[0] += currScroll;
            }
        });
        animator.start();
    }


    private OnAddCaptionTextCallback mAddCaptionTextCallback;

    public void setOnAddCaptionTextCallback(OnAddCaptionTextCallback callback) {
        this.mAddCaptionTextCallback = callback;
    }

    public interface OnAddCaptionTextCallback {
        void onAddText(int position, String blessing);
    }

}
