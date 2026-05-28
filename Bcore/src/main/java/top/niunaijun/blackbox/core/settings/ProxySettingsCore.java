package top.niunaijun.blackbox.core.settings;

import android.content.Context;
import android.content.SharedPreferences;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.Slog;

public final class ProxySettingsCore {
    private static final String KEY_PREFIX_PATH = "pathProxy:";
    private static final String KEY_PREFIX_CONTACTS = "contactsProxy:";
    private static final String SP_NAME = "proxy_settings";
    private static String TAG = "ProxySettingsCore";

    private ProxySettingsCore() {}

    private static SharedPreferences sp() {
        Context ctx = BlackBoxCore.getContext();
        return ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    private static String keyPath(int userId, String pkg) { return KEY_PREFIX_PATH + userId + ":" + pkg; }
    private static String keyContacts(int userId, String pkg) { return KEY_PREFIX_CONTACTS + userId + ":" + pkg; }

    public static boolean isPathEnabled(int userId, String pkg) {
        if (pkg == null) return false;
        Slog.e(TAG,"Path : userId: " +userId +" pkg: "+pkg);
        return sp().getBoolean(keyPath(userId, pkg), false);
    }

    public static boolean isContactsEnabled(int userId, String pkg) {
        if (pkg == null) return false;
        Slog.e(TAG,"Contacts : userId: " +userId +" pkg: "+pkg);
        return sp().getBoolean(keyContacts(userId, pkg), false);
    }

    public static void setPathEnabled(int userId, String pkg, boolean enabled) {
        if (pkg == null) return;
        Slog.e(TAG,"Path : userId: " +userId +" pkg: "+pkg+" en: "+enabled);
        sp().edit().putBoolean(keyPath(userId, pkg), enabled).apply();
    }

    public static void setContactsEnabled(int userId, String pkg, boolean enabled) {
        if (pkg == null) return;
        Slog.e(TAG,"Contacts : userId: " +userId +" pkg: "+pkg+" en: "+enabled);
        sp().edit().putBoolean(keyContacts(userId, pkg), enabled).apply();
    }

    public static boolean hasAnyConfig(int userId, String pkg) {
        if (pkg == null) return false;
        SharedPreferences s = sp();
        return s.contains(keyPath(userId, pkg)) || s.contains(keyContacts(userId, pkg));
    }
}
