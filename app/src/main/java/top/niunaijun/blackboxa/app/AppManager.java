package top.niunaijun.blackboxa.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackboxa.view.main.BlackBoxLoader;

/** BlackBoxA 应用层总线：负责 Loader、Core 与首选项管理。*/
public final class AppManager {
    private static final String TAG = "AppManager";

    /** BlackBoxCore 的桥接加载器（懒加载单例）。*/
    private static volatile BlackBoxLoader mBlackBoxLoader;
    /** 保存用户备注/标记的首选项实例（懒加载单例）。*/
    private static volatile SharedPreferences mRemarkSharedPreferences;

    private AppManager() {}

    /** 获取 BlackBoxLoader 单例（线程安全）。*/
    public static BlackBoxLoader getMBlackBoxLoader() {
        if (mBlackBoxLoader == null) {
            synchronized (AppManager.class) {
                if (mBlackBoxLoader == null) {
                    try {
                        mBlackBoxLoader = new BlackBoxLoader();
                    } catch (Exception e) {
                        Log.e(TAG, "创建 BlackBoxLoader 失败: " + e.getMessage());
                        // 兜底以避免崩溃
                        mBlackBoxLoader = new BlackBoxLoader();
                    }
                }
            }
        }
        return mBlackBoxLoader;
    }

    /** 通过 Loader 暴露 BlackBoxCore 实例。*/
    public static BlackBoxCore getMBlackBoxCore() {
        try {
            return getMBlackBoxLoader().getBlackBoxCore();
        } catch (Exception e) {
            Log.e(TAG, "获取 BlackBoxCore 失败: " + e.getMessage());
            throw e;
        }
    }

    /** 懒初始化用户备注首选项。*/
    public static SharedPreferences getMRemarkSharedPreferences() {
        if (mRemarkSharedPreferences == null) {
            synchronized (AppManager.class) {
                if (mRemarkSharedPreferences == null) {
                    try {
                        mRemarkSharedPreferences = App.getContext().getSharedPreferences("UserRemark", Context.MODE_PRIVATE);
                    } catch (Exception e) {
                        Log.e(TAG, "创建 SharedPreferences 失败: " + e.getMessage());
                        throw e;
                    }
                }
            }
        }
        return mRemarkSharedPreferences;
    }

    /** 绑定基础上下文并注册全局生命周期回调。*/
    public static void doAttachBaseContext(Context context) {
        try {
            getMBlackBoxLoader().attachBaseContext(context);
            getMBlackBoxLoader().addLifecycleCallback();
        } catch (Exception e) {
            Log.e(TAG, "doAttachBaseContext 异常: " + e.getMessage());
        }
    }

    /** 执行应用级初始化与第三方服务预留初始化。*/
    public static void doOnCreate(Context context) {
        try {
            getMBlackBoxLoader().doOnCreate(context);
            initThirdService(context);
        } catch (Exception e) {
            Log.e(TAG, "doOnCreate 异常: " + e.getMessage());
        }
    }

    /** 预留第三方 SDK/服务初始化入口。*/
    private static void initThirdService(Context context) {
        try {
            // 第三方服务初始化留空，由业务自行扩展
        } catch (Exception e) {
            Log.e(TAG, "initThirdService 异常: " + e.getMessage());
        }
    }
}
