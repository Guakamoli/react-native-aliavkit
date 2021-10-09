package com.manwei.libs.dialog;

/**
 * @author : wuyq
 * Time : 2020/11/1 17:34
 * Description :
 */
public interface OnDialogListener {
    /**
     * 点击回调
     */
    default void onLeftClick() {
    }

    /**
     * 点击回调
     */
    default void onRightClick() {
    }

    /**
     * Dialog关闭回调
     */
    default void onCancel() {
    }
}
