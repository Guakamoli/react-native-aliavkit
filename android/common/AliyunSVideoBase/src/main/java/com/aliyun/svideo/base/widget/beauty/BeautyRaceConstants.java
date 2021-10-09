package com.aliyun.svideo.base.widget.beauty;

import com.aliyun.svideo.common.RomUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Akira on 2018/5/30.
 * <p>
 * Queen初始参数
 * <p>
 * Queen 各等级参数
 */
public class BeautyRaceConstants {

    //queen 各个等级的参数
    public final static Map<Integer, BeautyParams> QUEEN_BEAUTY_MAP = new HashMap<>();

    static {

        if (RomUtils.isHuawei()) {
            QUEEN_BEAUTY_MAP.put(0, createBeautyParamLevel_0());
            QUEEN_BEAUTY_MAP.put(1, createHuaweiBeautyParamLevel_1());
            QUEEN_BEAUTY_MAP.put(2, createHuaweiBeautyParamLevel_2());
            QUEEN_BEAUTY_MAP.put(3, createHuaweiBeautyParamLevel_3());
            QUEEN_BEAUTY_MAP.put(4, createHuaweiBeautyParamLevel_4());
            QUEEN_BEAUTY_MAP.put(5, createHuaweiBeautyParamLevel_5());
        } else if (RomUtils.isXiaomi()) {
            QUEEN_BEAUTY_MAP.put(0, createBeautyParamLevel_0());
            QUEEN_BEAUTY_MAP.put(1, createXiaomiBeautyParamLevel_1());
            QUEEN_BEAUTY_MAP.put(2, createXiaomiBeautyParamLevel_2());
            QUEEN_BEAUTY_MAP.put(3, createXiaomiBeautyParamLevel_3());
            QUEEN_BEAUTY_MAP.put(4, createXiaomiBeautyParamLevel_4());
            QUEEN_BEAUTY_MAP.put(5, createXiaomiBeautyParamLevel_5());
        } else if (RomUtils.isVivo()) {
            QUEEN_BEAUTY_MAP.put(0, createBeautyParamLevel_0());
            QUEEN_BEAUTY_MAP.put(1, createVivoBeautyParamLevel_1());
            QUEEN_BEAUTY_MAP.put(2, createVivoBeautyParamLevel_2());
            QUEEN_BEAUTY_MAP.put(3, createVivoBeautyParamLevel_3());
            QUEEN_BEAUTY_MAP.put(4, createVivoBeautyParamLevel_4());
            QUEEN_BEAUTY_MAP.put(5, createVivoBeautyParamLevel_5());
        } else {
            QUEEN_BEAUTY_MAP.put(0, createBeautyParamLevel_0());
            QUEEN_BEAUTY_MAP.put(1, createOppoBeautyParamLevel_1());
            QUEEN_BEAUTY_MAP.put(2, createOppoBeautyParamLevel_2());
            QUEEN_BEAUTY_MAP.put(3, createOppoBeautyParamLevel_3());
            QUEEN_BEAUTY_MAP.put(4, createOppoBeautyParamLevel_4());
            QUEEN_BEAUTY_MAP.put(5, createOppoBeautyParamLevel_5());
        }

        //设置美妆透明度权重比
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(0)).makeupAlphaWeight = 0f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(1)).makeupAlphaWeight = 0.6f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(2)).makeupAlphaWeight = 0.7f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(3)).makeupAlphaWeight = 0.9f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(4)).makeupAlphaWeight = 0.95f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(5)).makeupAlphaWeight = 1f;

        //设置滤镜强度
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(0)).lutParam = 0.1f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(1)).lutParam = 0.6f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(2)).lutParam = 0.7f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(3)).lutParam = 0.8f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(4)).lutParam = 0.9f;
        Objects.requireNonNull(QUEEN_BEAUTY_MAP.get(5)).lutParam = 1f;

    }

    private static BeautyParams createBeautyParamLevel_0() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 0;
        beautyParams.beautyBuffing = 0;
        beautyParams.beautySharpen = 0;

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 0;
        beautyParams.beautyNasolabialFolds = 0;
        beautyParams.beautyWhiteTeeth = 0;

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 0;
        beautyParams.beautyRuddy = 0;
        beautyParams.beautyBlush = 0;

        //口红
        beautyParams.beautyLipstick = 0;
        beautyParams.beautyLipstickColor = 0;
        beautyParams.beautyLipstickGloss = 0;
        beautyParams.beautyLipstickBrightness = 0;

//        //瘦脸 大眼
//        beautyParams.beautySlimFace = 0;
//        beautyParams.beautyBigEye = 0;
        return beautyParams;
    }


    private static BeautyParams createHuaweiBeautyParamLevel_1() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 3;//默认 16
        beautyParams.beautyBuffing = 30;//默认 60
        beautyParams.beautySharpen = 6;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 25;//默认 60
        beautyParams.beautyNasolabialFolds = 25;//默认 60
        beautyParams.beautyWhiteTeeth = 10;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 10;//默认 20
        beautyParams.beautyRuddy = 5;//默认 15
        beautyParams.beautyBlush = 5;//默认 15

        //口红
        beautyParams.beautyLipstick = 5;//默认 10
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }


    private static BeautyParams createHuaweiBeautyParamLevel_2() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 6;//默认 16
        beautyParams.beautyBuffing = 55;//默认 60
        beautyParams.beautySharpen = 8;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 45;//默认 60
        beautyParams.beautyNasolabialFolds = 45;//默认 60
        beautyParams.beautyWhiteTeeth = 20;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 15;//默认 20
        beautyParams.beautyRuddy = 10;//默认 15
        beautyParams.beautyBlush = 10;//默认 15

        //口红
        beautyParams.beautyLipstick = 10;//默认 10
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }


    private static BeautyParams createHuaweiBeautyParamLevel_3() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 10;//默认 10
        beautyParams.beautyBuffing = 80;//默认 80
        beautyParams.beautySharpen = 10;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 60;//默认 60
        beautyParams.beautyNasolabialFolds = 60;//默认 60
        beautyParams.beautyWhiteTeeth = 30;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 20;//默认 20
        beautyParams.beautyRuddy = 20;//默认 20
        beautyParams.beautyBlush = 12;//默认 12

        //口红
        beautyParams.beautyLipstick = 15;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createHuaweiBeautyParamLevel_4() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 19;//默认 10
        beautyParams.beautyBuffing = 84;//默认 80
        beautyParams.beautySharpen = 11;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 62;//默认 60
        beautyParams.beautyNasolabialFolds = 65;//默认 60
        beautyParams.beautyWhiteTeeth = 35;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 22;//默认 20
        beautyParams.beautyRuddy = 21;//默认 20
        beautyParams.beautyBlush = 12;//默认 12

        //口红
        beautyParams.beautyLipstick = 16;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }


    private static BeautyParams createHuaweiBeautyParamLevel_5() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 28;//默认 10
        beautyParams.beautyBuffing = 88;//默认 80
        beautyParams.beautySharpen = 12;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 66;//默认 60
        beautyParams.beautyNasolabialFolds = 70;//默认 60
        beautyParams.beautyWhiteTeeth = 40;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 24;//默认 20
        beautyParams.beautyRuddy = 22;//默认 20
        beautyParams.beautyBlush = 13;//默认 12

        //口红
        beautyParams.beautyLipstick = 17;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createVivoBeautyParamLevel_1() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 10;//默认 35
        beautyParams.beautyBuffing = 45;//默认 80
        beautyParams.beautySharpen = 6;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 40;//默认 60
        beautyParams.beautyNasolabialFolds = 40;//默认 60
        beautyParams.beautyWhiteTeeth = 10;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 10;//默认 20
        beautyParams.beautyRuddy = 10;//默认 20
        beautyParams.beautyBlush = 5;//默认 15

        //口红
        beautyParams.beautyLipstick = 5;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createVivoBeautyParamLevel_2() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 25;//默认 35
        beautyParams.beautyBuffing = 65;//默认 80
        beautyParams.beautySharpen = 8;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 50;//默认 60
        beautyParams.beautyNasolabialFolds = 50;//默认 60
        beautyParams.beautyWhiteTeeth = 20;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 15;//默认 20
        beautyParams.beautyRuddy = 15;//默认 20
        beautyParams.beautyBlush = 10;//默认 15

        //口红
        beautyParams.beautyLipstick = 10;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createVivoBeautyParamLevel_3() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 35;//默认 35
        beautyParams.beautyBuffing = 85;//默认 80
        beautyParams.beautySharpen = 12;//默认 12

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 60;//默认 60
        beautyParams.beautyNasolabialFolds = 60;//默认 60
        beautyParams.beautyWhiteTeeth = 30;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 20;//默认 20
        beautyParams.beautyRuddy = 60;//默认 60
        beautyParams.beautyBlush = 20;//默认 20

        //口红
        beautyParams.beautyLipstick = 15;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createVivoBeautyParamLevel_4() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 50;//默认 35
        beautyParams.beautyBuffing = 85;//默认 80
        beautyParams.beautySharpen = 10;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 65;//默认 60
        beautyParams.beautyNasolabialFolds = 65;//默认 60
        beautyParams.beautyWhiteTeeth = 35;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 25;//默认 20
        beautyParams.beautyRuddy = 25;//默认 20
        beautyParams.beautyBlush = 20;//默认 15

        //口红
        beautyParams.beautyLipstick = 16;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }


    private static BeautyParams createVivoBeautyParamLevel_5() {
        BeautyParams beautyParams = new BeautyParams();

        //美白 磨皮 锐化
        beautyParams.beautyWhite = 65;//默认 35
        beautyParams.beautyBuffing = 90;//默认 80
        beautyParams.beautySharpen = 10;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 75;//默认 60
        beautyParams.beautyNasolabialFolds = 75;//默认 60
        beautyParams.beautyWhiteTeeth = 40;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 30;//默认 20
        beautyParams.beautyRuddy = 30;//默认 20
        beautyParams.beautyBlush = 25;//默认 15

        //口红
        beautyParams.beautyLipstick = 17;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createXiaomiBeautyParamLevel_1() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 3;//默认 10
        beautyParams.beautyBuffing = 35;//默认 70
        beautyParams.beautySharpen = 6;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 30;//默认 60
        beautyParams.beautyNasolabialFolds = 30;//默认 60
        beautyParams.beautyWhiteTeeth = 10;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 10;//默认 20
        beautyParams.beautyRuddy = 22;//默认 50
        beautyParams.beautyBlush = 10;//默认 25

        //口红
        beautyParams.beautyLipstick = 5;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createXiaomiBeautyParamLevel_2() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 6;//默认 10
        beautyParams.beautyBuffing = 55;//默认 70
        beautyParams.beautySharpen = 8;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 45;//默认 60
        beautyParams.beautyNasolabialFolds = 45;//默认 60
        beautyParams.beautyWhiteTeeth = 20;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 15;//默认 20
        beautyParams.beautyRuddy = 35;//默认 50
        beautyParams.beautyBlush = 15;//默认 25

        //口红
        beautyParams.beautyLipstick = 10;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createXiaomiBeautyParamLevel_3() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 16;//默认 10
        beautyParams.beautyBuffing = 80;//默认 70
        beautyParams.beautySharpen = 10;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 60;//默认 60
        beautyParams.beautyNasolabialFolds = 60;//默认 60
        beautyParams.beautyWhiteTeeth = 30;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 20;//默认 20
        beautyParams.beautyRuddy = 50;//默认 50
        beautyParams.beautyBlush = 25;//默认 25

        //口红
        beautyParams.beautyLipstick = 15;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createXiaomiBeautyParamLevel_4() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 21;//默认 10
        beautyParams.beautyBuffing = 80;//默认 70
        beautyParams.beautySharpen = 11;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 63;//默认 60
        beautyParams.beautyNasolabialFolds = 65;//默认 60
        beautyParams.beautyWhiteTeeth = 33;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 22;//默认 20
        beautyParams.beautyRuddy = 50;//默认 50
        beautyParams.beautyBlush = 25;//默认 25

        //口红
        beautyParams.beautyLipstick = 16;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createXiaomiBeautyParamLevel_5() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 28;//默认 10
        beautyParams.beautyBuffing = 85;//默认 70
        beautyParams.beautySharpen = 12;//默认 10

        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 66;//默认 60
        beautyParams.beautyNasolabialFolds = 70;//默认 60
        beautyParams.beautyWhiteTeeth = 36;//默认 30

        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 24;//默认 20
        beautyParams.beautyRuddy = 50;//默认 50
        beautyParams.beautyBlush = 25;//默认 25

        //口红
        beautyParams.beautyLipstick = 16;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createOppoBeautyParamLevel_1() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 6;//默认 25
        beautyParams.beautyBuffing = 40;//默认 80
        beautyParams.beautySharpen = 6;//默认 10
        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 30;//默认 60
        beautyParams.beautyNasolabialFolds = 30;//默认 60
        beautyParams.beautyWhiteTeeth = 10;//默认 20
        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 10;//默认 20
        beautyParams.beautyRuddy = 10;//默认 30
        beautyParams.beautyBlush = 6;//默认 15
        //口红
        beautyParams.beautyLipstick = 6;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createOppoBeautyParamLevel_2() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 11;//默认 25
        beautyParams.beautyBuffing = 65;//默认 80
        beautyParams.beautySharpen = 8;//默认 10
        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 45;//默认 60
        beautyParams.beautyNasolabialFolds = 45;//默认 60
        beautyParams.beautyWhiteTeeth = 15;//默认 20
        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 15;//默认 20
        beautyParams.beautyRuddy = 20;//默认 30
        beautyParams.beautyBlush = 10;//默认 15
        //口红
        beautyParams.beautyLipstick = 10;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createOppoBeautyParamLevel_3() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 16;//默认 25
        beautyParams.beautyBuffing = 80;//默认 80
        beautyParams.beautySharpen = 10;//默认 10
        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 60;//默认 60
        beautyParams.beautyNasolabialFolds = 60;//默认 60
        beautyParams.beautyWhiteTeeth = 20;//默认 20
        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 20;//默认 20
        beautyParams.beautyRuddy = 30;//默认 30
        beautyParams.beautyBlush = 15;//默认 15
        //口红
        beautyParams.beautyLipstick = 15;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createOppoBeautyParamLevel_4() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 25;//默认 25
        beautyParams.beautyBuffing = 83;//默认 80
        beautyParams.beautySharpen = 11;//默认 10
        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 63;//默认 60
        beautyParams.beautyNasolabialFolds = 65;//默认 60
        beautyParams.beautyWhiteTeeth = 25;//默认 20
        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 25;//默认 20
        beautyParams.beautyRuddy = 32;//默认 30
        beautyParams.beautyBlush = 16;//默认 15
        //口红
        beautyParams.beautyLipstick = 16;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

    private static BeautyParams createOppoBeautyParamLevel_5() {
        BeautyParams beautyParams = new BeautyParams();
        //美白 磨皮 锐化
        beautyParams.beautyWhite = 30;//默认 25
        beautyParams.beautyBuffing = 86;//默认 80
        beautyParams.beautySharpen = 12;//默认 10
        //眼袋 法令纹 白牙
        beautyParams.beautyPouch = 66;//默认 60
        beautyParams.beautyNasolabialFolds = 70;//默认 60
        beautyParams.beautyWhiteTeeth = 30;//默认 20
        //亮眼 红润 腮红
        beautyParams.beautyBrightenEye = 30;//默认 20
        beautyParams.beautyRuddy = 34;//默认 30
        beautyParams.beautyBlush = 17;//默认 15
        //口红
        beautyParams.beautyLipstick = 16;//默认 15
        beautyParams.beautyLipstickColor = -0.08f;
        beautyParams.beautyLipstickGloss = 1.0f;
        beautyParams.beautyLipstickBrightness = 0.0f;
        return beautyParams;
    }

}
