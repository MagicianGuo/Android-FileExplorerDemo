package com.magicianguo.fileexplorer.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.magicianguo.fileexplorer.constant.PathType;
import com.magicianguo.fileexplorer.constant.RequestCode;
import com.magicianguo.fileexplorer.util.FileTools;

import rikka.shizuku.Shizuku;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity
        implements Shizuku.OnRequestPermissionResultListener {
    protected final String TAG = this.getClass().getSimpleName();
    protected VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = onBinding();
        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Shizuku.addRequestPermissionResultListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Shizuku.removeRequestPermissionResultListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == RequestCode.SHIZUKU) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                FileTools.specialPathReadType = PathType.SHIZUKU;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCode.STORAGE) {
            onStoragePermissionResult(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                onStoragePermissionResult(Environment.isExternalStorageManager());
            }
        } else if (requestCode == RequestCode.DOCUMENT) {
            Uri uri;
            if (data != null && (uri = data.getData()) != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                onDocumentPermissionResult(true);
            } else {
                onDocumentPermissionResult(false);
            }
        }
    }

    protected void onStoragePermissionResult(boolean granted) {
    }

    protected void onDocumentPermissionResult(boolean granted) {
    }

    protected abstract VB onBinding();
}
