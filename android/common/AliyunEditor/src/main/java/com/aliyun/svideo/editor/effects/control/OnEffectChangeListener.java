/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.control;

/**
 * 选择完返回回调
 */
public interface OnEffectChangeListener {
    void onEffectChange(EffectInfo effectInfo);

    default void onChangeTime(int position,long startTime, long endTime){

    }
}
