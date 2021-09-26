package com.rncamerakit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface;
import com.aliyun.svideo.recorder.util.RecordCommon;
import com.aliyun.svideo.recorder.view.focus.FocusView;
import com.facebook.react.uimanager.ThemedReactContext;
import com.rncamerakit.recorder.RecorderManage;


import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CKCamera extends FrameLayout {

    private ThemedReactContext mContext;

    //焦点View
    private FocusView mFocusView;
    private SurfaceView mRecorderSurfaceView;
    private FrameLayout mVideoContainer;

    private RecorderManage mRecorderManage;
    private AlivcIMixRecorderInterface mRecorder;

    private DisposableObserver mDisposableObserver;

    private int mWidth, mHeight;

    public CKCamera(@NonNull ThemedReactContext context) {
        super(context);
        mContext = context;
        getPermissions(context);
        init();
    }

    private void init() {
        mWidth = ScreenUtils.getWidth(getContext());
        mHeight = mWidth * 16 / 9;
        initVideoContainer();
        initRecorderSurfaceView();
        initRecorder();
        initFocusView();
        copyAssets();
    }


    public RecorderManage getRecorderManage() {
        return mRecorderManage;
    }

    private void initRecorder() {
        mRecorderManage = new RecorderManage(mContext);
        mRecorder = mRecorderManage.getRecorder();
        mRecorder.setDisplayView(mRecorderSurfaceView, null);
        mRecorder.startPreview();
    }

    private void initVideoContainer() {
        mVideoContainer = new FrameLayout(getContext());
        LayoutParams params = new LayoutParams(mWidth, mHeight);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        addView(mVideoContainer, params);
    }

    private float lastScaleFactor;
    private float scaleFactor;

    @SuppressLint("ClickableViewAccessibility")
    private void initRecorderSurfaceView() {
        mRecorderSurfaceView = new SurfaceView(getContext());
        FrameLayout container = new FrameLayout(getContext());
        LayoutParams slp = new LayoutParams(mWidth, mHeight);
        slp.gravity = Gravity.CENTER;
        container.addView(mRecorderSurfaceView, slp);
        LayoutParams layoutParams = new LayoutParams(slp.width, slp.height);
        mVideoContainer.addView(container, layoutParams);

        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factorOffset = detector.getScaleFactor() - lastScaleFactor;
                scaleFactor += factorOffset;
                lastScaleFactor = detector.getScaleFactor();
                if (scaleFactor < 0) {
                    scaleFactor = 0;
                }
                if (scaleFactor > 1) {
                    scaleFactor = 1;
                }
                if (mRecorder != null) {
                    //设置缩放
                    mRecorder.setZoom(scaleFactor);
                }
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                lastScaleFactor = detector.getScaleFactor();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
        final GestureDetector gestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if (mRecorder == null) {
                            return true;
                        }
                        float x = e.getX() / mRecorderSurfaceView.getWidth();
                        float y = e.getY() / mRecorderSurfaceView.getHeight();
                        //手动对焦
                        mRecorder.setFocus(x, y);
                        mFocusView.showView();
                        mFocusView.setLocation(e.getRawX(), e.getRawY());
                        return true;
                    }
                });

        mRecorderSurfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() >= 2) {
                    scaleGestureDetector.onTouchEvent(event);
                } else if (event.getPointerCount() == 1) {
                    gestureDetector.onTouchEvent(event);
                }
                return true;
            }
        });
    }

    /**
     * 焦点View
     */
    private void initFocusView() {
        mFocusView = new FocusView(getContext());
        mFocusView.setPadding(10, 10, 10, 10);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mFocusView, params);//添加到布局中
        mFocusView.showViewInVisible();
    }


    private void copyAssets() {
        ProgressDialog progressBar = new ProgressDialog(mContext);
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setCancelable(false);
        progressBar.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        progressBar.show();
        mDisposableObserver = new DisposableObserver<String>() {
            @Override
            public void onNext(@NonNull String s) {
                if (mRecorderManage != null) {
                    mRecorderManage.initColorFilterAssets();
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onComplete() {
                progressBar.dismiss();
            }
        };
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                try {
                    RecordCommon.copyAll(mContext);
                    emitter.onNext("");
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mDisposableObserver);
    }


    public void onRelease() {
        if (mFocusView != null) {
            mFocusView.activityStop();
        }
        if (mRecorderManage != null) {
            mRecorderManage.onRelease();
        }
        if (mDisposableObserver != null) {
            mDisposableObserver.dispose();
        }
    }


    private void getPermissions(Context context) {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean checkResult = PermissionUtils.checkPermissionsGroup(context, permissions);
        if (!checkResult) {
            ActivityCompat.requestPermissions( Objects.requireNonNull(mContext.getCurrentActivity()), permissions, 0);
        }
    }


}
