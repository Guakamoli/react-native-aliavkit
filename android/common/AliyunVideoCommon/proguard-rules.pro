-ignorewarnings

#crashreporter
-keep class com.alibaba.motu.crashreporter.MotuCrashReporter{*;}
-keep class com.alibaba.motu.crashreporter.ReporterConfigure{*;}
-keep class com.alibaba.motu.crashreporter.IUTCrashCaughtListener{*;}
-keep class com.ut.mini.crashhandler.IUTCrashCaughtListener{*;}
-keep class com.alibaba.motu.crashreporter.utrestapi.UTRestReq{*;}
-keep class com.alibaba.motu.crashreporter.handler.nativeCrashHandler.NativeCrashHandler{*;}
-keep class com.alibaba.motu.crashreporter.handler.nativeCrashHandler.NativeExceptionHandler{*;}
-keep interface com.alibaba.motu.crashreporter.handler.nativeCrashHandler.NativeExceptionHandler{*;}
#crashreporter3.0以后 一定要加这个
-keep class com.uc.crashsdk.JNIBridge{*;}

#防止混淆AliRtcSDK公共类名称
-keep class com.serenegiant.**{*;}
-keep class org.webrtc.**{*;}

####################友盟数据统计############
-keep class com.umeng.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
######################conan混淆配置#########################
-keepattributes *Annotation*
-keep class com.alivc.conan.DoNotProguard
-keep,allowobfuscation @interface com.alivc.conan.DoNotProguard
-keep @com.alivc.conan.DoNotProguard class *
-keepclassmembers class * {
    @com.alivc.conan.DoNotProguard *;
}
######################短视频混淆配置#########################
-keep class com.aliyun.**{*;}
-keep class com.duanqu.**{*;}
-keep class com.qu.**{*;}
-keep class com.alibaba.**{*;}
-keep class component.alivc.**{*;}
-keep class com.alivc.**{*;}
-keep enum com.aliyun.editor.AudioEffectType { *; }

######################glide 混淆配置#########################
-keep class com.bumptech.glide{*;}
-keep class com.bumptech.glide.**{*;}

