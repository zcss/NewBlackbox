package top.niunaijun.blackbox.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import top.niunaijun.blackbox.BlackBoxCore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import android.content.Intent;
import android.util.Log;
import java.util.List;

/**
 * 系统 Hook 管理：尝试安装若干系统级代理/监听，增强稳定性（如配置变更、事务监听）。
 * 注：此类主要为防御性 Hook，适配不同系统版本的差异，尽量避免抛异常导致宿主崩溃。
 */
public class SystemHookManager {
    private static final String TAG = "SystemHookManager";

    /** 安装所有 Hook（尝试型）。 */
    public static void installAllHooks() {
        try {
            Log.d(TAG, "Installing system hooks...");

            // 事务监听控制器
            hookClientTransactionListenerController();
            // 配置控制器
            hookConfigurationController();
            // ActivityThread 关键方法
            hookActivityThread();

            Log.d(TAG, "System hooks installed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to install system hooks: " + e.getMessage(), e);
        }
    }

    private static void hookClientTransactionListenerController() {
        try {
            Class<?> controllerClass = Class.forName("android.app.servertransaction.ClientTransactionListenerController");
            if (controllerClass != null) {
                Log.d(TAG, "Found ClientTransactionListenerController class");

                Object proxy = Proxy.newProxyInstance(
                    controllerClass.getClassLoader(),
                    new Class<?>[] { controllerClass },
                    (proxyObj, method, args) -> {
                        if ("onContextConfigurationPreChanged".equals(method.getName())) {
                            try {
                                // 配置变更前，确保 Activity 拥有有效 Context
                                ensureAllActivitiesHaveContext();
                                if (args != null && args.length > 0) {
                                    return method.invoke(proxyObj, args);
                                } else {
                                    Log.w(TAG, "ClientTransactionListenerController called with null args, skipping");
                                    return null;
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error in ClientTransactionListenerController proxy: " + e.getMessage());
                                return null;
                            }
                        }
                        return method.invoke(proxyObj, args);
                    }
                );

                replaceControllerInstance(controllerClass, proxy, "ClientTransactionListenerController");
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not hook ClientTransactionListenerController: " + e.getMessage());
        }
    }

    private static void hookConfigurationController() {
        try {
            Class<?> controllerClass = Class.forName("android.app.ConfigurationController");
            if (controllerClass != null) {
                Log.d(TAG, "Found ConfigurationController class");

                Object proxy = Proxy.newProxyInstance(
                    controllerClass.getClassLoader(),
                    new Class<?>[] { controllerClass },
                    (proxyObj, method, args) -> {
                        if ("handleConfigurationChanged".equals(method.getName())) {
                            try {
                                ensureAllActivitiesHaveContext();
                                if (args != null && args.length > 0) {
                                    return method.invoke(proxyObj, args);
                                } else {
                                    Log.w(TAG, "ConfigurationController called with null args, skipping");
                                    return null;
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error in ConfigurationController proxy: " + e.getMessage());
                                return null;
                            }
                        }
                        return method.invoke(proxyObj, args);
                    }
                );

                replaceControllerInstance(controllerClass, proxy, "ConfigurationController");
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not hook ConfigurationController: " + e.getMessage());
        }
    }

    private static void hookActivityThread() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            if (activityThreadClass != null) {
                Log.d(TAG, "Found ActivityThread class");

                Object activityThread = BlackBoxCore.mainThread();
                if (activityThread != null) {
                    Log.d(TAG, "Found ActivityThread instance");
                    try {
                        Method handleLaunchActivity = activityThreadClass.getDeclaredMethod(
                            "handleLaunchActivity",
                            Object.class,
                            Intent.class,
                            Object.class,
                            Object.class,
                            Object.class,
                            String.class,
                            Object.class,
                            Object.class,
                            Object.class,
                            List.class,
                            List.class,
                            boolean.class,
                            boolean.class,
                            Object.class
                        );
                        if (handleLaunchActivity != null) {
                            Log.d(TAG, "Found handleLaunchActivity method");
                            handleLaunchActivity.setAccessible(true);
                            // 这里只做存在性校验与可访问性设置，避免强行替换导致不兼容
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Could not hook handleLaunchActivity: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not hook ActivityThread: " + e.getMessage());
        }
    }

    private static void replaceControllerInstance(Class<?> controllerClass, Object proxy, String controllerName) {
        try {
            Object activityThread = BlackBoxCore.mainThread();
            if (activityThread != null) {
                Field[] fields = activityThread.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(controllerClass)) {
                        field.setAccessible(true);
                        Object currentInstance = field.get(activityThread);
                        if (currentInstance != null) {
                            field.set(activityThread, proxy);
                            Log.d(TAG, "Successfully replaced " + controllerName + " instance");
                            return;
                        }
                    }
                }

                Log.w(TAG, "Could not find " + controllerName + " field, trying to create new instance");
                try {
                    java.lang.reflect.Constructor<?>[] constructors = controllerClass.getDeclaredConstructors();
                    if (constructors.length > 0) {
                        constructors[0].setAccessible(true);
                        Object newInstance = constructors[0].newInstance();
                        if (newInstance != null) {
                            Log.d(TAG, "Created new " + controllerName + " instance");
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not create new " + controllerName + " instance: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not replace " + controllerName + " instance: " + e.getMessage());
        }
    }

    /**
     * 遍历 ActivityThread 中的 Activity 记录，为其确保有效 Context（适配代理环境）。
     */
    private static void ensureAllActivitiesHaveContext() {
        try {
            Object activityThread = BlackBoxCore.mainThread();
            if (activityThread != null) {
                try {
                    Field[] fields = activityThread.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType().getName().contains("ArrayMap") ||
                            field.getType().getName().contains("HashMap")) {
                            field.setAccessible(true);
                            Object activityRecords = field.get(activityThread);
                            if (activityRecords != null) {
                                try {
                                    Method valuesMethod = activityRecords.getClass().getMethod("values");
                                    Object values = valuesMethod.invoke(activityRecords);
                                    if (values instanceof java.util.Collection) {
                                        for (Object record : (java.util.Collection<?>) values) {
                                            if (record != null) {
                                                try {
                                                    Field activityField = record.getClass().getDeclaredField("activity");
                                                    activityField.setAccessible(true);
                                                    Object activity = activityField.get(record);
                                                    if (activity instanceof android.app.Activity) {
                                                        BlackBoxCore.ensureActivityContext((android.app.Activity) activity);
                                                    }
                                                } catch (Exception e) {
                                                    // 忽略单条失败，继续其他记录
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Could not iterate through activity records: " + e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not access activity records: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error ensuring all activities have context: " + e.getMessage());
        }
    }
}
