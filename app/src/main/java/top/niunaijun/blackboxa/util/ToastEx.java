package top.niunaijun.blackboxa.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

import top.niunaijun.blackboxa.app.App;

public final class ToastEx {
    private static Toast toastImpl;

    private ToastEx() {}

    public static void toast(Context context, String msg) {
        if (toastImpl != null) toastImpl.cancel();
        toastImpl = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toastImpl.show();
    }

    public static void toast(String msg) {
        toast(App.getContext(), msg);
    }

    public static void toast(@StringRes int msgID) {
        toast(ResUtil.getString(msgID));
    }
}
