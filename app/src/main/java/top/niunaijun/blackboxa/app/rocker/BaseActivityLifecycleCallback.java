package top.niunaijun.blackboxa.app.rocker;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/** 提供 ActivityLifecycleCallbacks 的空实现，便于按需覆写。*/
public interface BaseActivityLifecycleCallback extends Application.ActivityLifecycleCallbacks {
    @Override
    default void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    default void onActivityStarted(Activity activity) {}

    @Override
    default void onActivityResumed(Activity activity) {}

    @Override
    default void onActivityPaused(Activity activity) {}

    @Override
    default void onActivityStopped(Activity activity) {}

    @Override
    default void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    default void onActivityDestroyed(Activity activity) {}
}
