package top.niunaijun.blackboxa.view.main;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;
import top.niunaijun.blackboxa.app.App;

/**
 * BlackBox 核心加载器：配置项存取、生命周期回调注册与核心初始化封装。
 */
public class BlackBoxLoader {
    private static final String TAG = BlackBoxLoader.class.getSimpleName();
    private static final String PREFS = "blackbox_loader";

    private final SharedPreferences prefs = App.getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

    private boolean getBool(String key, boolean def) {
        try { return prefs.getBoolean(key, def); } catch (Exception e) { Log.e(TAG, "Pref get error: " + e.getMessage()); return def; }
    }
    private void setBool(String key, boolean val) {
        try { prefs.edit().putBoolean(key, val).apply(); } catch (Exception e) { Log.e(TAG, "Pref set error: " + e.getMessage()); }
    }

    /** 是否隐藏 Root 环境。*/
    public boolean hideRoot() { return getBool("hide_root", false); }
    /** 设置隐藏 Root 开关。*/
    public void invalidHideRoot(boolean v) { setBool("hide_root", v); }

    /** 是否禁用 FLAG_SECURE（允许截图/录屏）。*/
    public boolean disableFlagSecure() { return getBool("disable_flag_secure", false); }
    /** 设置是否禁用 FLAG_SECURE。*/
    public void invalidDisableFlagSecure(boolean v) { setBool("disable_flag_secure", v); }

    /** 是否启用守护服务。*/
    public boolean daemonEnable() { return getBool("daemon_enable", false); }
    /** 设置是否启用守护服务。*/
    public void invalidDaemonEnable(boolean v) { setBool("daemon_enable", v); }

    /** 是否显示快捷方式权限提示对话框。*/
    public boolean showShortcutPermissionDialog() { return getBool("show_shortcut_permission_dialog", true); }
    /** 设置是否显示快捷方式权限提示对话框。*/
    public void invalidShortcutPermissionDialog(boolean v) { setBool("show_shortcut_permission_dialog", v); }

    /** 是否使用 VPN 网络。*/
    public boolean useVpnNetwork() { return getBool("use_vpn_network", false); }
    /** 设置是否使用 VPN 网络。*/
    public void invalidUseVpnNetwork(boolean v) { setBool("use_vpn_network", v); }

    /** 获取 BlackBoxCore 单例。*/
    public BlackBoxCore getBlackBoxCore() {
        try {
            return BlackBoxCore.get();
        } catch (Exception e) {
            Log.e(TAG, "Error getting BlackBoxCore: " + e.getMessage());
            throw e;
        }
    }

    /** 注册 App 生命周期回调（可在应用 onCreate 后附加功能）。*/
    public void addLifecycleCallback() {
        try {
            BlackBoxCore.get().addAppLifecycleCallback(new AppLifecycleCallback() {
                @Override
                public void beforeCreateApplication(String packageName, String processName, Context context, int userId) {
                    try {
                        Log.d(TAG, "beforeCreateApplication: pkg " + packageName + ", processName " + processName + ",userID:" + BActivityThread.getUserId());
                    } catch (Exception e) {
                        Log.e(TAG, "Error in beforeCreateApplication: " + e.getMessage());
                    }
                }

                @Override
                public void beforeApplicationOnCreate(String packageName, String processName, Application application, int userId) {
                    try {
                        Log.d(TAG, "beforeApplicationOnCreate: pkg " + packageName + ", processName " + processName);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in beforeApplicationOnCreate: " + e.getMessage());
                    }
                }

                @Override
                public void afterApplicationOnCreate(String packageName, String processName, Application application, int userId) {
                    try {
                        Log.d(TAG, "afterApplicationOnCreate: pkg " + packageName + ", processName " + processName);
                        // RockerManager.init(application, userId) via reflection (class may be absent)
                        try {
                            Class<?> clazz = Class.forName("top.niunaijun.blackboxa.app.rocker.RockerManager");
                            Method init = clazz.getMethod("init", Application.class, int.class);
                            init.invoke(null, application, userId);
                        } catch (Throwable ignore) { /* optional */ }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in afterApplicationOnCreate: " + e.getMessage());
                    }
                }

                @Override
                public boolean onStoragePermissionNeeded(String packageName, int userId) {
                    try {
                        Log.w(TAG, "Storage permission needed for launching: " + packageName);
                        Intent intent = new Intent("top.niunaijun.blackboxa.REQUEST_STORAGE_PERMISSION");
                        intent.putExtra("package_name", packageName);
                        intent.putExtra("user_id", userId);
                        intent.setPackage(App.getContext().getPackageName());
                        App.getContext().sendBroadcast(intent);
                        return false;
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onStoragePermissionNeeded: " + e.getMessage());
                        return false;
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error adding lifecycle callback: " + e.getMessage());
        }
    }

    /** 封装 BlackBoxCore.doAttachBaseContext 的配置与调用。*/
    public void attachBaseContext(Context context) {
        try {
            BlackBoxCore.get().doAttachBaseContext(context, new ClientConfiguration() {
                @Override public String getHostPackageName() {
                    try { return context.getPackageName(); } catch (Exception e) { Log.e(TAG, "Error getting package name: " + e.getMessage()); return "unknown"; }
                }
                @Override public boolean isHideRoot() {
                    return hideRoot();
                }
                @Override public boolean isEnableDaemonService() {
                    return daemonEnable();
                }
                @Override public boolean isUseVpnNetwork() {
                    return useVpnNetwork();
                }
                @Override public boolean isDisableFlagSecure() {
                    return disableFlagSecure();
                }
                @Override public boolean requestInstallPackage(File file, int userId) {
                    try {
                        if (file == null) { Log.w(TAG, "requestInstallPackage: file is null"); return false; }
                        context.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 0);
                        return false;
                    } catch (Exception e) { Log.e(TAG, "Error in requestInstallPackage: " + e.getMessage()); return false; }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in attachBaseContext: " + e.getMessage());
        }
    }

    /** BlackBoxCore.doCreate 包装，注册服务可用回调。*/
    public void doOnCreate(Context context) {
        try {
            BlackBoxCore.get().doCreate();
            try {
                BlackBoxCore.get().addServiceAvailableCallback(new Runnable() {
                    @Override public void run() {
                        Log.d(TAG, "Services became available, triggering app list refresh");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error registering service available callback: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in doOnCreate: " + e.getMessage());
        }
    }
}
