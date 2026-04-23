package top.niunaijun.blackbox.utils.compat;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.WindowManager;

import black.android.app.BRActivity;
import black.com.android.internal.BRRstyleable;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.utils.DrawableUtils;

/**
 * Activity 兼容修复：
 * - 应用 Window 样式中壁纸/全屏标志；
 * - 在 L+ 设置任务标签与图标（带用户前缀）。
 */
public class ActivityCompat {

    public static void fix(Activity activity) {
        // 触发 mActivityInfo 初始化，避免后续访问空指针
        BRActivity.get(activity).mActivityInfo();

        Context baseContext = activity.getBaseContext();
        try {
            TypedArray typedArray = activity.obtainStyledAttributes((BRRstyleable.get().Window()));
            if (typedArray != null) {
                boolean showWallpaper = typedArray.getBoolean(BRRstyleable.get().Window_windowShowWallpaper(),
                        false);
                if (showWallpaper) {
                    activity.getWindow().setBackgroundDrawable(WallpaperManager.getInstance(activity).getDrawable());
                }
                boolean fullscreen = typedArray.getBoolean(BRRstyleable.get().Window_windowFullscreen(), false);
                if (fullscreen) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                typedArray.recycle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = activity.getIntent();
            ApplicationInfo applicationInfo = baseContext.getApplicationInfo();
            PackageManager pm = activity.getPackageManager();
            if (intent != null && activity.isTaskRoot()) {
                try {
                    String label = TaskDescriptionCompat.getTaskDescriptionLabel(
                            BlackBoxCore.getUserId(), applicationInfo.loadLabel(pm));

                    Bitmap icon = null;
                    Drawable drawable = getActivityIcon(activity);
                    if (drawable != null) {
                        ActivityManager am = (ActivityManager) baseContext.getSystemService(Context.ACTIVITY_SERVICE);
                        int iconSize = am.getLauncherLargeIconSize();
                        icon = DrawableUtils.drawableToBitmap(drawable, iconSize, iconSize);
                    }

                    activity.setTaskDescription(new ActivityManager.TaskDescription(label, icon));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Drawable getActivityIcon(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        try {
            Drawable icon = pm.getActivityIcon(activity.getComponentName());
            if (icon != null)
                return icon;
        } catch (PackageManager.NameNotFoundException ignore) {
        }

        ApplicationInfo applicationInfo = activity.getApplicationInfo();
        return applicationInfo.loadIcon(pm);
    }
}
