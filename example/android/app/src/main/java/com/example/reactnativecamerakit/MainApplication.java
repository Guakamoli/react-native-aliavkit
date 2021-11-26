package com.example.reactnativecamerakit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.multidex.MultiDexApplication;

import com.aliyun.common.httpfinal.QupaiHttpFinal;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.ui.SdkVersionActivity;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.sys.AlivcSdkCore;
import com.blankj.utilcode.util.LogUtils;
import com.brentvatne.react.ReactVideoPackage;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.rncamerakit.AliAVkitManager;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;

import iyegoroff.imagefilterkit.ImageFilterKitPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.soloader.SoLoader;
import com.horcrux.svg.SvgPackage;
import com.reactnativecommunity.cameraroll.CameraRollPackage;
import com.rncamerakit.RNCameraKitPackage;
import com.swmansion.reanimated.ReanimatedPackage;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MainApplication extends MultiDexApplication implements ReactApplication {

    private final ReactNativeHost mReactNativeHost =
            new ReactNativeHost(this) {
                @Override
                public boolean getUseDeveloperSupport() {
                    return BuildConfig.DEBUG;
                }

//                @Override
//                protected JSIModulePackage getJSIModulePackage() {
//                    return new ReanimatedJSIModulePackage();
//                }

                @Override
                protected List<ReactPackage> getPackages() {
                    List<ReactPackage> packages = new PackageList(this).getPackages();
                    packages.add(new RNCameraKitPackage());
                    packages.add(new CameraRollPackage());
                    packages.add(new SvgPackage());
                    packages.add(new ReactVideoPackage());
                    packages.add(new ImageFilterKitPackage());

//                    boolean isAddRNGestureHandlerPackage = false;
//                    for (ReactPackage reactPackage : packages) {
//                        if (reactPackage.getClass() == RNGestureHandlerPackage.class) {
//                            isAddRNGestureHandlerPackage = true;
//                            break;
//                        }
//                    }
//                    if (!isAddRNGestureHandlerPackage) {
//                        packages.add(new RNGestureHandlerPackage());
//                    }
//
//                    boolean isAddReanimatedPackage = false;
//                    for (ReactPackage reactPackage : packages) {
//                        if (reactPackage.getClass() == ReanimatedPackage.class) {
//                            isAddReanimatedPackage = true;
//                            break;
//                        }
//                    }
//                    if (!isAddReanimatedPackage) {
//                        packages.add(new ReanimatedPackage());
//                    }

                    packages.add(new ReanimatedPackage());
                    packages.add(new RNGestureHandlerPackage());

                    return packages;
                }

                @Override
                protected String getJSMainModuleName() {
                    return "index";
                }
            };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.getConfig().setLogSwitch(true);
        SoLoader.init(this, /* native exopackage */ false);
        initializeFlipper(this, getReactNativeHost().getReactInstanceManager()); // Remove this line if you don't want Flipper enabled

        initKitCamera();

        initVideo(this);
    }

    private void initKitCamera() {
//        MusicFileInfoDao.instance.init(this);
//        //下载管理
//        FileDownloader.setupOnApplicationOnCreate(this);
        AliAVkitManager.init(this);
    }

    /**
     * Loads Flipper in React Native templates.
     *
     * @param context
     */
    private static void initializeFlipper(Context context, ReactInstanceManager reactInstanceManager) {
        if (BuildConfig.DEBUG) {
            try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
                Class<?> aClass = Class.forName("com.reactnativecamerakitExample.ReactNativeFlipper");
                aClass
                        .getMethod("initializeFlipper", Context.class, ReactInstanceManager.class)
                        .invoke(null, context, reactInstanceManager);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void initVideo(Application application) {
        QupaiHttpFinal.getInstance().initOkHttpFinal();
        DownloaderManager.getInstance().init(application);
        AlivcSdkCore.register(application.getApplicationContext());

        if (BuildConfig.DEBUG) {
            AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogWarn);
            AlivcSdkCore.setDebugLoggerLevel(AlivcSdkCore.AlivcDebugLoggerLevel.AlivcDLClose);
        } else {
            AlivcSdkCore.setLogLevel(AlivcSdkCore.AlivcLogLevel.AlivcLogDebug);
            AlivcSdkCore.setDebugLoggerLevel(AlivcSdkCore.AlivcDebugLoggerLevel.AlivcDLAll);
        }
        setSdkDebugParams(application);

        if (TextUtils.isEmpty(mLogPath)) {
            //保证每次运行app生成一个新的日志文件
            long time = System.currentTimeMillis();
            mLogPath =
                    application.getExternalFilesDir("Log").getAbsolutePath().toString() + "/log_" + time + ".log";
//                mLogPath = application.getExternalFilesDir("Log").getAbsolutePath() + "/log_" + time + ".log"
            AlivcSdkCore.setLogPath(mLogPath);
        }

        EffectService.setAppInfo(
                application.getResources().getString(R.string.app_name),
                application.getPackageName(),
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
        );
    }

    private String mLogPath = null;

    private void setSdkDebugParams(Application application) {
        //Demo 调试用，外部客户请勿使用
        SharedPreferences mySharedPreferences = application.getSharedPreferences(
                SdkVersionActivity.DEBUG_PARAMS,
                Activity.MODE_PRIVATE
        );
        int hostType = mySharedPreferences.getInt(SdkVersionActivity.DEBUG_DEVELOP_URL, 0);
        //AlivcSdkCore.setDebugHostType(hostType);
    }
}
