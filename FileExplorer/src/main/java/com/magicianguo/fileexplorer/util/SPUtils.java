package com.magicianguo.fileexplorer.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.magicianguo.fileexplorer.App;

public class SPUtils {
    private static final SharedPreferences SP = App.get().getSharedPreferences("SPUtils", Context.MODE_PRIVATE);
    private static final String KEY_USE_NEW_DOCUMENT = "use_new_document";

    public static void setUseNewDocument(boolean use) {
        SP.edit().putBoolean(KEY_USE_NEW_DOCUMENT, use).apply();
    }

    public static boolean getUseNewDocument() {
        return SP.getBoolean(KEY_USE_NEW_DOCUMENT, true);
    }
}
