package com.magicianguo.fileexplorer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.view.MotionEvent;

import androidx.appcompat.app.AlertDialog;

import com.magicianguo.fileexplorer.App;
import com.magicianguo.fileexplorer.databinding.ActivitySettingBinding;
import com.magicianguo.fileexplorer.util.SPUtils;

public class SettingActivity extends BaseActivity<ActivitySettingBinding> {
    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.swNewDocument.setChecked(SPUtils.getUseNewDocument());
        binding.swNewDocument.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setTag("clicked");
            }
            return false;
        });
        binding.swNewDocument.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!"clicked".equals(buttonView.getTag())) {
                return;
            }
            buttonView.setTag("");
            new AlertDialog.Builder(this)
                    .setMessage("切换后需要重新打开文件管理")
                    .setPositiveButton("确定", (dialog, which) -> {
                        buttonView.setChecked(isChecked);
                        SPUtils.setUseNewDocument(isChecked);
                        buttonView.postDelayed(() -> {
                            for (Activity activity : App.getAliveActivityList()) {
                                activity.finish();
                            }
                            Process.killProcess(Process.myPid());
                        }, 200L);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        buttonView.setChecked(!isChecked);
                    })
                    .show();
        });
    }

    @Override
    protected ActivitySettingBinding onBinding() {
        return ActivitySettingBinding.inflate(getLayoutInflater());
    }
}