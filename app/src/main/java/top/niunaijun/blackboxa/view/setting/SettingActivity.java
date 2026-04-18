package top.niunaijun.blackboxa.view.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.databinding.ActivitySettingBinding;
import top.niunaijun.blackboxa.view.base.BaseActivity;

public class SettingActivity extends BaseActivity {

    private ActivitySettingBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.setting, true, null);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new SettingFragment())
                .commit();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        context.startActivity(intent);
    }
}
