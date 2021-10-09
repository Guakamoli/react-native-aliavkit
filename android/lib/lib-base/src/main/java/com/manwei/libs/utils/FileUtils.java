package com.manwei.libs.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author : wuyq
 * Time : 2020/9/19 0:02
 * Description : 文件相关
 */
public final class FileUtils {


    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        return getFileName(new File(filePath));
    }

    public static String getFileName(File file) {
        if (file == null) {
            return null;
        }
        DocumentFile documentFile = DocumentFile.fromFile(file);
        return documentFile.getName();
    }

    /**
     * 获取文件扩展名
     *
     * @param filePath /storage/emulated/.../7ac389230d004e2dac6a7c6bb484c017.jpg
     *                 <p>
     *                 return .jpg
     */
    public static String getSuffixName(String filePath) {
        if ("".equals(filePath) || filePath == null) {
            return filePath;
        }
        if (filePath.length() > 0) {
            int dot = filePath.lastIndexOf('.');
            if ((dot > -1) && (dot < (filePath.length() - 1))) {
                return filePath.substring(dot);
            }
        }
        return filePath;
    }


    /**
     * 创建目录
     */
    private static File createDir(String sdcardDirName) {
        File fileDir = new File(sdcardDirName);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }

    /**
     * 判断文件是否存在
     */
    public static boolean fileIsExists(String strFile) {
        if (TextUtils.isEmpty(strFile)) {
            return false;
        }
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 创建文件
     */
    public static File createFile(String sdcardDirName, String fileName) {
        createDir(sdcardDirName);
        File file = new File(sdcardDirName + File.separator + fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 删除文件夹内所有内容
     *
     * @param path
     */
    public static void deleteFiles(@NonNull String path) {
        deleteFiles(new File(path));
    }

    public static void deleteFiles(@NonNull File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        deleteFiles(file1);
                    }
                }
            }
            //deleteOnExit 只能删除文件或者空目录
            file.delete();
        }
    }


    public static byte[] fileToByte(File file) {
        if (file == null) {
            return null;
        }
        FileInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        byte[] bytes = null;
        try {
            inputStream = new FileInputStream(file);
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            bytes = outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }


    public static File byteToFile(byte[] buf, String dirPath, String fileName) {
        BufferedOutputStream bufferedOutputStream = null;
        FileOutputStream outputStream = null;
        File file = null;
        try {
            File dir = new File(dirPath);
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            file = new File(dirPath + File.separator + fileName);
            if (!file.exists()) {
                file = createFile(dirPath, fileName);
            }
            outputStream = new FileOutputStream(file);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static boolean copyFile(File oldFile, File outputFile) {
        if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
            return false;
        }
        try {
            FileInputStream inputStream = new FileInputStream(oldFile);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            return copyFile(inputStream, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean copyFile(InputStream inputStream, FileOutputStream fileOutputStream) {
        if (inputStream == null || fileOutputStream == null) {
            return false;
        }
        try {
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = inputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                inputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }



}
