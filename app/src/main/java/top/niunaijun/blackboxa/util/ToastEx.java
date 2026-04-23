package top.niunaijun.blackboxa.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

import top.niunaijun.blackboxa.app.App;

/**
 * Toast 工具类：保证前一条提示被取消后再展示新提示。
 */
public final class ToastEx {
    private static Toast toastImpl;

    private ToastEx() {}

    /** 使用指定 Context 显示短 Toast。*/
    public static void toast(Context context, String msg) {
        if (toastImpl != null) toastImpl.cancel();
        toastImpl = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toastImpl.show();
    }

    /** 使用全局 Context 显示短 Toast（字符串）。*/
    public static void toast(String msg) {
        toast(App.getContext(), msg);
    }

    /** 使用全局 Context 显示短 Toast（字符串资源）。*/
    public static void toast(@StringRes int msgID) {
        toast(ResUtil.getString(msgID));
    }
}
