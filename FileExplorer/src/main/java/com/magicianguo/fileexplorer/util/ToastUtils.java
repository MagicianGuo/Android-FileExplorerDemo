package com.magicianguo.fileexplorer.util;

import android.widget.Toast;

import androidx.annotation.StringRes;

import com.magicianguo.fileexplorer.App;

public class ToastUtils {
    private static Toast sToast;

    public static void shortCall(@StringRes int resId) {
        shortCall(App.get().getString(resId));
    }

    public static void shortCall(String text) {
        cancelToast();
        sToast = Toast.makeText(App.get(), text, Toast.LENGTH_SHORT);
        sToast.show();
    }

    public static void longCall(@StringRes int resId) {
        longCall(App.get().getString(resId));
    }

    public static void longCall(String text) {
        cancelToast();
        sToast = Toast.makeText(App.get(), text, Toast.LENGTH_LONG);
        sToast.show();
    }

    private static void cancelToast() {
        if (sToast != null) {
            sToast.cancel();
        }
    }
}
