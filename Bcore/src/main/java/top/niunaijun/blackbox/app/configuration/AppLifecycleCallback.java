package top.niunaijun.blackbox.app.configuration;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


/**
 * 应用生命周期回调：宿主可实现以接入沙盒各阶段事件。
 * 所有方法均为可选覆盖，默认空实现，避免侵入。
 */
public class AppLifecycleCallback implements Application.ActivityLifecycleCallbacks {
    public static AppLifecycleCallback EMPTY = new AppLifecycleCallback() {

    };

    /** 主进程启动APK前置回调 */
    public void beforeMainLaunchApk(String packageName, int userid) {

    }
    
    
    /** 当需要存储权限时回调，返回true表示已处理 */
    public boolean onStoragePermissionNeeded(String packageName, int userId) {
        
        return false;
    }

    /** Application attachBaseContext 前 */
    public void beforeMainApplicationAttach(Application app, Context context) {

    }

    /** Application attachBaseContext 后 */
    public void afterMainApplicationAttach(Application app, Context context) {

    }

    /** 目标主Activity onCreate 前 */
    public void beforeMainActivityOnCreate(Activity activity) {

    }

    /** 目标主Activity onCreate 后 */
    public void afterMainActivityOnCreate(Activity activity) {

    }

    /** Application 对象创建前 */
    public void beforeCreateApplication(String packageName, String processName, Context context, int userId) {

    }

    /** Application.onCreate 前 */
    public void beforeApplicationOnCreate(String packageName, String processName, Application application, int userId) {

    }

    /** Application.onCreate 后 */
    public void afterApplicationOnCreate(String packageName, String processName, Application application, int userId) {

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
