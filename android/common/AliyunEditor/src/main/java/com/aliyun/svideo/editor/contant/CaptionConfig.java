package com.aliyun.svideo.editor.contant;

public class CaptionConfig {
    public static final int FONT_TYPE = 1;
    public static final int CAPTION_TYPE = 6;
    public static final String SYSTEM_FONT = "system_font";
    public static final int VIEW_TYPE_SELECTED = 1;
    public static final int VIEW_TYPE_UNSELECTED = 2;
    //字幕动效
    public static final int EFFECT_NONE = 0, EFFECT_UP = 1, EFFECT_RIGHT = 4, EFFECT_LEFT = 3, EFFECT_DOWN = 2,
            EFFECT_LINEARWIPE = 6, EFFECT_FADE = 5, EFFECT_SCALE = 7;
    public static final int[] POSITION_FONT_ANIM_ARRAY = {EFFECT_NONE, EFFECT_UP, EFFECT_RIGHT, EFFECT_LEFT,
            EFFECT_DOWN, EFFECT_LINEARWIPE, EFFECT_FADE, EFFECT_SCALE
    };
    public static final String FONT_NAME = "/font.ttf";
    /**
     * 花字体
     */
    public static final String COOL_TEXT_FILE_DIR = "font_effect";
    public static final String COOL_TEXT_FILE_ICON_NAME = "icon.png";
    public static final long DEFAULT_DURATION = 1500000;
    public static final long CAPTION_MIN_DURATION =500000;
}
