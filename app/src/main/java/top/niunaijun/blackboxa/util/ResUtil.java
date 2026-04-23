package top.niunaijun.blackboxa.util;

import android.content.Context;

import androidx.annotation.StringRes;

import top.niunaijun.blackboxa.app.App;

/**
 * 资源访问工具类：简化字符串资源获取与格式化。
 */
public final class ResUtil {
    private ResUtil() {}

    /** 获取字符串资源，支持可变参数格式化。*/
    public static String getString(@StringRes int id, String... args) {
        Context ctx = App.getContext();
        if (args == null || args.length == 0) {
            return ctx.getString(id);
        }
        return ctx.getString(id, (Object[]) args);
    }
}
