package top.niunaijun.blackboxa.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * Context 相关便捷方法集合。
 */
public final class ContextUtil {
    private ContextUtil() {}

    /**
     * 打开当前应用的系统设置页（应用详情）。
     */
    public static void openAppSystemSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        context.startActivity(intent);
    }
}
