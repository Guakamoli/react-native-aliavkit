package com.aliyun.svideo.recorder.queen;

import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.models.BeautyFilterType;
import com.taobao.android.libqueen.models.BeautyParams;
import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.FaceShapeType;
import com.taobao.android.libqueen.models.MakeupType;

import java.util.Iterator;

public class QueenParamHolder {
    /**
     * 参数加权配置
     */
    private interface QueenParamWeight {
        float SKIN_WHITING = 1.0f;

        float SKIN_BUFFING = 1.0f;
        float SKIN_SHARPEN = 1.0f;

        float FACE_BUFFING_NASOLABIALFOLDS = 1.0f;
        float FACE_BUFFING_POUCH = 1.0f;
        float FACE_BUFFING_WHITE_TEETH = 1.0f;
        float FACE_BUFFING_LIPSTICK = 1.0f;
        float FACE_BUFFING_BLUSH = 1.0f;

        float FACE_SHAPE_PARAM = 1.0f;

        float FACE_MAKEUP_ALPHA = 1.0f;

        float FACE_LUT_PARAM = 1.0f;
    }

    private static QueenParam sQueenParam;

    public static QueenParam getQueenParam() {
        if (null == sQueenParam) {
            sQueenParam = new QueenParam();
        }
        return sQueenParam;
    }

    public static void writeParamToEngine(QueenEngine mediaChainEngine) {
        if (null != mediaChainEngine) {
            QueenParam.BasicBeautyRecord basicBeautyRecord = getQueenParam().basicBeautyRecord;
            //磨皮开关
            mediaChainEngine.enableBeautyType(BeautyFilterType.kSkinBuffing, basicBeautyRecord.enableSkinBuffing);
            //高级美颜开关
            mediaChainEngine.enableBeautyType(BeautyFilterType.kFaceBuffing, basicBeautyRecord.enableFaceBuffing);
            //美白开关
            mediaChainEngine.enableBeautyType(BeautyFilterType.kSkinWhiting, basicBeautyRecord.enableSkinWhiting);
            // 美型开关，其中第二个参数是功能开关，第三个参数为调试开关
            mediaChainEngine.enableBeautyType(BeautyFilterType.kFaceShape, getQueenParam().faceShapeRecord.enableFaceShape, false);
            //色相饱和度明度开关
            mediaChainEngine.enableBeautyType(BeautyFilterType.kHSV, true);
            //背景模糊开关
//            mediaChainEngine.enableBeautyType(BeautyFilterType.kBTBackgroundProcess, true);

            //普通美颜
            //美白 [0,1]  默认 36
            mediaChainEngine.setBeautyParam(BeautyParams.kBPSkinWhitening, basicBeautyRecord.skinWhitingParam);
            //磨皮 [0,1] 默认 40
            mediaChainEngine.setBeautyParam(BeautyParams.kBPSkinBuffing, basicBeautyRecord.skinBuffingParam);
            //锐化 [0,1] 默认 10
            mediaChainEngine.setBeautyParam(BeautyParams.kBPSkinSharpen, basicBeautyRecord.skinSharpenParam);

            //高级美颜
            //去眼袋[0,1]   默认 100
            mediaChainEngine.setBeautyParam(BeautyParams.kBPPouch, basicBeautyRecord.faceBuffingPouchParam);
            //去法令纹[0,1]  默认 100
            mediaChainEngine.setBeautyParam(BeautyParams.kBPNasolabialFolds, basicBeautyRecord.faceBuffingNasolabialFoldsParam);
            //白牙[0,1]     默认 100
            mediaChainEngine.setBeautyParam(BeautyParams.kBPWhiteTeeth, basicBeautyRecord.faceBuffingWhiteTeeth); //白牙[0,1]
            //滤镜美妆：亮眼[0,1] 默认 50
            mediaChainEngine.setBeautyParam(BeautyParams.kBPBrightenEye, basicBeautyRecord.faceBuffingBrightenEye);
            // 滤镜美妆：红润[0,1] 默认 15
            mediaChainEngine.setBeautyParam(BeautyParams.kBPSkinRed, basicBeautyRecord.faceBuffingSkinRed);
            //滤镜美妆：腮红[0,1] 默认 20
            mediaChainEngine.setBeautyParam(BeautyParams.kBPBlush, basicBeautyRecord.faceBuffingBlush);
            //滤镜美妆：口红[0,1] 默认 20
            mediaChainEngine.setBeautyParam(BeautyParams.kBPLipstick, basicBeautyRecord.faceBuffingLipstick);
            // 滤镜美妆：口红色相[-0.5,0.5]，需配合饱和度、明度使用，参考颜色如下：土红(-0.125)、粉红(-0.1)、复古红(0.0)、紫红(-0.2)、正红(-0.08)、橘红(0.0)、紫色(-0.42)、橘色(0.125)、黄色(0.25)
            mediaChainEngine.setBeautyParam(BeautyParams.kBPLipstickColorParam, basicBeautyRecord.faceBuffingLipstickColorParam);
            // 滤镜美妆：口红饱和度[0,1]，需配合色相、明度使用，参考颜色如下：土红(0.25)、粉红(0.125)、复古红(1.0)、紫红(0.35)、正红(1.0)、橘红(0.35)、紫色(0.35)、橘色(0.25)、黄色(0.45)
            mediaChainEngine.setBeautyParam(BeautyParams.kBPLipstickGlossParam, basicBeautyRecord.faceBuffingLipstickGlossParam);
            // 滤镜美妆：口红明度[0,1]，需配合色相、饱和度使用，参考颜色如下：土红(0.4)、粉红(0.0)、复古红(0.2)、紫红(0.0)、正红(0.0)、橘红(0.0)、紫色(0.0)、橘色(0.0)、黄色(0.0)
            mediaChainEngine.setBeautyParam(BeautyParams.kBPLipstickBrightnessParam, basicBeautyRecord.faceBuffingLipstickBrightnessParam);

            // 美型
            QueenParam.FaceShapeRecord faceShapeRecord = getQueenParam().faceShapeRecord;
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCutCheek, faceShapeRecord.cutCheekParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCutFace, faceShapeRecord.cutFaceParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinFace, faceShapeRecord.thinFaceParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeLongFace, faceShapeRecord.longFaceParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeLowerJaw, faceShapeRecord.lowerJawParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeHigherJaw, faceShapeRecord.higherJawParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinJaw, faceShapeRecord.thinJawParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinMandible, faceShapeRecord.thinMandibleParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeBigEye, faceShapeRecord.bigEyeParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeEyeAngle1, faceShapeRecord.eyeAngle1Param);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCanthus, faceShapeRecord.canthusParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCanthus1, faceShapeRecord.canthus1Param);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeEyeAngle2, faceShapeRecord.eyeAngle2Param);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeEyeTDAngle, faceShapeRecord.eyeTDAngleParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinNose, faceShapeRecord.thinNoseParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeNosewing, faceShapeRecord.nosewingParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeNasalHeight, faceShapeRecord.nasalHeightParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeNoseTipHeight, faceShapeRecord.noseTipHeightParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeMouthWidth, faceShapeRecord.mouthWidthParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeMouthSize, faceShapeRecord.mouthSizeParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeMouthHigh, faceShapeRecord.mouthHighParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typePhiltrum, faceShapeRecord.philtrumParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeHairLine, faceShapeRecord.hairLineParam);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeSmile, faceShapeRecord.smileParam);


            float alphaWeight = basicBeautyRecord.makeupAlphaWeight;
            //美妆开关 第二个参数是开关，第三个参数是调试开关
            mediaChainEngine.enableBeautyType(BeautyFilterType.kMakeup, getQueenParam().faceMakeupRecord.enableFaceMakeup && alphaWeight > 0);
            if (getQueenParam().faceMakeupRecord.enableFaceMakeup && alphaWeight > 0) {
                for (int makeupType = 0; makeupType < MakeupType.kMakeupMax; makeupType++) {
                    String makeUpResourcePath = getQueenParam().faceMakeupRecord.makeupResourcePath[makeupType];
                    int blendType = getQueenParam().faceMakeupRecord.makeupBlendType[makeupType];
                    if (!TextUtils.isEmpty(makeUpResourcePath)) {
                        String[] path = new String[]{getQueenParam().faceMakeupRecord.makeupResourcePath[makeupType]};
                        // 第一个参数makeupType是类型，眼妆就是 MakeupType.kMakeupEyeBrow
                        // 第二个参数path是资源路径，
                        // 第三个参数blendType就是 BlendType.kBlendNormal
                        //  第四个参数固定15即可；
                        mediaChainEngine.setMakeupImage(makeupType, path, blendType, 15);

                        float alpha = getQueenParam().faceMakeupRecord.makeupAlpha[makeupType] * alphaWeight;
                        //type - 美妆类型; alpha - 美妆透明度;  maleAlpha - 保留参数（男性美妆透明度）：暂时不支持
                        mediaChainEngine.setMakeupAlpha(makeupType, alpha, alpha);
                    } else {
                        String[] path = new String[]{};
                        mediaChainEngine.setMakeupImage(makeupType, path, BlendType.kBlendNormal, 15);
                        mediaChainEngine.setMakeupAlpha(makeupType, 0, 0);
                    }
                }
            }

            // 滤镜功能开关
            mediaChainEngine.enableBeautyType(BeautyFilterType.kLUT, getQueenParam().lutRecord.lutEnable);
            if (getQueenParam().lutRecord.lutEnable) {
                mediaChainEngine.setFilter(getQueenParam().lutRecord.lutPath); //设置滤镜
                mediaChainEngine.setBeautyParam(BeautyParams.kBPLUT, getQueenParam().lutRecord.lutParam); //滤镜强度
            }

            // 贴纸
            boolean enableSticker = getQueenParam().stickerRecord.stickerEnable;
            String stickerPath = getQueenParam().stickerRecord.stickerPath;
            if (enableSticker) {
                if (!TextUtils.isEmpty(stickerPath) && !getQueenParam().stickerRecord.usingStickerPathList.contains(stickerPath)) {
                    mediaChainEngine.addMaterial(stickerPath);
                    getQueenParam().stickerRecord.usingStickerPathList.add(stickerPath);
                }
            }
            Iterator<String> iterator = getQueenParam().stickerRecord.usingStickerPathList.iterator();
            while (iterator.hasNext()) {
                String usingStickerPath = iterator.next();
                if (!enableSticker || !TextUtils.equals(usingStickerPath, stickerPath)) {
                    mediaChainEngine.removeMaterial(usingStickerPath);
                    iterator.remove();
                }
            }
        }
    }

    public static void relaseQueenParams() {
        getQueenParam().stickerRecord.usingStickerPathList.clear();
    }

}
