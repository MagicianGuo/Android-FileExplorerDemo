package com.magicianguo.fileexplorer.activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.magicianguo.fileexplorer.R;
import com.magicianguo.fileexplorer.bean.BeanFile;
import com.magicianguo.fileexplorer.constant.BundleKey;
import com.magicianguo.fileexplorer.constant.PathType;
import com.magicianguo.fileexplorer.databinding.ActivityMainBinding;
import com.magicianguo.fileexplorer.observer.IFileItemClickObserver;
import com.magicianguo.fileexplorer.userservice.FileExplorerServiceManager;
import com.magicianguo.fileexplorer.util.FileTools;
import com.magicianguo.fileexplorer.util.PermissionTools;
import com.magicianguo.fileexplorer.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private long mLastPressBackTime = 0L;
    private String mPathCache = FileTools.ROOT_PATH;
    private File mDirectory;

    private NavController mNavController;

    private final IFileItemClickObserver mFileItemClickObserver = new IFileItemClickObserver() {
        @Override
        public void onClickDir(String path) {
            loadPath(path, true);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        checkStoragePermission();
        FileTools.addFileItemClickObserver(mFileItemClickObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPath(mPathCache, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileTools.removeFileItemClickObserver(mFileItemClickObserver);
    }

    private void initView() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fcv_file_list);
        if (navHostFragment != null) {
            mNavController = navHostFragment.getNavController();
        }
        binding.btnBack.setOnClickListener(v -> {
            if (!FileTools.ROOT_PATH.equals(mDirectory.getPath())) {
                mDirectory = mDirectory.getParentFile();
                assert mDirectory != null;
                mPathCache = mDirectory.getPath();
                binding.tvPath.setText(mPathCache);
                mNavController.popBackStack();
            }
        });
        binding.tvPath.setOnLongClickListener(v -> {
            ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            manager.setPrimaryClip(new ClipData(new ClipDescription("", new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN}),
                    new ClipData.Item(binding.tvPath.getText())));
            ToastUtils.shortCall(R.string.toast_path_copied_to_clip);
            return true;
        });
    }

    private void loadPath(String path, boolean isUserClicked) {
        if (path == null) {
            return;
        }
        boolean isNavigate = !TextUtils.equals(mPathCache, path);
        mPathCache = path;
        if (FileTools.shouldRequestUriPermission(path)) {
            if (isUserClicked) {
                showRequestUriPermissionDialog();
            }
        } else {
            mDirectory = new File(path);
            binding.tvPath.setText(mDirectory.getPath());
            List<BeanFile> list = FileTools.getSortedFileList(path);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(BundleKey.FILE_LIST, (ArrayList<? extends Parcelable>) list);
            if (!isNavigate) {
                mNavController.popBackStack();
            }
            mNavController.navigate(R.id.fileListFragment, bundle);
        }
    }

    @Override
    protected ActivityMainBinding onBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    private void showStoragePermissionDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.dialog_storage_message)
                .setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) -> {
                    PermissionTools.requestStoragePermission(this);
                })
                .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                    finish();
                }).create().show();
    }

    private void showRequestUriPermissionDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.dialog_need_uri_permission_message)
                .setPositiveButton(R.string.dialog_button_request_permission, (dialog, which) -> {
                    FileTools.requestUriPermission(this, mPathCache);
                })
                .setNegativeButton(R.string.dialog_button_cancel, (dialog, which) -> {
                }).create().show();
    }

    private void checkStoragePermission() {
        if (PermissionTools.hasStoragePermission()) {
            loadPath(mPathCache, false);
            checkShizukuPermission();
        } else {
            showStoragePermissionDialog();
        }
    }

    private void checkShizukuPermission() {
        // 安卓11以下不需要Shizuku，使用File接口就能浏览/sdcard全部文件
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (PermissionTools.isShizukuAvailable()) {
                if (PermissionTools.hasShizukuPermission()) {
                    FileTools.specialPathReadType = PathType.SHIZUKU;
                    FileExplorerServiceManager.bindService();
                } else {
                    PermissionTools.requestShizukuPermission();
                }
            }
        }
    }

    @Override
    protected void onStoragePermissionResult(boolean granted) {
        if (granted) {
            loadPath(mPathCache, false);
            checkShizukuPermission();
        } else {
            showStoragePermissionDialog();
        }
    }

    @Override
    protected void onDocumentPermissionResult(boolean granted) {
        if (granted) {
            loadPath(mPathCache, false);
            ToastUtils.shortCall(R.string.toast_permission_granted);
        } else {
            ToastUtils.shortCall(R.string.toast_permission_not_granted);
        }
    }

    @Override
    public void onBackPressed() {
        if (!FileTools.ROOT_PATH.equals(mDirectory.getPath())) {
            mDirectory = mDirectory.getParentFile();
            assert mDirectory != null;
            mPathCache = mDirectory.getPath();
            binding.tvPath.setText(mPathCache);
            super.onBackPressed();
        } else {
            long time = System.currentTimeMillis();
            if (time - mLastPressBackTime < 2000L) {
                super.onBackPressed();
                finish();
            } else {
                ToastUtils.shortCall(R.string.toast_press_back_again_to_exit);
            }
            mLastPressBackTime = time;
        }
    }
}
