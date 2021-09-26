package com.manwei.libs.utils.permission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentActivity  和  Fragment 中申请权限 请使用 RxPermissionUtils
 * <p>
 * Activity 申请时必须在 onRequestPermissionsResult 中调用 PermissionUtils.onRequestPermissionsResult 方法
 *
 * @author wuyq
 */
public class PermissionUtils {

    private final static int REQUEST_PERMISSION_CODE = 0x89;
    private final static int REQUEST_PERMISSION_COMBINED_CODE = 0x90;

    private PermissionsListener mListener;

    /**
     * 判断是否获取权限
     */
    public static boolean isPermissions(@NonNull FragmentActivity content, @NonNull String[] permissions) {
        for (String permission : permissions) {
            //多个权限判断时，只要其中有一个未获取，返回 false
            if (!isPermission(content, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有该权限
     */
    public static boolean isPermission(FragmentActivity context, String permission) {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }


    /**
     * 获取权限，如果同时获取多个权限，会回调多次结果
     */
    public void getPermissions(@NonNull AppCompatActivity content, PermissionsListener listener, @NonNull String... permissions) {
        this.mListener = listener;
        requestPermissions(content, REQUEST_PERMISSION_CODE, permissions);
    }

    /**
     * 组合权限，listener 只回调一次
     */
    public void getPermissionsCombined(@NonNull AppCompatActivity content, PermissionsListener listener, @NonNull String... permissions) {
        this.mListener = listener;
        requestPermissions(content, REQUEST_PERMISSION_COMBINED_CODE, permissions);
    }

    /**
     * 请求权限结果，对应 Activity 中 onRequestPermissionsResult() 方法。
     */
    public void onRequestPermissionsResult(final AppCompatActivity context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (context.isFinishing()) {
            return;
        }
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    //同意授权
                    if (mListener != null) {
                        mListener.onGranted();
                    }
                    continue;
                }
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    if (isNeverAgainPermission(context, permissions[i])) {
                        //权限拒绝并且不再提示
                        if (mListener != null) {
                            mListener.onNeverAgain(permissions[i]);
                        }
                    } else {
                        //权限被拒绝
                        if (mListener != null) {
                            mListener.onDenied(permissions[i]);
                        }
                    }
                }
            }
        } else if (requestCode == REQUEST_PERMISSION_COMBINED_CODE) {
            //组合权限请求，mListener 只回调一次
            if (isPermissions(context, permissions)) {
                //全部同意
                if (mListener != null) {
                    mListener.onGranted();
                }
            } else {
                // 有权限 被拒绝 或者 被拒绝且不再提示
                List<String> deniedPermissions = new ArrayList<>();
                List<String> neverAgainPermissions = new ArrayList<>();
                for (String permission : permissions) {
                    if (!isPermission(context, permission)) {
                        if (isNeverAgainPermission(context, permission)) {
                            neverAgainPermissions.add(permission);
                        } else {
                            deniedPermissions.add(permission);
                        }
                    }
                }
                if (deniedPermissions.size() > 0) {
                    //组合申请权限时，权限组中 “拒绝权限” 和 “拒绝并且不再提示” 都存在时，优先回调 onDenied
                    //和 RxPermission 中的逻辑统一
                    if (mListener != null) {
                        mListener.onDenied(TextUtils.join(",", deniedPermissions));
                    }
                } else {
                    if (mListener != null) {
                        mListener.onNeverAgain(TextUtils.join(",", neverAgainPermissions));
                    }
                }
            }
        }
    }


    /**
     * 是否彻底拒绝了某项权限。
     * <p>
     * <p> * * * 还未开始申请权限时，返回 true， 所以这个方法要放到 申请权限回调之后执行
     *
     * @param context
     * @param deniedPermissions 权限
     * @return 只要有一个权限拒绝并且不再提示，那么返回 true
     */
    public static boolean isNeverAgainPermission(@NonNull AppCompatActivity context, @NonNull String... deniedPermissions) {
        for (String deniedPermission : deniedPermissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isPermission(context, deniedPermission)) {
                if (!shouldShowRequestPermissionRationale(context, deniedPermission)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否有权限需要说明提示
     * //shouldShowRequestPermissionRationale 在 拒绝权限并且 没有 勾选不再提示时 会返回true, 其余都返回 false
     * //当判断没有权限，并且返回时 false 时判断用户：勾选了不在提示并且拒绝权限了。
     * //注意：如果没有调用过申请权限 requestPermissions，直接调用当前方法也是返回 false ,所以这个方法要放到申请权限之后执行。
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean shouldShowRequestPermissionRationale(final AppCompatActivity context, final String permission) {
        return context.shouldShowRequestPermissionRationale(permission);
    }


    /**
     * 去请求权限
     */
    private void requestPermissions(AppCompatActivity content, int requestCode, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || isPermissions(content, permissions)) {
            //权限全部获取了
            if (requestCode == REQUEST_PERMISSION_COMBINED_CODE) {
                if (mListener != null) {
                    mListener.onGranted();
                }
            } else {
                for (int i = 0; i < permissions.length; i++) {
                    if (mListener != null) {
                        mListener.onGranted();
                    }
                }
            }
            return;
        }
        //去申请权限
        content.requestPermissions(permissions, requestCode);
    }


    public static PermissionUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final PermissionUtils INSTANCE = new PermissionUtils();
    }

    /**
     * 跳转到APP应用信息页面
     */
    public static void getAppDetailSettingIntent(AppCompatActivity context) {
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

}
