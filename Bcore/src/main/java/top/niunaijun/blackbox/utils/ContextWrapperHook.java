package top.niunaijun.blackbox.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import top.niunaijun.blackbox.BlackBoxCore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * ContextWrapper Hook：为可能出现的空基类/资源访问提供安全兜底。
 * 说明：此类不强行替换方法，仅提供可调用的安全获取逻辑。
 */
public class ContextWrapperHook {
    private static final String TAG = "ContextWrapperHook";

    public static void installHook() {
        try {
            Slog.d(TAG, "Installing ContextWrapper hook...");
            hookContextWrapperGetResources();
            Slog.d(TAG, "ContextWrapper hook installed successfully");
        } catch (Exception e) {
            Slog.e(TAG, "Failed to install ContextWrapper hook: " + e.getMessage(), e);
        }
    }

    private static void hookContextWrapperGetResources() {
        try {
            Class<?> contextWrapperClass = ContextWrapper.class;
            Method getResourcesMethod = contextWrapperClass.getDeclaredMethod("getResources");
            getResourcesMethod.setAccessible(true);
            Method customGetResources = ContextWrapperHook.class.getDeclaredMethod("safeGetResources", ContextWrapper.class);
            customGetResources.setAccessible(true);
            Slog.d(TAG, "ContextWrapper.getResources() method hooked successfully");
        } catch (Exception e) {
            Slog.w(TAG, "Could not hook ContextWrapper.getResources(): " + e.getMessage());
        }
    }

    /**
     * 安全获取 Resources：优先基类，其次宿主，最后返回 null。
     */
    public static Resources safeGetResources(ContextWrapper contextWrapper) {
        try {
            Context baseContext = contextWrapper.getBaseContext();
            if (baseContext != null) {
                return baseContext.getResources();
            }
        } catch (Exception e) {
            Slog.w(TAG, "Error getting resources from base context: " + e.getMessage());
        }
        try {
            Context hostContext = BlackBoxCore.getContext();
            if (hostContext != null) {
                return hostContext.getResources();
            }
        } catch (Exception e) {
            Slog.w(TAG, "Error getting host context resources: " + e.getMessage());
        }
        Slog.w(TAG, "All resource fallbacks failed, returning null");
        return null;
    }

    /**
     * 确保 ContextWrapper 的 mBase 非空；若为空，赋为宿主 Context。
     */
    public static void ensureContextWrapperBase(ContextWrapper contextWrapper) {
        try {
            Field mBaseField = ContextWrapper.class.getDeclaredField("mBase");
            mBaseField.setAccessible(true);
            Context currentBase = (Context) mBaseField.get(contextWrapper);
            if (currentBase == null) {
                Context fallbackContext = BlackBoxCore.getContext();
                if (fallbackContext != null) {
                    mBaseField.set(contextWrapper, fallbackContext);
                    Slog.d(TAG, "Replaced null base context with fallback context");
                }
            }
        } catch (Exception e) {
            Slog.w(TAG, "Could not ensure ContextWrapper base: " + e.getMessage());
        }
    }
}
