package top.niunaijun.blackboxa.view.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.util.InjectionUtil;
import top.niunaijun.blackboxa.view.list.ListViewModel;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        jump();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previewInstalledAppList();
        jump();
    }

    private void jump() {
        MainActivity.start(this);
        finish();
    }

    private void previewInstalledAppList() {
        ListViewModel viewModel = new ViewModelProvider(this, InjectionUtil.getListFactory())
                .get(ListViewModel.class);
        viewModel.previewInstalledList();
    }
}
