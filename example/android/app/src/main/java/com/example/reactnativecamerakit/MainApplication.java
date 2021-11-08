package com.example.reactnativecamerakit;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.aliyun.svideo.downloader.DownloaderManager;
import com.blankj.utilcode.util.LogUtils;
import com.brentvatne.react.ReactVideoPackage;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.bridge.JSIModulePackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.soloader.SoLoader;
import com.horcrux.svg.SvgPackage;
import com.liulishuo.filedownloader.FileDownloader;
import com.reactnativecommunity.cameraroll.CameraRollPackage;
import com.rncamerakit.RNCameraKitPackage;
import com.swmansion.reanimated.ReanimatedJSIModulePackage;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import iyegoroff.imagefilterkit.MainReactPackageWithFrescoCache;

public class MainApplication extends MultiDexApplication implements ReactApplication {

    private final ReactNativeHost mReactNativeHost =
            new ReactNativeHost(this) {
                @Override
                public boolean getUseDeveloperSupport() {
                    return BuildConfig.DEBUG;
                }

                @Override
                protected JSIModulePackage getJSIModulePackage() {
                    return new ReanimatedJSIModulePackage();
                }

                @Override
                protected List<ReactPackage> getPackages() {
                    List<ReactPackage> packages = new PackageList(this).getPackages();
                    packages.add(new RNCameraKitPackage());
                    packages.add(new CameraRollPackage());
                    packages.add(new SvgPackage());
                    packages.add(new ReactVideoPackage());
//                    packages.add(new RNGestureHandlerPackage());
//                    return packages;
                    return MainReactPackageWithFrescoCache.inject(packages);
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
        DownloaderManager.getInstance().init(this);

        //下载管理
        FileDownloader.setupOnApplicationOnCreate(this);
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
}
