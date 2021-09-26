package com.aliyun.svideo.recorder.view.effects.face;

import android.content.Context;

import androidx.annotation.IntDef;

import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.aliyun.svideo.recorder.faceunity.FaceUnityManager;
import com.aliyun.svideo.recorder.queen.QueenManager;
import com.aliyun.svideo.recorder.queen.QueenParam;
import com.aliyun.svideo.recorder.queen.QueenParamHolder;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;

public class BeautyService {

    public static final int BEAUTY_FACE = 0;
    public static final int BEAUTY_SKIN = 1;

    @IntDef({BEAUTY_FACE, BEAUTY_SKIN})
    public @interface BeautyType {
    }

    private FaceUnityManager faceUnityManager;
    private QueenManager queenManager;

    private BeautyParams beautyParams;
    private RenderingMode renderingMode = RenderingMode.Queen;

    /**
     * FaceUnity
     * normal跟advanced分开，当是normal的时候不需要初始化BeautyParams
     */
    public BeautyParams bindFaceUnity(Context context, FaceUnityManager faceUnityManager) {
        renderingMode = RenderingMode.FaceUnity;
        this.faceUnityManager = faceUnityManager;
        return initBeautyParam(context);
    }

    public void bindNormalFaceUnity( FaceUnityManager faceUnityManager) {
        renderingMode = RenderingMode.FaceUnity;
        this.faceUnityManager = faceUnityManager;

    }
    /**
     * Race
     * normal跟advanced分开，当是normal的时候不需要初始化BeautyParams
     */
    public BeautyParams bindQueen(Context context, QueenManager queenManager) {
        renderingMode = RenderingMode.Queen;
        this.queenManager = queenManager;
        return initBeautyParam(context);
    }

    public void bindNormalRace( QueenManager queenManager) {
        renderingMode = RenderingMode.Queen;
        this.queenManager = queenManager;

    }

    private BeautyParams initBeautyParam(Context context) {

        //高级美颜等级
        int beautyFaceLevel = SharedPreferenceUtils.getBeautyFaceLevel(context);
        //美肌等级
        int beautySkinLevel = SharedPreferenceUtils.getBeautySkinLevel(context);

        float beautyfaceValue = checkBeautyParam(beautyFaceLevel) / 100;
        float beautySkinValue = checkBeautyParam(beautySkinLevel) / 100;

        beautyParams = new BeautyParams();
        beautyParams.beautyBuffing = (int) (beautyfaceValue * 10);
        if (renderingMode == RenderingMode.FaceUnity) {
            if (faceUnityManager != null) {
                faceUnityManager
                        .setFaceBeautyWhite(beautyfaceValue)
                        .setFaceBeautyRuddy(beautyfaceValue)
                        .setFaceBeautyBuffing(beautyfaceValue * 10 * 0.6f)
                        .setFaceBeautyBigEye(beautySkinValue * 1.5f)
                        .setFaceBeautySlimFace(beautySkinValue);
            }
        } else if (renderingMode == RenderingMode.Queen) {
            if (queenManager != null) {
                QueenParamHolder.getQueenParam().basicBeautyRecord.enableSkinWhiting = true;
                QueenParamHolder.getQueenParam().basicBeautyRecord.skinWhitingParam = beautyfaceValue;
                QueenParamHolder.getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
                QueenParamHolder.getQueenParam().basicBeautyRecord.skinSharpenParam = beautyfaceValue;
                QueenParamHolder.getQueenParam().basicBeautyRecord.skinBuffingParam = beautyfaceValue;
                QueenParamHolder.getQueenParam().faceShapeRecord.enableFaceShape = true;
                QueenParamHolder.getQueenParam().faceShapeRecord.bigEyeParam = beautySkinValue * 2;
                QueenParamHolder.getQueenParam().faceShapeRecord.thinFaceParam = beautySkinValue * 2;

                queenManager.addTask();
            }
        }
        return beautyParams;
    }

    private float checkBeautyParam(int level) {
        BeautyLevel beautyLevel;
        switch (level) {
            case 0:
                beautyLevel = BeautyLevel.BEAUTY_LEVEL_ZERO;
                break;
            case 1:
                beautyLevel = BeautyLevel.BEAUTY_LEVEL_ONE;
                break;
            case 2:
                beautyLevel = BeautyLevel.BEAUTY_LEVEL_TWO;
                break;
            case 3:
                beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
                break;
            case 4:
                beautyLevel = BeautyLevel.BEAUTY_LEVEL_FOUR;
                break;
            case 5:
                beautyLevel = BeautyLevel.BEAUTY_LEVEL_FIVE;
                break;
            default:
                beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
                break;
        }
        return beautyLevel.getValue();
    }


    public void setBeautyParam(BeautyParams beautyParams, @BeautyType int beautyType) {
        if (beautyParams == null) {
            throw new IllegalArgumentException("beautyParams is null");
        }
        this.beautyParams = beautyParams;
        if (renderingMode == RenderingMode.FaceUnity) {
            if (beautyType == BEAUTY_FACE) {
                faceUnityManager
                        .setFaceBeautyWhite(beautyParams.beautyWhite / 100)
                        .setFaceBeautyRuddy(beautyParams.beautyRuddy / 100)
                        .setFaceBeautyBuffing(beautyParams.beautyBuffing / 10 * 0.6f);
            } else {
                faceUnityManager
                        .setFaceBeautySlimFace(beautyParams.beautySlimFace / 100 * 1.5f)
                        .setFaceBeautyBigEye(beautyParams.beautyBigEye / 100 * 1.5f);
            }
        } else if (renderingMode == RenderingMode.Queen) {
            if (beautyType == BEAUTY_FACE) {
                //美颜,重新设置美颜参数
                QueenParam.setBeautyParam(beautyParams);
                queenManager.addTask();
            } else {
                //美肌
                QueenParam.setBeautySkinParam(beautyParams);
                queenManager.addTask();
            }
        }

    }

    public BeautyParams getBeautyParam() {
        return beautyParams;
    }

    public void changeBeautyMode(BeautyMode beautyMode) {

        if (beautyMode == BeautyMode.Normal) {

        } else {

        }
    }

    public void unbindFaceUnity() {
        faceUnityManager = null;
        queenManager = null;
    }

    public void saveBeautyMode(Context context, BeautyMode beautyMode) {
        SharedPreferenceUtils.setBeautyMode(context, beautyMode);
    }


    public void saveSelectParam(Context context, int beautyNormalFacePosition, int beautyFacePosition,
                                int beautySkinPosition, int beautySharpPosition) {
        SharedPreferenceUtils.setBeautyNormalFaceLevel(context, beautyNormalFacePosition);
        SharedPreferenceUtils.setBeautyFaceLevel(context, beautyFacePosition);
        SharedPreferenceUtils.setBeautySkinLevel(context, beautySkinPosition);
        SharedPreferenceUtils.setBeautyShapeLevel(context, beautySharpPosition);
    }
}
