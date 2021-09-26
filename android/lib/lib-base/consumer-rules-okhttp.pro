# Okio
-keep class okio** { *; }
-dontwarn okio.**
# Okio

#Okhttp3
-keep class okhttp3** { *; }
-keep interface okhttp3** { *; }
-dontwarn okhttp3.**
#Okhttp3

#retrofit2
-keep class retrofit2** { *; }
-keep interface retrofit2** { *; }
-dontwarn retrofit2.**
#retrofit2



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