package top.niunaijun.blackboxa.view.base;

import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.roger.catloadinglibrary.CatLoadingView;

import top.niunaijun.blackboxa.R;

public abstract class LoadingActivity extends BaseActivity {

    @Nullable
    private CatLoadingView loadingView;

    public void showLoading() {
        if (loadingView == null) {
            loadingView = new CatLoadingView();
        }
        if (!loadingView.isAdded()) {
            loadingView.setBackgroundColor(R.color.primary);
            loadingView.show(getSupportFragmentManager(), "");
            getSupportFragmentManager().executePendingTransactions();
            loadingView.setClickCancelAble(false);
            if (loadingView.getDialog() != null) {
                loadingView.getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                        return true; // consume back/escape
                    }
                    return false;
                });
            }
        }
    }

    public void hideLoading() {
        if (loadingView != null) {
            loadingView.dismiss();
        }
    }
}
