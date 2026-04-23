package top.niunaijun.blackboxa.view.base;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * 基础 Activity，提供通用工具栏初始化与当前用户获取。
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * 初始化工具栏标题与返回键。
     * @param toolbar Toolbar 实例
     * @param title 标题资源 ID
     * @param showBack 是否显示返回按钮
     * @param onBack 返回按钮回调（可为空，默认 finish 当前页）
     */
    protected void initToolbar(Toolbar toolbar, int title, boolean showBack, @Nullable Runnable onBack) {
        setSupportActionBar(toolbar);
        toolbar.setTitle(title);
        if (showBack && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> {
                if (onBack != null) onBack.run();
                finish();
            });
        }
    }

    /**
     * 获取当前页面携带的 userID 参数，默认 0。
     * @return 当前用户 ID
     */
    protected int currentUserID() {
        Intent intent = getIntent();
        return intent != null ? intent.getIntExtra("userID", 0) : 0;
    }
}
