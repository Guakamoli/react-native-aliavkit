package com.aliyun.svideo.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aliyun.svideo.common.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * @author : wuyq
 * Time : 2020/9/24 13:45
 * Description :
 */
public abstract class BaseBottomFragment extends BottomSheetDialogFragment {


    protected Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //给dialog设置主题为透明背景 不然会有默认的白色背景
        setStyle(DialogFragment.STYLE_NO_FRAME, getStyle());
        isStarted = false;
        mExpandHeight = -1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mExpandHeight = -1;
        isStarted = false;
        View view = inflater.inflate(getContentView(), container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    protected int getStyle() {
        return R.style.BaseBottomFragmentStyle;
    }

    protected abstract int getContentView();

    protected abstract void initView(View view);


    private BottomSheetDialog mBottomSheetDialog;


    private void getBottomSheetDialog() {
        if (mBottomSheetDialog == null) {
            Dialog baseDialog = getDialog();
            if (baseDialog instanceof BottomSheetDialog) {
                mBottomSheetDialog = (BottomSheetDialog) baseDialog;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isStarted = true;
        if (mExpandHeight > 0) {
            setExpandHeight(mExpandHeight);
        }
    }

    private boolean isStarted;
    private int mExpandHeight = -1;

    /**
     * 设置展开高度
     */
    public void setExpandHeight(int expandHeight) {
        if (!isStarted) {
            //如果还未执行 onStart()。记录下高度，待 onStart() 之后再设置高度
            this.mExpandHeight = expandHeight;
            return;
        }
        if (mBottomSheetDialog != null) {
            View bottomSheet = mBottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            assert bottomSheet != null;
            assert bottomSheet.getLayoutParams() != null;
            //修改的是我们传入的跟布局高度。必须在onStart之后设置，否则会崩溃
            bottomSheet.getLayoutParams().height = expandHeight;
        }
    }

    /**
     * 设置折叠的高度
     */
    public void setPeekHeight(int peekHeight) {
        if (mBottomSheetDialog == null) {
            getBottomSheetDialog();
        }
        if (mBottomSheetDialog != null) {
            BottomSheetBehavior<?> behavior = mBottomSheetDialog.getBehavior();
            //设置折叠高度
            behavior.setPeekHeight(peekHeight, true);
        }
    }

    /**
     * 是否可以下拉隐藏折叠状态
     * hideable=false:折叠状态时不能下拉隐藏
     */
    public void setHideable(boolean hideable) {
        if (mBottomSheetDialog == null) {
            getBottomSheetDialog();
        }
        if (mBottomSheetDialog != null) {
            BottomSheetBehavior<?> behavior = mBottomSheetDialog.getBehavior();
            behavior.setHideable(hideable);
        }
    }

    /**
     * 当隐藏时，是否跳过折叠状态，直接全部隐藏
     */
    public void setSkipCollapsed(boolean skipCollapsed) {
        if (mBottomSheetDialog == null) {
            getBottomSheetDialog();
        }
        if (mBottomSheetDialog != null) {
            BottomSheetBehavior<?> behavior = mBottomSheetDialog.getBehavior();
            behavior.setSkipCollapsed(skipCollapsed);
        }
    }


    /**
     * 是否禁止拖动来显示/隐藏，当设置为false，需要实现自定义方式来展开/折叠
     */
    public void setDraggable(boolean draggable) {
        if (mBottomSheetDialog == null) {
            getBottomSheetDialog();
        }
        if (mBottomSheetDialog != null) {
            BottomSheetBehavior<?> behavior = mBottomSheetDialog.getBehavior();
            behavior.setDraggable(draggable);
        }
    }


    /**
     * 设置 BottomSheetDialog 状态
     * <p>
     * BottomSheetBehavior.STATE_COLLAPSED：折叠（默认）
     * BottomSheetBehavior.STATE_EXPANDED：展开
     * BottomSheetBehavior.STATE_HIDDEN：隐藏，布局区域全部不可见，半透明背景可见
     * BottomSheetBehavior.STATE_HALF_EXPANDED：折叠布局半关闭，fitToContents=false时有效。
     * 这时 BottomSheetDialogFragment 有3个可见状态：展开、折叠、半折叠
     */
    public void setState(int state) {
        if (mBottomSheetDialog == null) {
            getBottomSheetDialog();
        }
        if (mBottomSheetDialog != null) {
            BottomSheetBehavior<?> behavior = mBottomSheetDialog.getBehavior();
            if (state == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                behavior.setFitToContents(false);
            }
            behavior.setState(state);
        }
    }


    /**
     * android Q 以下版本： style 中设置的 android:windowBackground背景色无效，会变透明，需要代码设置
     */
    public void setWindowBackground(@ColorInt int color) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (mBottomSheetDialog == null) {
                getBottomSheetDialog();
            }
            if (mBottomSheetDialog != null && mBottomSheetDialog.getWindow() != null) {
                mBottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(color));
            }
//        }
    }


    public void setWindowBackground(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (mBottomSheetDialog == null) {
            getBottomSheetDialog();
        }
        if (mBottomSheetDialog != null && mBottomSheetDialog.getWindow() != null) {
            mBottomSheetDialog.getWindow().setBackgroundDrawable(drawable);
        }
    }

    @Override
    public int show(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        //重复添加 fragmenta 或者 DialogFragment.show 导致  java.lang.IllegalStateException
        if (isAdded()) {
            dismiss();
        }
        return super.show(transaction, tag);
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        //重复添加 fragmenta 或者 DialogFragment.show 导致  java.lang.IllegalStateException
        if (isAdded()) {
            dismiss();
        }
        super.show(manager, tag);
    }
}
