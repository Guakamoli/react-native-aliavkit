package com.aliyun.svideo.editor.publish.paiya;

import android.graphics.Bitmap;

public class CoverInfo {
    private Bitmap bitmap;
    private Integer position;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
