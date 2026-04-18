package top.niunaijun.blackboxa.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import top.niunaijun.blackbox.BlackBoxCore;

public class App extends Application {
    private static volatile Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        try {
            super.attachBaseContext(base);

            try {
                BlackBoxCore.get().closeCodeInit();
            } catch (Exception e) {
                Log.e("App", "Error in closeCodeInit: " + e.getMessage(), e);
            }

            try {
                BlackBoxCore.get().onBeforeMainApplicationAttach(this, base);
            } catch (Exception e) {
                Log.e("App", "Error in onBeforeMainApplicationAttach: " + e.getMessage(), e);
            }

            mContext = base;

            try {
                AppManager.doAttachBaseContext(base);
            } catch (Exception e) {
                Log.e("App", "Error in doAttachBaseContext: " + e.getMessage(), e);
            }

            try {
                BlackBoxCore.get().onAfterMainApplicationAttach(this, base);
            } catch (Exception e) {
                Log.e("App", "Error in onAfterMainApplicationAttach: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            Log.e("App", "Critical error in attachBaseContext: " + e.getMessage(), e);
            if (base != null) {
                mContext = base;
            }
        }
    }

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            AppManager.doOnCreate(mContext);
        } catch (Exception e) {
            Log.e("App", "Error in onCreate: " + e.getMessage(), e);
        }
    }
}
