package com.aliyun.svideo.base.widget.beauty;

/**
 * Created by Akira on 2018/6/20.
 */
public class BeautyParams implements Cloneable {

    /**
     * 美白
     */
    public float beautyWhite = 30;
    /**
     * 磨皮
     */
    public float beautyBuffing = 70;
    /**
     * 锐化
     */
    public float beautySharpen = 10;

    /**
     * 眼袋
     */
    public float beautyPouch = 70;
    /**
     * 法令纹
     */
    public float beautyNasolabialFolds = 60;
    /**
     * 白牙
     */
    public float beautyWhiteTeeth = 35;

    /**
     * 亮眼
     */
    public float beautyBrightenEye = 20;
    /**
     * 红润
     */
    public float beautyRuddy = 20;
    /**
     * 腮红
     */
    public float beautyBlush = 20;


    /**
     * 口红
     */
    public float beautyLipstick = 15;
    public float beautyLipstickColor = -0.08f;
    public float beautyLipstickGloss = 1.0f;
    public float beautyLipstickBrightness = 0.0f;


    /**
     * 颧骨[0,1]
     */
    public float cutCheekParam = 50;
    /**
     * 削脸[0,1]
     */
    public float cutFaceParam = 50;
    /**
     * 瘦脸[0,1]
     */
    public float beautySlimFace = 50;
    /**
     * 脸长[0,1]
     */
    public float longFaceParam = 35;

    /**
     * 下巴缩短[-1,1]  下巴拉长[-1,1]  瘦下巴[0,1]  瘦下颌[0,1]
     */
    public float lowerJawParam = 0;
    public float higherJawParam = 0;
    public float thinJawParam = 30;
    public float thinMandibleParam = 0;


    /**
     * 大眼[0,1]  眼角1[0,1]  眼距[-1,1]  拉宽眼距[-1,1]  眼角2[-1,1]  眼睛高度[-1,1]
     */
    public float beautyBigEye = 45;
    public float eyeAngle1Param = 0;
    public float canthusParam = 0;
    public float canthus1Param = 0;
    public float eyeAngle2Param = 0;
    public float eyeTDAngleParam = 0;


    /**
     * 瘦鼻[-1,1] 鼻翼[-1,1]  鼻长[-1,1] 鼻头长[-1,1]
     */
    public float thinNoseParam = 40;
    public float nosewingParam = 30;
    public float nasalHeightParam = 0;
    public float noseTipHeightParam = 0;

    /**
     * 唇宽[-1,1] 嘴唇大小[-1,1] 唇高[-1,1] 人中[-1,1]
     */
    public float mouthWidthParam = 0;
    public float mouthSizeParam = 0;
    public float mouthHighParam = 0;
    public float philtrumParam = 0;

    /**
     * 发际线[-1,1]  嘴角上扬(微笑)[-1,1]
     */
    public float hairLineParam = 0;
    public float smileParam = 0;

    public float maxParam = 0;

    /**
     * 美妆透明度权重比
     */
    public float makeupAlphaWeight = 1f;

    /**
     * 滤镜强度
     */
    public float lutParam = 0.8f;

    @Override
    public BeautyParams clone() {
        BeautyParams beautyParams = null;
        try {
            beautyParams = (BeautyParams) super.clone();
            //美颜
            beautyParams.beautyWhite = this.beautyWhite;
            beautyParams.beautyBuffing = this.beautyBuffing;
            beautyParams.beautySharpen = this.beautySharpen;

            beautyParams.beautyPouch = this.beautyPouch;
            beautyParams.beautyNasolabialFolds = this.beautyNasolabialFolds;
            beautyParams.beautyWhiteTeeth = this.beautyWhiteTeeth;

            beautyParams.beautyBrightenEye = this.beautyBrightenEye;
            beautyParams.beautyRuddy = this.beautyRuddy;
            beautyParams.beautyBlush = this.beautyBlush;

            beautyParams.beautyLipstick = this.beautyLipstick;
            beautyParams.beautyLipstickColor = this.beautyLipstickColor;
            beautyParams.beautyLipstickGloss = this.beautyLipstickGloss;
            beautyParams.beautyLipstickBrightness = this.beautyLipstickBrightness;

            //美型
            beautyParams.cutCheekParam = this.cutCheekParam;
            beautyParams.cutFaceParam = this.cutFaceParam;
            beautyParams.beautySlimFace = this.beautySlimFace;
            beautyParams.longFaceParam = this.longFaceParam;

            beautyParams.lowerJawParam = this.lowerJawParam;
            beautyParams.higherJawParam = this.higherJawParam;
            beautyParams.thinJawParam = this.thinJawParam;
            beautyParams.thinMandibleParam = this.thinMandibleParam;

            beautyParams.beautyBigEye = this.beautyBigEye;
            beautyParams.eyeAngle1Param = this.eyeAngle1Param;
            beautyParams.canthusParam = this.canthusParam;
            beautyParams.canthus1Param = this.canthus1Param;
            beautyParams.eyeAngle2Param = this.thinMandibleParam;
            beautyParams.eyeTDAngleParam = this.eyeTDAngleParam;

            beautyParams.thinNoseParam = this.thinNoseParam;
            beautyParams.nosewingParam = this.nosewingParam;
            beautyParams.nasalHeightParam = this.nasalHeightParam;
            beautyParams.noseTipHeightParam = this.noseTipHeightParam;

            beautyParams.mouthWidthParam = this.mouthWidthParam;
            beautyParams.mouthSizeParam = this.mouthSizeParam;
            beautyParams.mouthHighParam = this.mouthHighParam;
            beautyParams.philtrumParam = this.philtrumParam;

            beautyParams.hairLineParam = this.hairLineParam;
            beautyParams.smileParam = this.smileParam;
            beautyParams.maxParam = this.maxParam;

            beautyParams.makeupAlphaWeight = this.makeupAlphaWeight;
            beautyParams.lutParam = this.lutParam;

            return beautyParams;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
