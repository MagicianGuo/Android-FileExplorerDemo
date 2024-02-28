package com.magicianguo.fileexplorer.userservice;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.magicianguo.fileexplorer.App;
import com.magicianguo.fileexplorer.BuildConfig;
import com.magicianguo.fileexplorer.R;
import com.magicianguo.fileexplorer.util.FileTools;
import com.magicianguo.fileexplorer.util.ToastUtils;

import rikka.shizuku.Shizuku;

public class FileExplorerServiceManager {
    private static final String TAG = "FileExplorerServiceManager";
    private static boolean isBind = false;

    private static final Shizuku.UserServiceArgs USER_SERVICE_ARGS = new Shizuku.UserServiceArgs(
            new ComponentName(App.get().getPackageName(), FileExplorerService.class.getName())
    ).daemon(false).debuggable(BuildConfig.DEBUG).processNameSuffix("file_explorer_service").version(1);

    private static final ServiceConnection SERVICE_CONNECTION = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            isBind = true;
            FileTools.iFileExplorerService = IFileExplorerService.Stub.asInterface(service);
            ToastUtils.shortCall(R.string.toast_shizuku_connected);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
            isBind = false;
            FileTools.iFileExplorerService = null;
            ToastUtils.shortCall(R.string.toast_shizuku_disconnected);
        }
    };

    public static void bindService() {
        Log.d(TAG, "bindService: isBind = " + isBind);
        if (!isBind) {
            Shizuku.bindUserService(USER_SERVICE_ARGS, SERVICE_CONNECTION);
        }
    }
}
