package top.niunaijun.blackbox.utils.compat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import black.android.os.BRBundle;

/**
 * Bundle 与 Intent 的 Binder 兼容访问：在 API < 18 上通过反射桥接。
 */
public class BundleCompat {
    public static IBinder getBinder(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= 18) {
            return bundle.getBinder(key);
        } else {
            return BRBundle.get(bundle).getIBinder(key);
        }
    }

    public static void putBinder(Bundle bundle, String key, IBinder value) {
        if (Build.VERSION.SDK_INT >= 18) {
            bundle.putBinder(key, value);
        } else {
            BRBundle.get(bundle).putIBinder(key, value);
        }
    }

    /**
     * 将 Binder 作为额外 Bundle 放入 Intent，避免直放在部分系统上的序列化问题。
     */
    public static void putBinder(Intent intent, String key, IBinder value) {
        Bundle bundle = new Bundle();
        putBinder(bundle, "binder", value);
        intent.putExtra(key, bundle);
    }

    /**
     * 从 Intent 中取出 putBinder 存放的 Binder。
     */
    public static IBinder getBinder(Intent intent, String key) {
        Bundle bundle = intent.getBundleExtra(key);
        if (bundle != null) {
            return getBinder(bundle, "binder");
        }
        return null;
    }
}
