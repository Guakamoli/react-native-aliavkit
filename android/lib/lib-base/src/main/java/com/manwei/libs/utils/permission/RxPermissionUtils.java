package com.manwei.libs.utils.permission;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.tbruyelle.rxpermissions3.Permission;
import com.tbruyelle.rxpermissions3.RxPermissions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;


/**
 * 权限请求工具
 * <p>
 * Fragment 中会报：java.lang.IllegalStateException Can not perform this action after onSaveInstanceState
 *
 * @author wuyq
 */
public class RxPermissionUtils {

    /**
     * 判断是否获取权限
     */
    public boolean isPermissions(@NonNull FragmentActivity content, @NonNull String... permissions) {
        return PermissionUtils.isPermissions(content, permissions);
    }

    public boolean isPermissions(@NonNull Fragment content, @NonNull String... permissions) {
        return PermissionUtils.isPermissions(content.requireActivity(), permissions);
    }

    /**
     * 获取权限，如果同时获取多个权限，会回调多次结果
     */
    public void getPermissions(@NonNull FragmentActivity content, PermissionsListener listener, @NonNull String... permissions) {
        getPermissions(new RxPermissions(content), listener, permissions);
    }

    public void getPermissions(@NonNull Fragment content, PermissionsListener listener, @NonNull String... permissions) {
        getPermissions(new RxPermissions(content), listener, permissions);
    }

    /**
     * 组合获取权限，同时获取多个权限，只会回调一次
     */
    public void getPermissionsCombined(@NonNull FragmentActivity content, PermissionsListener listener, @NonNull String... permissions) {
        getPermissionsCombined(new RxPermissions(content), listener, permissions);
    }

    public void getPermissionsCombined(@NonNull Fragment content, PermissionsListener listener, @NonNull String... permissions) {
        getPermissionsCombined(new RxPermissions(content), listener, permissions);
    }


    /**
     *
     */
    public static RxPermissionUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final RxPermissionUtils INSTANCE = new RxPermissionUtils();
    }

    /**
     * 判断是否获取了全部权限
     * <p>
     * return:有一个权限未获取，返回 false
     */
    private boolean isPermissions(RxPermissions rxPermissions, @NonNull String... permissions) {
        for (String permission : permissions) {
            //多个权限判断时，只要其中有一个未获取，返回 false
            if (!isGranted(rxPermissions, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否获取权限
     */
    private boolean isGranted(RxPermissions rxPermissions, String permission) {
        if (rxPermissions == null) {
            return false;
        }
        return rxPermissions.isGranted(permission);
    }

    private void getPermissions(@NonNull RxPermissions rxPermissions, final PermissionsListener listener, @NonNull String... permissions) {
        dispose();
        // 组合请求，onNext 会执行多次回调
        rxPermissions.requestEach(permissions).subscribe(new Observer<Permission>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NotNull Permission permission) {
                if (permission.granted) {
                    //同意了权限
                    if (listener != null) {
                        listener.onGranted();
                    }
                } else if (permission.shouldShowRequestPermissionRationale) {
                    //拒绝了权限
                    if (listener != null) {
                        listener.onDenied(permission.name);
                    }
                } else {
                    //拒绝并且不再提示权限
                    if (listener != null) {
                        listener.onNeverAgain(permission.name);
                    }
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {
                dispose();
            }
        });
    }


    private void getPermissionsCombined(@NonNull RxPermissions rxPermissions, final PermissionsListener listener, @NonNull String... permissions) {
        //当权限都已经获取了，返回 onGranted();
        if (isPermissions(rxPermissions, permissions)) {
            if (listener != null) {
                listener.onGranted();
            }
            return;
        }
        dispose();
        // 组合请求，onNext 只会回调一次
        rxPermissions.requestEachCombined(permissions).subscribe(new Observer<Permission>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                addDisposable(d);
            }

            @Override
            public void onNext(@NotNull Permission permission) {
                if (permission.granted) {
                    //所有权限都同意
                    if (listener != null) {
                        listener.onGranted();
                    }
                } else if (permission.shouldShowRequestPermissionRationale) {
                    //至少有一个权限被拒绝
                    //同时申请两个权限。一个拒绝，一个拒绝并勾选不再提示，返回 onDenied
                    if (listener != null) {
                        listener.onDenied(permission.name);
                    }
                } else {
                    //至少有一个权限被拒绝并且勾选不再提示
                    if (listener != null) {
                        listener.onNeverAgain(permission.name);
                    }
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {

            }

            @Override
            public void onComplete() {
                dispose();
            }
        });
    }


    private CompositeDisposable mCompositeDisposable;

    private void addDisposable(Disposable subscription) {
        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(subscription);
    }

    private void dispose() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }

    /**
     * 跳转到APP应用信息页面
     */
    public static void getAppDetailSettingIntent(Context context) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 在 onStop activity.isFinishing() 时 调用 dispose 防止 rxJava 内存泄漏
     */
    public void onDispose() {
        dispose();
    }
}
