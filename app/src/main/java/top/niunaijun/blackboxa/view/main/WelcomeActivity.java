package top.niunaijun.blackboxa.view.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.util.InjectionUtil;
import top.niunaijun.blackboxa.view.list.ListViewModel;

/**
 * 启动欢迎页：预热应用列表数据后跳转主界面。
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    /** 新 Intent 到达时继续执行跳转逻辑。*/
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        jump();
    }

    @Override
    /** 首次启动时预览列表并跳转主界面。*/
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previewInstalledAppList();
        jump();
    }

    /** 跳转到主页面并关闭当前页。*/
    private void jump() {
        MainActivity.start(this);
        finish();
    }

    /** 预加载已安装应用列表以加快后续展示。*/
    private void previewInstalledAppList() {
        ListViewModel viewModel = new ViewModelProvider(this, InjectionUtil.getListFactory())
                .get(ListViewModel.class);
        viewModel.previewInstalledList();
    }
}
