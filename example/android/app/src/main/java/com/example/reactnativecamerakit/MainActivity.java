package com.example.reactnativecamerakit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "CameraKitExample";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
    }


    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected ReactRootView createRootView() {
                return new RNGestureHandlerEnabledRootView(MainActivity.this);
            }
        };
    }

    /**
     * 将图片保存到系统相册
     */
    public static boolean saveImgToAlbum(Context context, String imageFile) {
        try {
            ContentResolver localContentResolver = context.getContentResolver();
            File tempFile = new File(imageFile);
            ContentValues localContentValues = getImageContentValues(tempFile, System.currentTimeMillis());
            Uri uri = localContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
            copyFileAfterQ(context, localContentResolver, tempFile, uri);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将视频保存到系统相册
     */
    public static boolean saveVideoToAlbum(Context context, String videoFile) {
        try {
            ContentResolver localContentResolver = context.getContentResolver();
            File tempFile = new File(videoFile);
            ContentValues localContentValues = getVideoContentValues(tempFile, System.currentTimeMillis());

            Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);

            copyFileAfterQ(context, localContentResolver, tempFile, localUri);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 获取图片的ContentValue
     */
    public static ContentValues getImageContentValues(File paramFile, long timestamp) {
        ContentValues localContentValues = new ContentValues();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            localContentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera");
        }
        localContentValues.put(MediaStore.Images.Media.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Images.Media.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        localContentValues.put(MediaStore.Images.Media.DATE_TAKEN, timestamp);
        localContentValues.put(MediaStore.Images.Media.DATE_MODIFIED, timestamp);
        localContentValues.put(MediaStore.Images.Media.DATE_ADDED, timestamp);
        localContentValues.put(MediaStore.Images.Media.ORIENTATION, 0);
        localContentValues.put(MediaStore.Images.Media.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Images.Media.SIZE, paramFile.length());
        return localContentValues;
    }


    /**
     * 获取视频的contentValue
     */
    public static ContentValues getVideoContentValues(File paramFile, long timestamp) {
        ContentValues localContentValues = new ContentValues();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            localContentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "DCIM/Camera");
        }
        localContentValues.put(MediaStore.Video.Media.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        localContentValues.put(MediaStore.Video.Media.DATE_TAKEN, timestamp);
        localContentValues.put(MediaStore.Video.Media.DATE_MODIFIED, timestamp);
        localContentValues.put(MediaStore.Video.Media.DATE_ADDED, timestamp);
        localContentValues.put(MediaStore.Video.Media.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Video.Media.SIZE, paramFile.length());
        return localContentValues;
    }

    private static void copyFileAfterQ(Context context, ContentResolver localContentResolver, File tempFile, Uri localUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //拷贝文件到相册的uri,android11及以上得这么干，否则不会显示。可以参考ScreenMediaRecorder的save方法
            OutputStream os = localContentResolver.openOutputStream(localUri, "w");
            Files.copy(tempFile.toPath(), os);
            os.close();
            tempFile.deleteOnExit();
        }
    }


}
