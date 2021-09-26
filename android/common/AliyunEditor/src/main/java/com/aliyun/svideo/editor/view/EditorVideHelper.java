package com.aliyun.svideo.editor.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.aliyun.svideosdk.common.AliyunIClipConstructor;
import com.aliyun.svideosdk.common.struct.common.AliyunClip;
import com.aliyun.svideosdk.common.struct.common.AliyunImageClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoParam;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory;
import com.aliyun.svideosdk.importer.AliyunIImport;
import com.aliyun.svideosdk.importer.impl.AliyunImportCreator;
import com.blankj.utilcode.util.FileUtils;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditorVideHelper {


    public static List<AliyunClip> getInitClipList(AliyunIEditor aliyunIEditor) {
        AliyunIClipConstructor clipConstructor = aliyunIEditor.getSourcePartManager();
        if (clipConstructor == null) {
            return null;
        }
        List<AliyunClip> clipList = clipConstructor.getAllClips();
        return clipList;
    }


    public static List<AliyunClip> setVideoTimes(AliyunIEditor aliyunIEditor, long videoStartTime, long videoEntTime) {
        if (aliyunIEditor == null) {
            return null;
        }
        if (videoStartTime == 0 && videoEntTime == 0) {
            return null;
        }
//        aliyunIEditor.stop();
//        aliyunIEditor.saveEffectToLocal();

        AliyunIClipConstructor clipConstructor = aliyunIEditor.getSourcePartManager();
        if (clipConstructor == null) {
            return null;
        }

        List<Long> durationList = new ArrayList<>();
        List<AliyunClip> aliYunClipList = new ArrayList<>();
        List<AliyunClip> clipList = clipConstructor.getAllClips();
        aliYunClipList.addAll(clipList);
        Iterator<AliyunClip> iter = clipList.iterator();
        long videoAllTime = 0;
        while (iter.hasNext()) {
            AliyunClip aliyunClip = iter.next();
            if (aliyunClip instanceof AliyunVideoClip) {
                AliyunVideoClip videoClip = (AliyunVideoClip) aliyunClip;
                durationList.add(videoClip.getEndTime());
                long duration = videoClip.getEndTime() * 1000;
                if (videoStartTime >= videoAllTime && videoStartTime <= videoAllTime + duration) {
                    //开始时间在这段视频内
                    long startTime = (videoStartTime - videoAllTime) / 1000;
                    videoClip.setStartTime(startTime);
                }
                if (videoEntTime >= videoAllTime && videoEntTime <= videoAllTime + duration) {
                    long endTime = (videoEntTime - videoAllTime) / 1000;
                    videoClip.setEndTime(endTime);
                }
                //这个视频被裁剪掉了，不进行合成
                if (videoStartTime > videoAllTime + duration || videoEntTime < videoAllTime) {
                    iter.remove();
                } else {
//                    mImport.addMediaClip(videoClip);
                }
                videoAllTime += duration;

            }
        }

//        for (AliyunClip clip : clipList) {
//            if (clip instanceof AliyunVideoClip) {
//                AliyunVideoClip videoClip = (AliyunVideoClip) clip;
//                Log.e("CCC", "getStartTime = " + videoClip.getStartTime() + "；getEndTime = " + videoClip.getEndTime());
//            }
//        }


        clipConstructor.updateAllClips(clipList);
        aliyunIEditor.saveEffectToLocal();
        aliyunIEditor.applySourceChange();
        for (int i = 0; i < aliYunClipList.size(); i++) {
            AliyunClip clip = aliYunClipList.get(i);
            if (clip instanceof AliyunVideoClip) {
                AliyunVideoClip videoClip = (AliyunVideoClip) clip;
                if (durationList != null && durationList.size() > i) {
                    Long duration = durationList.get(i);
                    videoClip.setStartTime(0);
                    if (duration != null) {
                        videoClip.setEndTime(duration);
                    }
                }
//                Log.e("CCC", "原版 getStartTime = " + videoClip.getStartTime() + "；getEndTime = " + videoClip.getEndTime());
            }
        }
        return aliYunClipList;
    }


    public static void resetVideoTimes(AliyunIEditor aliyunIEditor, long startTime, long endTime) {
        if (aliyunIEditor == null) {
            return;
        }
        AliyunIClipConstructor clipConstructor = aliyunIEditor.getSourcePartManager();
        if (clipConstructor == null) {
            return;
        }
        List<AliyunClip> clipList = clipConstructor.getAllClips();
        for (AliyunClip clip : clipList) {
            if (clip instanceof AliyunVideoClip) {
                AliyunVideoClip videoClip = (AliyunVideoClip) clip;
                videoClip.setStartTime(startTime / 1000);
                videoClip.setDuration((endTime-startTime)/1000);
                videoClip.setEndTime(endTime / 1000);
            }
        }
//       boolean isPlaying = aliyunIEditor.isPlaying();
//        aliyunIEditor.stop();
//        aliyunIEditor.saveEffectToLocal();
        aliyunIEditor.applySourceChange();
//        aliyunIEditor.seek(0);
//        if (isPlaying) {
//            aliyunIEditor.play();
//        }
    }


}
