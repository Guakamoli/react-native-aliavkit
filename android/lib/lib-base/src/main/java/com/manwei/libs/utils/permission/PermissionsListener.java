package com.manwei.libs.utils.permission;

public interface PermissionsListener {

    /**
     * 同意权限申请
     */
    default void onGranted() {
    }

    /**
     * 拒绝权限申请
     */
    default void onDenied(String permission) {
    }

    /**
     * 拒绝权限，并且不再询问
     */
    default void onNeverAgain(String permission) {
    }

}
