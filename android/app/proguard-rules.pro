-keep class com.facebook.hermes.unicode.** { *; }
-keep class com.facebook.jni.** { *; }
-keep class com.guakamoli.paiyatalent.android.** { *; }

-keep class com.facebook.**{ *; }
-dontwarn com.facebook.**
-keep class com.facebook.react.devsupport.** { *; }
-dontwarn com.facebook.react.devsupport.**

-keep class org.jetbrains.anko.**{ *; }
-dontwarn org.jetbrains.anko.**
-keep class kotlinx.coroutines.**{ *; }
-dontwarn kotlinx.coroutines.**



#-dontobfuscate

# React Native
# React Native
# React Native
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.proguard.annotations.DoNotStrip
-keep,allowobfuscation @interface com.facebook.proguard.annotations.KeepGettersAndSetters
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.proguard.annotations.DoNotStrip class *
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
 @com.facebook.proguard.annotations.DoNotStrip *;
 @com.facebook.common.internal.DoNotStrip *;
}

-keepclassmembers @com.facebook.proguard.annotations.KeepGettersAndSetters class * {
 void set*(***);
 *** get*();
}

-keep class * extends com.facebook.react.bridge.JavaScriptModule { *; }
-keep class * extends com.facebook.react.bridge.NativeModule { *; }
-keepclassmembers,includedescriptorclasses class * { native <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.UIProp <fields>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactProp <methods>; }
-keepclassmembers class *  { @com.facebook.react.uimanager.annotations.ReactPropGroup <methods>; }

-keep class com.facebook.** { *; }
-dontwarn com.facebook.react.**


-keep,allowobfuscation,includedescriptorclasses @interface com.facebook.proguard.annotations.DoNotStrip
-keep,allowobfuscation,includedescriptorclasses @interface com.facebook.proguard.annotations.KeepGettersAndSetters
-keep,allowobfuscation,includedescriptorclasses @interface com.facebook.common.internal.DoNotStrip

# SoLoader
-keep class com.facebook.soloader.** { *; }
-keepclassmembers class com.facebook.soloader.SoLoader {
     static <fields>;
}

# Do not strip any method/class that is annotated with @DoNotStrip
-keep,includedescriptorclasses @com.facebook.proguard.annotations.DoNotStrip class *
-keep,includedescriptorclasses @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers,includedescriptorclasses class * {
    @com.facebook.proguard.annotations.DoNotStrip *;
    @com.facebook.common.internal.DoNotStrip *;
}

-keepclassmembers,includedescriptorclasses @com.facebook.proguard.annotations.KeepGettersAndSetters class * {
  void set*(***);
  *** get*();
}

-keep,includedescriptorclasses class * { native <methods>; }
-keep,includedescriptorclasses class * { @com.facebook.react.uimanager.UIProp <fields>; }
-keep,includedescriptorclasses class * { @com.facebook.react.uimanager.annotations.ReactProp <methods>; }
-keep,includedescriptorclasses class * { @com.facebook.react.uimanager.annotations.ReactPropGroup <methods>; }
-keep,includedescriptorclasses class com.facebook.react.uimanager.UIProp { *; }

-keep,includedescriptorclasses class * extends com.facebook.react.bridge.JavaScriptModule { *; }
-keep,includedescriptorclasses class * extends com.facebook.react.bridge.NativeModule { *; }
-keep,includedescriptorclasses class com.facebook.react.bridge.CatalystInstanceImpl { *; }
-keep,includedescriptorclasses class com.facebook.react.bridge.JavaScriptExecutor { *; }
-keep,includedescriptorclasses class com.facebook.react.bridge.queue.NativeRunnable { *; }
-keep,includedescriptorclasses class com.facebook.react.bridge.ExecutorToken { *; }
-keep,includedescriptorclasses class com.facebook.react.bridge.ReadableType { *; }

-dontwarn com.facebook.react.**
-dontnote com.facebook.**

# TextLayoutBuilder uses a non-public Android constructor within StaticLayout.
# See libs/proxy/src/main/java/com/facebook/fbui/textlayoutbuilder/proxy for details.
-dontwarn android.text.StaticLayout
# React Native
# React Native
# React Native

# okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


# okio

-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**


-keepattributes *Annotation*
-keepclassmembers class ** {
  @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }


#kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keep  class  com.rncamerakit.db.MusicFileBaseInfo{*;}
-keep  class  com.rncamerakit.db.MusicFileBean{*;}
-keep  class  com.rncamerakit.editor.manager.**{*;}

-keep  class  com.rncamerakit.db.MusicFileBaseInfo{*;}

-keep public class * extends com.facebook.react.bridge.NativeModule{*;}


-keep public class com.dylanvann.fastimage.* {*;}
-keep public class com.dylanvann.fastimage.** {*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
