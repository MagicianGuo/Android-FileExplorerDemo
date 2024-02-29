package com.magicianguo.fileexplorer.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import com.magicianguo.fileexplorer.App;
import com.magicianguo.fileexplorer.constant.RequestCode;

import rikka.shizuku.Shizuku;

public class PermissionTools {
    private static final String SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api";

    public static boolean hasStoragePermission() {
        Context context = App.get();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    .setData(Uri.parse("package:"+activity.getPackageName()));
            activity.startActivityForResult(intent, RequestCode.STORAGE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE } , RequestCode.STORAGE);
        }
    }

    private static boolean isShizukuInstalled() {
        try {
            App.get().getPackageManager().getPackageInfo(SHIZUKU_PACKAGE_NAME, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isShizukuAvailable() {
        return isShizukuInstalled() && Shizuku.pingBinder();
    }

    public static boolean hasShizukuPermission() {
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求权限。
     * @return Shizuku是否可用
     */
    public static void requestShizukuPermission() {
        Shizuku.requestPermission(RequestCode.SHIZUKU);
    }
}
