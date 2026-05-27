package top.niunaijun.blackbox.fake.frameworks;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.fake.hook.HookManager;
import top.niunaijun.blackbox.fake.hook.IInjectHook;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;


/**
 * 资源框架代理：
 * - 资源/图标/标签的安全加载工具，遇异常回退到包名/空对象；
 * - 提供创建安全 ResourcesManager 与问题 overlay 路径识别。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BResourcesManager implements IInjectHook {
    private static final String TAG = "BResourcesManager";

    @Override
    public void injectHook() {
        // 这里可注入与资源相关的 hook，目前仅保留占位实现
        Log.d(TAG, "BResourcesManager hook initialized");
    }

    @Override
    public boolean isBadEnv() {
        return false; // 资源加载环境正常
    }

    /**
     * 安全获取应用标签（尽可能避免抛异常）。
     */
    public static String safeLoadAppLabel(Object applicationInfo) {
        if (applicationInfo == null) {
            return "Unknown App";
        }

        // 先取包名作为回退
        String packageName = getPackageNameSafely(applicationInfo);

        // 包名存在即可使用
        if (packageName != null && !packageName.isEmpty()) {
            return packageName;
        }

        try {
            // 1. 尝试 labelRes 资源
            Method getLabelResMethod = applicationInfo.getClass().getMethod("getLabelRes");
            Integer labelRes = (Integer) getLabelResMethod.invoke(applicationInfo);

            if (labelRes != null && labelRes != 0) {
                Object packageManager = getPackageManager();
                if (packageManager != null) {
                    Method getTextMethod = packageManager.getClass().getMethod("getText", String.class, int.class, android.content.pm.ApplicationInfo.class);
                    Object label = getTextMethod.invoke(packageManager, packageName, labelRes, applicationInfo);
                    if (label != null) {
                        return label.toString();
                    }
                }
            }

            // 2. 尝试 loadLabel
            Method loadLabelMethod = applicationInfo.getClass().getMethod("loadLabel", android.content.pm.PackageManager.class);
            Object packageManager = getPackageManager();
            if (packageManager != null) {
                Object label = loadLabelMethod.invoke(applicationInfo, packageManager);
                if (label != null) {
                    return label.toString();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to load app label: " + e.getMessage());
        }

        return "Unknown App";
    }

    /**
     * 尽量安全地获取包名。
     */
    private static String getPackageNameSafely(Object applicationInfo) {
        // 1. 直接字段
        try {
            Field packageNameField = applicationInfo.getClass().getDeclaredField("packageName");
            packageNameField.setAccessible(true);
            Object packageName = packageNameField.get(applicationInfo);
            if (packageName != null) {
                return packageName.toString();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get package name via field: " + e.getMessage());
        }

        // 2. getter 方法
        try {
            Method getPackageNameMethod = applicationInfo.getClass().getMethod("getPackageName");
            Object packageName = getPackageNameMethod.invoke(applicationInfo);
            if (packageName != null) {
                return packageName.toString();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get package name via method: " + e.getMessage());
        }

        // 3. 兜底：解析 toString
        try {
            String toString = applicationInfo.toString();
            if (toString.contains("packageName=")) {
                int start = toString.indexOf("packageName=") + 12;
                int end = toString.indexOf(" ", start);
                if (end == -1) end = toString.length();
                return toString.substring(start, end);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get package name via toString: " + e.getMessage());
        }

        return null;
    }

    /**
     * 安全获取应用图标 Drawable（失败返回 null）。
     */
    public static Object safeLoadAppIcon(Object applicationInfo) {
        try {
            if (applicationInfo != null) {
                // 1. 通过 iconRes 尝试
                Method getIconMethod = applicationInfo.getClass().getMethod("getIcon");
                Integer iconRes = (Integer) getIconMethod.invoke(applicationInfo);

                if (iconRes != null && iconRes != 0) {
                    Object packageManager = getPackageManager();
                    if (packageManager != null) {
                        try {
                            Method getDrawableMethod = packageManager.getClass().getMethod("getDrawable", String.class, int.class, android.content.pm.ApplicationInfo.class);
                            Object drawable = getDrawableMethod.invoke(packageManager, getPackageName(applicationInfo), iconRes, applicationInfo);
                            if (drawable != null) {
                                return drawable;
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Failed to load icon via getDrawable: " + e.getMessage());
                        }
                    }
                }

                // 2. 回退到 loadIcon
                Method loadIconMethod = applicationInfo.getClass().getMethod("loadIcon", android.content.pm.PackageManager.class);
                Object packageManager = getPackageManager();
                if (packageManager != null) {
                    return loadIconMethod.invoke(applicationInfo, packageManager);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to load app icon: " + e.getMessage());
        }
        return null;
    }

    /** 包名（空安全） */
    private static String getPackageName(Object applicationInfo) {
        String packageName = getPackageNameSafely(applicationInfo);
        return packageName != null ? packageName : "";
    }

    /** 反射获取宿主 PackageManager */
    private static Object getPackageManager() {
        try {
            Class<?> blackBoxCoreClass = Class.forName("top.niunaijun.blackbox.BlackBoxCore");
            Method getPackageManagerMethod = blackBoxCoreClass.getMethod("getPackageManager");
            return getPackageManagerMethod.invoke(null);
        } catch (Exception e) {
            Log.w(TAG, "Failed to get PackageManager: " + e.getMessage());
            return null;
        }
    }

    /**
     * 创建更安全的 ResourcesManager 实例，尽量避免 overlay 干扰。
     */
    public static Object createSafeResourceManager(Context context) {
        try {
            // Android 内部类，可能在部分版本存在
            Class<?> resourcesManagerClass = Class.forName("android.app.ResourcesManager");
            Object resourcesManager = resourcesManagerClass.newInstance();

            // 禁止 overlay 加载（若字段存在）
            try {
                Field disableOverlayField = resourcesManagerClass.getDeclaredField("mDisableOverlayLoading");
                disableOverlayField.setAccessible(true);
                disableOverlayField.setBoolean(resourcesManager, true);
            } catch (Exception e) {
                Log.w(TAG, "Could not set overlay loading flag: " + e.getMessage());
            }

            return resourcesManager;
        } catch (Exception e) {
            Log.w(TAG, "Failed to create safe resource manager: " + e.getMessage());
            return null;
        }
    }

    /**
     * 简单识别问题 overlay 路径（缓存 FRRO 文件）。
     */
    public static boolean isProblematicOverlayPath(String path) {
        return path != null && path.contains("/data/resource-cache/") && path.contains(".frro");
    }
}
