package top.niunaijun.blackboxa.app.rocker;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.imuxuan.floatingview.FloatingMagnetView;
import com.imuxuan.floatingview.FloatingView;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import top.niunaijun.blackbox.entity.location.BLocation;
import top.niunaijun.blackbox.fake.frameworks.BLocationManager;
import top.niunaijun.blackboxa.app.App;
import top.niunaijun.blackboxa.widget.EnFloatView;

/** 浮动式 GPS 摇杆管理器：用于更新虚拟定位。*/
public final class RockerManager {
    private static final String TAG = "RockerManager";
    /** 摇杆是否已初始化。*/
    private static boolean isInitialized = false;

    // 地球长半轴与短半轴（单位：米）
    private static final double Ea = 6378137.0; // 赤道半径
    private static final double Eb = 6356725.0; // 极半径

    private RockerManager() {}

    /** 初始化浮动摇杆与生命周期回调，按用户 ID 生效。*/
    public static void init(Application application, int userId) {
        try {
            if (isInitialized) {
                Log.d(TAG, "RockerManager 已初始化，跳过...");
                return;
            }
            if (application == null) {
                Log.w(TAG, "Application 为空，无法初始化 RockerManager");
                return;
            }
            if (!checkPermissions(application)) {
                Log.w(TAG, "缺少必要权限，RockerManager 无法初始化");
                Log.w(TAG, "请授予: " + String.join(", ", getRequiredPermissions()));
                return;
            }
            if (!BLocationManager.isFakeLocationEnable()) {
                Log.d(TAG, "未开启虚拟定位，RockerManager 不进行初始化");
                return;
            }

            Log.d(TAG, "初始化 RockerManager，userId: " + userId);

            FloatingMagnetView enFloatView = initFloatView();
            if (enFloatView instanceof EnFloatView) {
                // 回调参数：角度（度）与距离（米）
                ((EnFloatView) enFloatView).setListener((angle, distance) -> changeLocation(distance, angle, application.getPackageName(), userId));
                Log.d(TAG, "浮动视图初始化成功");
            } else {
                Log.w(TAG, "浮动视图初始化失败");
                return;
            }

            application.registerActivityLifecycleCallbacks(new BaseActivityLifecycleCallback() {
                @Override
                public void onActivityStarted(Activity activity) {
                    try {
                        FloatingView.get().attach(activity);
                        Log.d(TAG, "已向 Activity 附加浮动视图: " + activity.getClass().getSimpleName());
                    } catch (Exception e) {
                        Log.e(TAG, "附加浮动视图失败: " + e.getMessage());
                    }
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    try {
                        FloatingView.get().detach(activity);
                        Log.d(TAG, "已从 Activity 分离浮动视图: " + activity.getClass().getSimpleName());
                    } catch (Exception e) {
                        Log.e(TAG, "分离浮动视图失败: " + e.getMessage());
                    }
                }
            });

            isInitialized = true;
            Log.d(TAG, "RockerManager 初始化完成 - 浮动 GPS 摇杆已启用！");
        } catch (Exception e) {
            Log.e(TAG, "初始化 RockerManager 出错: " + e.getMessage(), e);
        }
    }

    /** 创建并注册浮动摇杆视图。*/
    private static FloatingMagnetView initFloatView() {
        try {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.START | Gravity.CENTER;
            EnFloatView view = new EnFloatView(App.getContext());
            view.setLayoutParams(params);
            FloatingView.get().customView(view);
            Log.d(TAG, "浮动视图创建成功");
            return FloatingView.get().getView();
        } catch (Exception e) {
            Log.e(TAG, "创建浮动视图出错: " + e.getMessage());
            return null;
        }
    }

    /** 根据距离（米）与角度（度）计算新的虚拟坐标。*/
    private static void changeLocation(float distance, float angle, String packageName, int userId) {
        try {
            BLocation location = BLocationManager.get().getLocation(userId, packageName);
            if (location == null) {
                Log.w(TAG, "未取得当前定位，pkg: " + packageName + ", userId: " + userId);
                return;
            }
            Log.d(TAG, "更新位置 - 距离: " + distance + "m, 角度: " + angle + "°, 当前: " + location.getLatitude() + ", " + location.getLongitude());

            // 局部位移（米）
            double dx = distance * Math.sin(angle * Math.PI / 180.0);
            double dy = distance * Math.cos(angle * Math.PI / 180.0);

            // 根据当前纬度修正地球曲率
            double ec = Eb + (Ea - Eb) * (90.0 - location.getLatitude()) / 90.0;
            double ed = ec * Math.cos(location.getLatitude() * Math.PI / 180.0);

            double newLng = (dx / ed + location.getLongitude() * Math.PI / 180.0) * 180.0 / Math.PI;
            double newLat = (dy / ec + location.getLatitude() * Math.PI / 180.0) * 180.0 / Math.PI;

            BLocation newLocation = new BLocation(newLat, newLng);
            BLocationManager.get().setLocation(userId, packageName, newLocation);
            Log.d(TAG, "位置已更新 - 新坐标: " + newLat + ", " + newLng);
        } catch (Exception e) {
            Log.e(TAG, "更新位置出错: " + e.getMessage(), e);
        }
    }

    /** 返回摇杆是否处于激活状态。*/
    public static boolean isActive() {
        return isInitialized;
    }

    /** 检查显示悬浮窗与定位权限是否具备。*/
    public static boolean checkPermissions(Context context) {
        try {
            boolean hasOverlayPermission = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasOverlayPermission = Settings.canDrawOverlays(context);
            }
            if (!hasOverlayPermission) {
                Log.w(TAG, "未授予悬浮窗权限，无法显示浮动摇杆");
                return false;
            }
            boolean hasLocationPermission = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasLocationPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
            }
            if (!hasLocationPermission) {
                Log.w(TAG, "未授予定位权限，无法获取位置");
                return false;
            }
            Log.d(TAG, "必要权限已具备");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "检查权限出错: " + e.getMessage());
            return false;
        }
    }

    /** 返回启用摇杆所需的权限列表。*/
    public static java.util.List<String> getRequiredPermissions() {
        return java.util.Arrays.asList(
                android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        );
    }

    /** 清理内部状态；在关闭摇杆时调用。*/
    public static void cleanup() {
        try {
            isInitialized = false;
            Log.d(TAG, "RockerManager 已清理");
        } catch (Exception e) {
            Log.e(TAG, "清理过程中出错: " + e.getMessage());
        }
    }
}
