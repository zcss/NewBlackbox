package top.niunaijun.blackboxa.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackboxa.view.main.BlackBoxLoader;

public final class AppManager {
    private static final String TAG = "AppManager";

    private static volatile BlackBoxLoader mBlackBoxLoader;
    private static volatile SharedPreferences mRemarkSharedPreferences;

    private AppManager() {}

    public static BlackBoxLoader getMBlackBoxLoader() {
        if (mBlackBoxLoader == null) {
            synchronized (AppManager.class) {
                if (mBlackBoxLoader == null) {
                    try {
                        mBlackBoxLoader = new BlackBoxLoader();
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating BlackBoxLoader: " + e.getMessage());
                        // Fallback to default instance to avoid crash
                        mBlackBoxLoader = new BlackBoxLoader();
                    }
                }
            }
        }
        return mBlackBoxLoader;
    }

    public static BlackBoxCore getMBlackBoxCore() {
        try {
            return getMBlackBoxLoader().getBlackBoxCore();
        } catch (Exception e) {
            Log.e(TAG, "Error getting BlackBoxCore: " + e.getMessage());
            throw e;
        }
    }

    public static SharedPreferences getMRemarkSharedPreferences() {
        if (mRemarkSharedPreferences == null) {
            synchronized (AppManager.class) {
                if (mRemarkSharedPreferences == null) {
                    try {
                        mRemarkSharedPreferences = App.getContext().getSharedPreferences("UserRemark", Context.MODE_PRIVATE);
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating SharedPreferences: " + e.getMessage());
                        throw e;
                    }
                }
            }
        }
        return mRemarkSharedPreferences;
    }

    public static void doAttachBaseContext(Context context) {
        try {
            getMBlackBoxLoader().attachBaseContext(context);
            getMBlackBoxLoader().addLifecycleCallback();
        } catch (Exception e) {
            Log.e(TAG, "Error in doAttachBaseContext: " + e.getMessage());
        }
    }

    public static void doOnCreate(Context context) {
        try {
            getMBlackBoxLoader().doOnCreate(context);
            initThirdService(context);
        } catch (Exception e) {
            Log.e(TAG, "Error in doOnCreate: " + e.getMessage());
        }
    }

    private static void initThirdService(Context context) {
        try {
            // Reserved for third-party service initialization
        } catch (Exception e) {
            Log.e(TAG, "Error in initThirdService: " + e.getMessage());
        }
    }
}
