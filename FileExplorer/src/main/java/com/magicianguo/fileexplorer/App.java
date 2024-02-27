package com.magicianguo.fileexplorer;

import android.app.Application;
import android.os.Build;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.io.FileOutputStream;

public class App extends Application {
    private static App sApp;

    public static App get() {
        return sApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        addHiddenApiExemptions();
        createFile();
    }

    private void addHiddenApiExemptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 用于绕过隐藏api的调用限制
            HiddenApiBypass.addHiddenApiExemptions("L");
        }
    }

    private void createFile() {
        try {
            String path = getExternalFilesDir(null).getParent();
            File file = new File(path, "test.txt");
            if (!file.exists()) {
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write("Hello world!".getBytes());
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
