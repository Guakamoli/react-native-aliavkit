package com.aliyun.svideo.base.widget.beauty.sharp;

/**
 * Created by Akira on 2018/6/20.
 *
 * 美型
 */
public class BeautyShapeParams implements Cloneable {

    /**
     * 颧骨
     */
    public float beautyCutCheek = 50;
    /**
     * 窄脸
     */
    public float beautyCutFace = 50;
    /**
     * 瘦脸
     */
    public float beautyThinFace = 50;
    /**
     * 脸长
     */
    public float beautyLongFace = 35;
    /**
     * 下巴缩短
     */
    public float beautyLowerJaw = 30;
    /**
     * 大眼
     */
    public float beautyBigEye = 45;
    /**
     * 瘦鼻
     */
    public float beautyThinNose =40;

    /**
     * 鼻翼
     */
    public float beautyNoseWing = 30;
    /**
     * 唇宽
     */
    public float beautyMouthWidth = 0;
    /**
     * 下颌
     */
    public float beautyThinMandible = 0;


    public BeautyShapeParams() {
    }


    @Override
    public BeautyShapeParams clone() {
        BeautyShapeParams beautyParams = null;
        try {
            beautyParams = (BeautyShapeParams)super.clone();
            beautyParams.beautyCutFace = this.beautyCutFace;
            beautyParams.beautyThinFace = this.beautyThinFace;
            beautyParams.beautyLongFace = this.beautyLongFace;
            beautyParams.beautyLowerJaw = this.beautyLowerJaw;
            beautyParams.beautyBigEye = this.beautyBigEye;
            beautyParams.beautyThinNose = this.beautyThinNose;
            beautyParams.beautyNoseWing = this.beautyNoseWing;
            beautyParams.beautyMouthWidth = this.beautyMouthWidth;
            beautyParams.beautyThinMandible = this.beautyThinMandible;
            beautyParams.beautyCutCheek = this.beautyCutCheek;
            return beautyParams;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
