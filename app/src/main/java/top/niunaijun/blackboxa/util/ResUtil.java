package top.niunaijun.blackboxa.util;

import android.content.Context;

import androidx.annotation.StringRes;

import top.niunaijun.blackboxa.app.App;

public final class ResUtil {
    private ResUtil() {}

    public static String getString(@StringRes int id, String... args) {
        Context ctx = App.getContext();
        if (args == null || args.length == 0) {
            return ctx.getString(id);
        }
        return ctx.getString(id, (Object[]) args);
    }
}
