package top.niunaijun.blackboxa.view.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import top.niunaijun.blackbox.BlackBoxCore;

/**
 * 快捷方式启动页：读取包名与用户 ID 后后台拉起目标 App。
 */
public class ShortcutActivity extends AppCompatActivity {
    @Override
    /** 后台线程调用 BlackBoxCore.launchApk，完成后关闭当前页。*/
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String pkg = getIntent().getStringExtra("pkg");
        final int userID = getIntent().getIntExtra("userId", 0);

        // Run in background similar to lifecycleScope.launch in Kotlin
        new Thread(() -> {
            try {
                BlackBoxCore.get().launchApk(pkg, userID);
            } finally {
                runOnUiThread(this::finish);
            }
        }, "ShortcutActivity-Launcher").start();
    }
}
