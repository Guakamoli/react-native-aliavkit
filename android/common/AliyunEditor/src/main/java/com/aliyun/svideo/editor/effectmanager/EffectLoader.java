/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.content.Context;
import android.database.Cursor;

import com.aliyun.svideo.base.Form.AnimationEffectForm;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.Form.IMVForm;
import com.aliyun.svideo.base.Form.ResourceForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.http.HttpCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EffectLoader {

    public static final int ASPECT_1_1 = 1;
    public static final int ASPECT_4_3 = 2;
    public static final int ASPECT_9_16 = 3;
    public EffectService mService = new EffectService();

    public interface LoadCallback<T> {
        void onLoadCompleted(List<T> localInfos, List<T> remoteInfos, Throwable e);
    }

    public List<FileDownloaderModel> loadLocalEffect(int effectType) {
        List<FileDownloaderModel> localPasters = new ArrayList<FileDownloaderModel>();
        List<String> selectedColumns = new ArrayList<String>();
        selectedColumns.add(FileDownloaderModel.ICON);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION_EN);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.ISNEW);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.NAME_EN);
        selectedColumns.add(FileDownloaderModel.PREVIEW);
        selectedColumns.add(FileDownloaderModel.SORT);

        selectedColumns.add(FileDownloaderModel.PREVIEWMP4);
        selectedColumns.add(FileDownloaderModel.PREVIEWPIC);
        selectedColumns.add(FileDownloaderModel.KEY);
        selectedColumns.add(FileDownloaderModel.DURATION);
        //selectedColumns.add(FileDownloaderModel.SUBTYPE);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(effectType));
        Cursor cursor = DownloaderManager.getInstance()
                .getDbController().getResourceColumns(conditionMap, selectedColumns);

        while (cursor.moveToNext()) {
            FileDownloaderModel paster = new FileDownloaderModel();
            paster.setIcon(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            String description = cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION));
            if ("assets".equals(description)) {
                continue;
            } else {
                paster.setDescription(description);
                paster.setDescriptionEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION_EN)));
            }
            paster.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            paster.setIsnew(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ISNEW)));
            paster.setLevel(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.LEVEL)));
            paster.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            paster.setNameEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME_EN)));
            paster.setPreview(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEW)));
            paster.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));

            paster.setPreviewmp4(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWMP4)));
            paster.setPreviewpic(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWPIC)));
            paster.setKey(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.KEY)));
            paster.setDuration(cursor.getLong(cursor.getColumnIndex(FileDownloaderModel.DURATION)));
            //paster.setSubtype(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SUBTYPE)));

            localPasters.add(paster);
        }
        cursor.close();
        return localPasters;
    }

    public int loadAllPaster(Context context, final LoadCallback<ResourceForm> callback) {

        mService.loadEffectPaster(context.getPackageName(), new HttpCallback<List<ResourceForm>>() {
            @Override
            public void onSuccess(List<ResourceForm> result) {
                if (callback != null) {
                    List<ResourceForm> localPasters = loadLocalPaster();
                    List<Integer> localIds = null;
                    if (localPasters != null && localPasters.size() > 0) {
                        localIds = new ArrayList<Integer>(localPasters.size());
                        for (ResourceForm paster : localPasters) {
                            localIds.add(paster.getId());
                        }
                    }
                    if (localIds != null && localIds.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            if (localIds.contains(result.get(i).getId())) {
                                result.remove(i);
                                i--;
                            }
                        }
                    }

                    callback.onLoadCompleted(localPasters, result, null);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                List<ResourceForm> localPasters = loadLocalPaster();
                if (callback != null) {
                    callback.onLoadCompleted(localPasters, null, e);
                }
            }
        });
        return 0;
    }

    public List<ResourceForm> loadLocalPaster() {
        List<ResourceForm> localPasters = new ArrayList<ResourceForm>();
        List<String> selectedColumns = new ArrayList<String>();
        selectedColumns.add(FileDownloaderModel.ICON);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION_EN);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.ISNEW);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.NAME_EN);
        selectedColumns.add(FileDownloaderModel.PREVIEW);
        selectedColumns.add(FileDownloaderModel.SORT);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(EffectService.EFFECT_PASTER));
        Cursor cursor = DownloaderManager.getInstance()
                .getDbController().getResourceColumns(conditionMap, selectedColumns);

        while (cursor.moveToNext()) {
            ResourceForm paster = new ResourceForm();
            paster.setIcon(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            String description = cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION));
            if ("assets".equals(description)) {
                continue;
            } else {
                paster.setDescription(description);
                paster.setDescriptionEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION_EN)));
            }
            paster.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            paster.setIsNew(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ISNEW)));
            paster.setLevel(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.LEVEL)));
            paster.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            paster.setNameEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME_EN)));
            paster.setPreviewUrl(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEW)));
            paster.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));
            localPasters.add(paster);
        }
        cursor.close();
        return localPasters;
    }

    public int loadAllMV(Context context, final LoadCallback<IMVForm> callback) {

        mService.loadEffectMv(context.getPackageName(), new HttpCallback<List<IMVForm>>() {
            @Override
            public void onSuccess(List<IMVForm> result) {
                if (callback != null) {

                    List<IMVForm> localMvs = loadLocalMV();
                    List<Integer> localIds = null;
                    if (localMvs != null && localMvs.size() > 0) {
                        localIds = new ArrayList<Integer>(localMvs.size());
                        for (IMVForm mv : localMvs) {
                            localIds.add(mv.getId());
                        }
                    }
                    if (localIds != null && localIds.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            if (localIds.contains(result.get(i).getId())) {
                                result.remove(i);
                                i--;
                            }
                        }
                    }

                    callback.onLoadCompleted(localMvs, result, null);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                List<IMVForm> localMvs = loadLocalMV();
                if (callback != null) {
                    callback.onLoadCompleted(localMvs, null, e);
                }
            }
        });
        return 0;
    }


    public List<IMVForm> loadLocalMV() {
        List<IMVForm> localMvs = new ArrayList<IMVForm>();
        List<String> selectedColumns = new ArrayList<String>();
        selectedColumns.add(FileDownloaderModel.TAG);
        selectedColumns.add(FileDownloaderModel.CAT);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.SUBTYPE);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.NAME_EN);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION);
        selectedColumns.add(FileDownloaderModel.PREVIEWPIC);
        selectedColumns.add(FileDownloaderModel.PREVIEWMP4);
        selectedColumns.add(FileDownloaderModel.SORT);
        selectedColumns.add(FileDownloaderModel.DURATION);
        selectedColumns.add(FileDownloaderModel.KEY);
        selectedColumns.add(FileDownloaderModel.ICON);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(EffectService.EFFECT_MV));
        Cursor cursor = DownloaderManager.getInstance()
                .getDbController().getResourceColumns(conditionMap, selectedColumns);
        List<Integer> localIds = new ArrayList();

        while (cursor.moveToNext()) {
            IMVForm mv = new IMVForm();
            mv.setTag(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.TAG)));
            mv.setCat(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.CAT)));
            mv.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            String description = cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION));
            if ("assets".equals(description)) {
                continue;
            }
            mv.setDuration(cursor.getLong(cursor.getColumnIndex(FileDownloaderModel.DURATION)));
            mv.setLevel(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.LEVEL)));
            mv.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            mv.setNameEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME_EN)));
            mv.setPreviewPic(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWPIC)));
            mv.setPreviewMp4(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWMP4)));
            mv.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));
            mv.setType(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SUBTYPE)));
            mv.setKey(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.KEY)));
            mv.setIcon(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            localMvs.add(mv);
            localIds.add(mv.getId());
        }
        cursor.close();
        return localMvs;
    }

    public int loadAllAnimationFilter(Context context, final LoadCallback<AnimationEffectForm> callback) {

        mService.loadAnimationFilter(context.getPackageName(), new HttpCallback<List<AnimationEffectForm>>() {
            @Override
            public void onSuccess(List<AnimationEffectForm> result) {
                if (callback != null) {

                    List<AnimationEffectForm> localFilters = loadLocalAnimationEffect(EffectService.ANIMATION_FILTER);
                    List<Integer> localIds = null;
                    if (localFilters != null && localFilters.size() > 0) {
                        localIds = new ArrayList<Integer>(localFilters.size());
                        for (AnimationEffectForm af : localFilters) {
                            localIds.add(af.getId());
                        }
                    }
                    if (localIds != null && localIds.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            if (localIds.contains(result.get(i).getId())) {
                                result.remove(i);
                                i--;
                            }
                        }
                    }

                    callback.onLoadCompleted(localFilters, result, null);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                List<AnimationEffectForm> localFilters = loadLocalAnimationEffect(EffectService.ANIMATION_FILTER);
                if (callback != null) {
                    callback.onLoadCompleted(localFilters, null, e);
                }
            }
        });
        return 0;
    }

    /**
     * 查询数据库获取特效生态化滤镜、转场资源
     * @param type {@link EffectService#ANIMATION_FILTER,EffectService#EFFECT_TRANSITION}
     * @return AnimationEffectForm
     */
    public List<AnimationEffectForm> loadLocalAnimationEffect(int type) {
        List<AnimationEffectForm> localafs = new ArrayList<AnimationEffectForm>();
        List<String> selectedColumns = new ArrayList<String>();
        selectedColumns.add(FileDownloaderModel.TAG);
        selectedColumns.add(FileDownloaderModel.CAT);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.SUBTYPE);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.NAME_EN);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION);
        selectedColumns.add(FileDownloaderModel.PREVIEWPIC);
        selectedColumns.add(FileDownloaderModel.PREVIEWMP4);
        selectedColumns.add(FileDownloaderModel.SORT);
        selectedColumns.add(FileDownloaderModel.DURATION);
        selectedColumns.add(FileDownloaderModel.KEY);
        selectedColumns.add(FileDownloaderModel.ICON);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(type));
        Cursor cursor = DownloaderManager.getInstance()
                .getDbController().getResourceColumns(conditionMap, selectedColumns);
        List<Integer> localIds = new ArrayList();

        while (cursor.moveToNext()) {
            AnimationEffectForm af = new AnimationEffectForm();
            af.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            String description = cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION));
            if ("assets".equals(description)) {
                continue;
            }
            af.setDuration(cursor.getLong(cursor.getColumnIndex(FileDownloaderModel.DURATION)));
            af.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            af.setNameEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME_EN)));
            af.setPreviewImageUrl(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWPIC)));
            af.setPreviewMediaUrl(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWMP4)));
            af.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));
            af.setType(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SUBTYPE)));
            af.setDescription(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.KEY)));
            af.setIconUrl(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            localafs.add(af);
            localIds.add(af.getId());
        }
        cursor.close();
        return localafs;
    }

    public void loadAllTransition(Context context, final LoadCallback<AnimationEffectForm> callback) {
        mService.loadTransitonEffect(context.getPackageName(), new HttpCallback<List<AnimationEffectForm>>() {
            @Override
            public void onSuccess(List<AnimationEffectForm> result) {
                if (callback != null) {

                    List<AnimationEffectForm> localFilters = loadLocalAnimationEffect(EffectService.EFFECT_TRANSITION);
                    List<Integer> localIds = null;
                    if (localFilters != null && localFilters.size() > 0) {
                        localIds = new ArrayList<Integer>(localFilters.size());
                        for (AnimationEffectForm af : localFilters) {
                            localIds.add(af.getId());
                        }
                    }
                    if (localIds != null && localIds.size() > 0) {
                        for (int i = 0; i < result.size(); i++) {
                            if (localIds.contains(result.get(i).getId())) {
                                result.remove(i);
                                i--;
                            }
                        }
                    }

                    callback.onLoadCompleted(localFilters, result, null);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                List<AnimationEffectForm> localFilters = loadLocalAnimationEffect(EffectService.EFFECT_TRANSITION);
                if (callback != null) {
                    callback.onLoadCompleted(localFilters, null, e);
                }
            }
        });
    }
}
