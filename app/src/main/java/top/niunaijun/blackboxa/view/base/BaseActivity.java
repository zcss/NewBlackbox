package top.niunaijun.blackboxa.view.base;

import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaseActivity extends AppCompatActivity {

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

    protected int currentUserID() {
        Intent intent = getIntent();
        return intent != null ? intent.getIntExtra("userID", 0) : 0;
    }
}
