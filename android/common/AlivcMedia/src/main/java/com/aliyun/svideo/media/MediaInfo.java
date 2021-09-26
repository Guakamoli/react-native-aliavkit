/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.media;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public class MediaInfo implements Parcelable, Cloneable {

    public String filePath;
    public String fileUri;
    public String thumbnailPath;
    public String thumbnailUri;
    public String mimeType;
    public String title;
    public long startTime;
    public int duration;
    public int id;
    public long addTime;
    public boolean isSquare;
    public int type;

    @Override
    public boolean equals(Object o) {
        if (o instanceof MediaInfo) {
            MediaInfo info = (MediaInfo) o;
            return id == info.id;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.filePath);
        dest.writeString(this.thumbnailPath);
        dest.writeString(this.mimeType);
        dest.writeString(this.title);
        dest.writeLong(this.startTime);
        dest.writeInt(this.duration);
        dest.writeInt(this.id);
        dest.writeLong(this.addTime);
        dest.writeByte(this.isSquare ? (byte) 1 : (byte) 0);
        dest.writeInt(this.type);
    }

    public MediaInfo() {
    }

    protected MediaInfo(Parcel in) {
        this.filePath = in.readString();
        this.thumbnailPath = in.readString();
        this.mimeType = in.readString();
        this.title = in.readString();
        this.startTime = in.readLong();
        this.duration = in.readInt();
        this.id = in.readInt();
        this.addTime = in.readLong();
        this.isSquare = in.readByte() != 0;
        this.type = in.readInt();
    }

    public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>() {
        @Override
        public MediaInfo createFromParcel(Parcel source) {
            return new MediaInfo(source);
        }

        @Override
        public MediaInfo[] newArray(int size) {
            return new MediaInfo[size];
        }
    };

    @NonNull
    @NotNull
    @Override
    public MediaInfo clone() {
        try {
            MediaInfo info = (MediaInfo) super.clone();
            info.filePath = this.filePath;
            info.fileUri = this.fileUri;
            info.thumbnailPath = this.thumbnailPath;
            info.thumbnailUri = this.thumbnailUri;
            info.mimeType = this.mimeType;
            info.title = this.title;
            info.startTime = this.startTime;
            info.duration = this.duration;
            info.id = this.id;
            info.addTime = this.addTime;
            info.isSquare = this.isSquare;
            info.type = this.type;
            return info;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }
}