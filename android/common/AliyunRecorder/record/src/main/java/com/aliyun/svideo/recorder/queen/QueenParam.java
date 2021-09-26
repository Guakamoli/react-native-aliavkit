package com.aliyun.svideo.recorder.queen;

import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.MakeupType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 默认参数
 */
public class QueenParam {

    public BasicBeautyRecord basicBeautyRecord = new BasicBeautyRecord();
    public static class BasicBeautyRecord {
        //基础美颜
        //美白开关
        public boolean enableSkinWhiting = true;
        public float skinWhitingParam = 0.30f; // 美白[0,1]
        //磨皮开关
        public boolean enableSkinBuffing = true;
        public float skinBuffingParam = 0.70f; // 磨皮[0,1]
        public float skinSharpenParam = 0.10f; // 锐化[0,1]

        //高级美颜
        public boolean enableFaceBuffing = true;
        public float faceBuffingPouchParam = 0.7f; //去眼袋[0,1]
        public float faceBuffingNasolabialFoldsParam = 0.6f; //去法令纹[0,1]
        public float faceBuffingWhiteTeeth = 0.35f; //白牙[0,1]
        public float faceBuffingBrightenEye = 0.2f; // 滤镜美妆：亮眼[0,1]
        public float faceBuffingSkinRed = 0.20f; // 滤镜美妆：红润[0,1]
        public float faceBuffingBlush = 0.20f; // 滤镜美妆：腮红[0,1]
        public float faceBuffingLipstick = 0.15f; // 滤镜美妆：口红[0,1]
        public float faceBuffingLipstickColorParam = -0.08f; // 滤镜美妆：口红色相[-0.5,0.5] 正红(-0.08)
        public float faceBuffingLipstickGlossParam = 1.0f; // 滤镜美妆：口红饱和度[0,1] 正红(1.0)
        public float faceBuffingLipstickBrightnessParam = 0.0f; // 滤镜美妆：口红明度[0,1] 正红(0.0)

        public float makeupAlphaWeight = 1f; // 滤镜美妆：美妆透明度权重
    }


    public LUTRecord lutRecord = new LUTRecord();
    public static class LUTRecord {
        public boolean lutEnable = true;
        public String lutPath = "lookups/lookup_27.png"; // 滤镜色卡路径
        public float lutParam = 0.8f; // 滤镜强度[0,1]
        @Override
        public String toString() {
            return "LUTRecord{lutEnable=" + lutEnable +", lutPath='" + lutPath + ", lutParam=" + lutParam +'}';
        }
    }


    public static class StickerRecord {
        public boolean stickerEnable = false;
        public String stickerPath = "sticker/tuanzhang"; // 贴纸路径
        public List<String> usingStickerPathList = new ArrayList<>(); //设置新的贴纸之后需要去掉旧的资源，这里做备份

        @Override
        public String toString() {
            return "StickerRecord{stickerEnable=" + stickerEnable +", stickerPath='" + stickerPath +'}';
        }
    }

    public StickerRecord stickerRecord = new StickerRecord();

    //美型
    public static class FaceShapeRecord {
        public boolean enableFaceShape = true;

        public float cutCheekParam = 0.5f; //颧骨[0,1]
        public float cutFaceParam = 0.5f; //削脸[0,1]
        public float thinFaceParam = 0.5f; //瘦脸[0,1]
        public float longFaceParam = 0.35f; //脸长[0,1]
        public float lowerJawParam = 0.0f; //下巴缩短[-1,1]
        public float higherJawParam = 0.0f; //下巴拉长[-1,1]
        public float thinJawParam = 0.3f; //瘦下巴[0,1]
        public float thinMandibleParam = 0.0f; //瘦下颌[0,1]
        public float bigEyeParam = 0.45f; //大眼[0,1]
        public float eyeAngle1Param = 0.0f; //眼角1[0,1]
        public float canthusParam = 0.0f; //眼距[-1,1]
        public float canthus1Param = 0.0f; //拉宽眼距[-1,1]
        public float eyeAngle2Param = 0.0f; //眼角2[-1,1]
        public float eyeTDAngleParam = 0.0f; //眼睛高度[-1,1]
        public float thinNoseParam = 0.4f; //瘦鼻[0,1]
        public float nosewingParam = 0.3f; //鼻翼[0,1]
        public float nasalHeightParam = 0.0f; //鼻长[-1,1]
        public float noseTipHeightParam = 0.0f; //鼻头长[-1,1]
        public float mouthWidthParam = 0.0f; //唇宽[-1,1]
        public float mouthSizeParam = 0.0f; //嘴唇大小[-1,1]
        public float mouthHighParam = 0.0f; //唇高[-1,1]
        public float philtrumParam = 0.0f; //人中[-1,1]
        public float hairLineParam = 0.0f; //发际线[-1,1]
        public float smileParam = 0.0f; //嘴角上扬(微笑)[-1,1]
        public float maxParam = 0.0f; //

        public static float formatFaceShapeParam(int param) {
            return param / 100.0f;
        }

        public static float formatReverseParam(int param) {
            return param / 100.0f * -1f;
        }

    }

    public static FaceShapeRecord sNoneFaceShapeRecord = new FaceShapeRecord();

    static {
        sNoneFaceShapeRecord.enableFaceShape = false;
    }

    //自定义美型
    public static FaceShapeRecord sCustomFaceShapeRecord = new FaceShapeRecord();

    static {
        sCustomFaceShapeRecord.enableFaceShape = true;
        //颧骨[0,1]
        sCustomFaceShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(10);
        //削脸[0,1]
        sCustomFaceShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(15);
        //瘦脸[0,1]
        sCustomFaceShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(50);
        //脸长[0,1]
        sCustomFaceShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(17);

        //下巴缩短[-1,1]
        sCustomFaceShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(7);
        //下巴拉长[-1,1]
        sCustomFaceShapeRecord.higherJawParam = FaceShapeRecord.formatReverseParam(7);
        //瘦下巴[0,1]
        sCustomFaceShapeRecord.thinJawParam = FaceShapeRecord.formatFaceShapeParam(30);
        //瘦下颌[0,1]
        sCustomFaceShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);

        //大眼[0,1]
        sCustomFaceShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(45);
        sCustomFaceShapeRecord.eyeAngle1Param = FaceShapeRecord.formatFaceShapeParam(0);
        sCustomFaceShapeRecord.canthusParam = FaceShapeRecord.formatFaceShapeParam(0);
        sCustomFaceShapeRecord.canthus1Param = FaceShapeRecord.formatFaceShapeParam(0);
        sCustomFaceShapeRecord.eyeAngle2Param = FaceShapeRecord.formatFaceShapeParam(0);
        sCustomFaceShapeRecord.eyeAngle2Param = FaceShapeRecord.formatFaceShapeParam(0);
        sCustomFaceShapeRecord.eyeTDAngleParam = FaceShapeRecord.formatFaceShapeParam(0);

        //瘦鼻
        sCustomFaceShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(40);
        //鼻翼[-1,1]
        sCustomFaceShapeRecord.nosewingParam = FaceShapeRecord.formatFaceShapeParam(30);
        //鼻长[-1,1]
        sCustomFaceShapeRecord.nasalHeightParam = FaceShapeRecord.formatFaceShapeParam(10);
        // 鼻头长[-1,1]
        sCustomFaceShapeRecord.noseTipHeightParam = FaceShapeRecord.formatFaceShapeParam(10);

        //唇宽[-1,1]
        sCustomFaceShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(18);
        sCustomFaceShapeRecord.mouthSizeParam = FaceShapeRecord.formatReverseParam(0);
        sCustomFaceShapeRecord.mouthHighParam = FaceShapeRecord.formatReverseParam(0);
        sCustomFaceShapeRecord.philtrumParam = FaceShapeRecord.formatReverseParam(0);
        //发际线[-1,1]
        sCustomFaceShapeRecord.hairLineParam = FaceShapeRecord.formatReverseParam(0);
        //嘴角上扬(微笑)[-1,1]
        sCustomFaceShapeRecord.smileParam = FaceShapeRecord.formatReverseParam(0);

    }

    // 优雅
    public static FaceShapeRecord sGraceFaceShapeRecord = new FaceShapeRecord();

    static {
        sGraceFaceShapeRecord.enableFaceShape = true;
        sGraceFaceShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(33);
        sGraceFaceShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(22);
        sGraceFaceShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(17);
        sGraceFaceShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(7);
        sGraceFaceShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(33);
        sGraceFaceShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sGraceFaceShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(18);
        sGraceFaceShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sGraceFaceShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }

    // 精致
    public static FaceShapeRecord sDelicateShapeRecord = new FaceShapeRecord();

    static {
        sDelicateShapeRecord.enableFaceShape = true;
        sDelicateShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(6);
        sDelicateShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(22);
        sDelicateShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(10);
        sDelicateShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(33);
        sDelicateShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(0);
        sDelicateShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sDelicateShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(0);
        sDelicateShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sDelicateShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }

    // 网红
    public static FaceShapeRecord sWangHongShapeRecord = new FaceShapeRecord();

    static {
        sWangHongShapeRecord.enableFaceShape = true;
        sWangHongShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(33);
        sWangHongShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(5);
        sWangHongShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(2);
        sWangHongShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(2);
        sWangHongShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(16);
        sWangHongShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sWangHongShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(12);
        sWangHongShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sWangHongShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }

    // 可爱
    public static FaceShapeRecord sCuteShapeRecord = new FaceShapeRecord();

    static {
        sCuteShapeRecord.enableFaceShape = true;
        sCuteShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(17);
        sCuteShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(22);
        sCuteShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(16);
        sCuteShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(-3);
        sCuteShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(33);
        sCuteShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sCuteShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(-8);
        sCuteShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sCuteShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }

    // 婴儿
    public static FaceShapeRecord sBabyFaceShapeRecord = new FaceShapeRecord();

    static {
        sBabyFaceShapeRecord.enableFaceShape = true;
        sBabyFaceShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(15);
        sBabyFaceShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(6);
        sBabyFaceShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(27);
        sBabyFaceShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(-10);
        sBabyFaceShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(16);
        sBabyFaceShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sBabyFaceShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(-8);
        sBabyFaceShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sBabyFaceShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }

    public FaceShapeRecord faceShapeRecord = sNoneFaceShapeRecord;

    public static class FaceMakeupRecord {
        public boolean enableFaceMakeup = true;
        /**
         * 美妆资源路径
         */
        public String[] makeupResourcePath = new String[MakeupType.kMakeupMax];

        /**
         * 美妆纹理混合模式
         */
        public int[] makeupBlendType = new int[MakeupType.kMakeupMax];

        /**
         * 美妆透明度
         */
        public float[] makeupAlpha = new float[MakeupType.kMakeupMax];

        public FaceMakeupRecord() {
//            //整装
//            makeupResourcePath[MakeupType.kMakeupWhole] = "makeup/mitao.png";
            //高光
            makeupResourcePath[MakeupType.kMakeupHighlight] = "makeup/highlight.png";
            //美瞳
            makeupResourcePath[MakeupType.kMakeupEyeball] = "makeup/eyeball.png";
            //口红
            makeupResourcePath[MakeupType.kMakeupMouth] = "makeup/mouth.png";
            //眼妆
            makeupResourcePath[MakeupType.kMakeupEyeBrow] = "makeup/eyebrow.png";
            //腮红
            makeupResourcePath[MakeupType.kMakeupBlush] = "makeup/blush_daizi.png";


//            //整装: 美妆纹理混合模式：正常
//            makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendNormal;
            //高光: 美妆纹理混合模式：叠加
            makeupBlendType[MakeupType.kMakeupHighlight] = BlendType.kBlendOverlay;
            //美瞳: 美妆纹理混合模式：正常
            makeupBlendType[MakeupType.kMakeupEyeball] = BlendType.kBlendNormal;
            //口红: 美妆纹理混合模式：正常
            makeupBlendType[MakeupType.kMakeupMouth] = BlendType.kBlendNormal;
            //眼妆: 美妆纹理混合模式：正常
            makeupBlendType[MakeupType.kMakeupEyeBrow] = BlendType.kBlendNormal;
            //腮红: 美妆纹理混合模式：正常
            makeupBlendType[MakeupType.kMakeupBlush] = BlendType.kBlendNormal;


//            //整装: 透明度：正常
//            makeupAlpha[MakeupType.kMakeupWhole] = 0.0f;
            //高光: 透明度：叠加
            makeupAlpha[MakeupType.kMakeupHighlight] = 0.20f;
            //美瞳: 透明度：正常
            makeupAlpha[MakeupType.kMakeupEyeball] = 0.10f;
            //口红: 透明度：正常
            makeupAlpha[MakeupType.kMakeupMouth] = 0.10f;
            //眼妆: 透明度：正常
            makeupAlpha[MakeupType.kMakeupEyeBrow] = 0.10f;
            //腮红: 透明度：正常
            makeupAlpha[MakeupType.kMakeupBlush] = 0.1f;

        }

    }

    public FaceMakeupRecord faceMakeupRecord = new FaceMakeupRecord();

    public String serialize() {
        return "";
    }

    public void deserialize() {

    }

    /**
     * 设置美颜参数
     */
    public static void setBeautyParam(BeautyParams beautyParams) {
        if (beautyParams == null) {
            return;
        }
        BasicBeautyRecord beautyRecord = QueenParamHolder.getQueenParam().basicBeautyRecord;
        //美颜开关
        beautyRecord.enableSkinWhiting = true;
        beautyRecord.enableFaceBuffing = true;
        beautyRecord.enableSkinBuffing = true;

        //美白 磨皮 锐化
        beautyRecord.skinWhitingParam = beautyParams.beautyWhite / 100;
        beautyRecord.skinBuffingParam = beautyParams.beautyBuffing / 100;
        beautyRecord.skinSharpenParam = beautyParams.beautySharpen / 100;

        //眼袋  法令纹  白牙
        beautyRecord.faceBuffingPouchParam = beautyParams.beautyPouch / 100;
        beautyRecord.faceBuffingNasolabialFoldsParam = beautyParams.beautyNasolabialFolds / 100;
        beautyRecord.faceBuffingWhiteTeeth = beautyParams.beautyWhiteTeeth / 100;

        //亮眼  红润  腮红
        beautyRecord.faceBuffingBrightenEye = beautyParams.beautyBrightenEye / 100;
        beautyRecord.faceBuffingSkinRed = beautyParams.beautyRuddy / 100;
        beautyRecord.faceBuffingBlush = beautyParams.beautyBlush / 100;

        //口红
        beautyRecord.faceBuffingLipstick = beautyParams.beautyLipstick / 100;
        beautyRecord.faceBuffingLipstickColorParam = beautyParams.beautyLipstickColor;
        beautyRecord.faceBuffingLipstickGlossParam = beautyParams.beautyLipstickGloss;
        beautyRecord.faceBuffingLipstickBrightnessParam = beautyParams.beautyLipstickBrightness;

        //美妆透明度
        beautyRecord.makeupAlphaWeight = beautyParams.makeupAlphaWeight;

        //滤镜强度
        QueenParamHolder.getQueenParam().lutRecord.lutParam = beautyParams.lutParam;
    }


    /**
     * 设置美型参数
     */
    public static void setBeautySkinParam(BeautyParams beautyParams) {
        if (beautyParams == null) {
            return;
        }
        FaceShapeRecord faceShapeRecord = QueenParamHolder.getQueenParam().faceShapeRecord;
        faceShapeRecord.cutCheekParam = beautyParams.cutCheekParam / 100F; //颧骨[0,1]
        faceShapeRecord.cutFaceParam = beautyParams.cutFaceParam/ 100F; //削脸[0,1]
        faceShapeRecord.thinFaceParam = beautyParams.beautySlimFace/ 100F; //瘦脸[0,1]
        faceShapeRecord.longFaceParam = beautyParams.longFaceParam/ 100F; //脸长[0,1]
        faceShapeRecord.lowerJawParam = beautyParams.lowerJawParam/ 100F; //下巴缩短[-1,1]
        faceShapeRecord.higherJawParam = beautyParams.higherJawParam/ 100F; //下巴拉长[-1,1]
        faceShapeRecord.thinJawParam = beautyParams.thinJawParam/ 100F; //瘦下巴[0,1]
        faceShapeRecord.thinMandibleParam = beautyParams.thinMandibleParam/ 100F; //瘦下颌[0,1]
        faceShapeRecord.bigEyeParam = beautyParams.beautyBigEye/ 100F; //大眼[0,1]
        faceShapeRecord.eyeAngle1Param = beautyParams.eyeAngle1Param/ 100F; //眼角1[0,1]
        faceShapeRecord.canthusParam = beautyParams.canthusParam/ 100F; //眼距[-1,1]
        faceShapeRecord.canthus1Param = beautyParams.canthus1Param/ 100F; //拉宽眼距[-1,1]
        faceShapeRecord.eyeAngle2Param = beautyParams.eyeAngle2Param/ 100F; //眼角2[-1,1]
        faceShapeRecord.eyeTDAngleParam = beautyParams.eyeTDAngleParam/ 100F; //眼睛高度[-1,1]
        faceShapeRecord.thinNoseParam = beautyParams.thinNoseParam/ 100F; //瘦鼻[0,1]
        faceShapeRecord.nosewingParam = beautyParams.nosewingParam/ 100F; //鼻翼[0,1]
        faceShapeRecord.nasalHeightParam = beautyParams.nasalHeightParam/ 100F; //鼻长[-1,1]
        faceShapeRecord.noseTipHeightParam = beautyParams.noseTipHeightParam/ 100F; //鼻头长[-1,1]
        faceShapeRecord.mouthWidthParam = beautyParams.mouthWidthParam/ 100F; //唇宽[-1,1]
        faceShapeRecord.mouthSizeParam = beautyParams.mouthSizeParam/ 100F; //嘴唇大小[-1,1]
        faceShapeRecord.mouthHighParam = beautyParams.mouthHighParam/ 100F; //唇高[-1,1]
        faceShapeRecord.philtrumParam = beautyParams.philtrumParam/ 100F; //人中[-1,1]

//        faceShapeRecord.thinFaceParam = beautyParams.beautySlimFace / 50.F;
//        faceShapeRecord.bigEyeParam = beautyParams.beautyBigEye / 50.F;
    }
}
