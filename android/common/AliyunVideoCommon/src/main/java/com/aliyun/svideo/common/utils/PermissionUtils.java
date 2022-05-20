package com.aliyun.svideo.common.utils;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.aliyun.svideo.common.R;

/**
 * 检查权限/权限数组
 * request权限
 */
public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getName();

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
     * 判断是否有该权限
     */
    public static boolean isPermission(FragmentActivity context, String permission) {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public static final String[] PERMISSION_MANIFEST = {
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static final String[] PERMISSION_CAMERA = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 无权限时对应的提示内容
     */
    public static final int[] NO_PERMISSION_TIP = {
            R.string.alivc_common_no_camera_permission,
            R.string.alivc_common_no_record_bluetooth_permission,
            R.string.alivc_common_no_record_audio_permission,
            R.string.alivc_common_no_read_phone_state_permission,
            R.string.alivc_common_no_write_external_storage_permission,
            R.string.alivc_common_no_read_external_storage_permission,
    };


    /**
     * 检查多个权限
     * <p>
     * 检查权限
     *
     * @param permissions 权限数组
     * @param context     Context
     * @return true 已经拥有所有check的权限 false存在一个或多个未获得的权限
     */
    public static boolean checkPermissionsGroup(Context context, String[] permissions) {

        for (String permission : permissions) {
            if (!checkPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查单个权限
     *
     * @param context    Context
     * @param permission 权限
     * @return boolean
     */
    private static boolean checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 申请权限
     *
     * @param activity    Activity
     * @param permissions 权限数组
     * @param requestCode 请求码
     */
    public static void requestPermissions(FragmentActivity activity, String[] permissions, int requestCode) {
        // 先检查是否已经授权
        if (!checkPermissionsGroup(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    /**
     * 通过AppOpsManager判断小米手机授权情况
     *
     * @return boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkXiaomi(Context context, String[] opstrArrays) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        String packageName = context.getPackageName();
        for (String opstr : opstrArrays) {
            int locationOp = appOpsManager.checkOp(opstr, Binder.getCallingUid(), packageName);
            if (locationOp == AppOpsManager.MODE_IGNORED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 没有权限的提示
     *
     * @param context Context
     * @param tip     对于的提示 {@link #NO_PERMISSION_TIP}
     */
    public static void showNoPermissionTip(Context context, String tip) {
        ToastUtils.show(context, tip, Toast.LENGTH_LONG);
    }

}
