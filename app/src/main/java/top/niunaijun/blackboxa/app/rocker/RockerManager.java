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

public final class RockerManager {
    private static final String TAG = "RockerManager";
    private static boolean isInitialized = false;

    // Earth's semi-major and semi-minor axes (meters)
    private static final double Ea = 6378137.0; // Equatorial radius
    private static final double Eb = 6356725.0; // Polar radius

    private RockerManager() {}

    public static void init(Application application, int userId) {
        try {
            if (isInitialized) {
                Log.d(TAG, "RockerManager already initialized, skipping...");
                return;
            }
            if (application == null) {
                Log.w(TAG, "Application is null, cannot initialize RockerManager");
                return;
            }
            if (!checkPermissions(application)) {
                Log.w(TAG, "Required permissions not granted, RockerManager cannot initialize");
                Log.w(TAG, "Please grant: " + String.join(", ", getRequiredPermissions()));
                return;
            }
            if (!BLocationManager.isFakeLocationEnable()) {
                Log.d(TAG, "Fake location is not enabled, RockerManager will not initialize");
                return;
            }

            Log.d(TAG, "Initializing RockerManager for userId: " + userId);

            FloatingMagnetView enFloatView = initFloatView();
            if (enFloatView instanceof EnFloatView) {
//                ((EnFloatView) enFloatView).setListener((angle, distance) ->
//                        changeLocation(distance, angle, application.getPackageName(), userId));
//                ((EnFloatView) enFloatView).setListener(new Function2<Float, Float, Unit>() {
//                    @Override
//                    public Unit invoke(Float angle, Float distance) {
//                        changeLocation(distance, angle, application.getPackageName(), userId);
//                        return null;
//                    }
//                });
                ((EnFloatView) enFloatView).setListener((angle, distance) -> changeLocation(distance, angle, application.getPackageName(), userId));
                Log.d(TAG, "Floating view initialized successfully");
            } else {
                Log.w(TAG, "Failed to initialize floating view");
                return;
            }

            application.registerActivityLifecycleCallbacks(new BaseActivityLifecycleCallback() {
                @Override
                public void onActivityStarted(Activity activity) {
                    try {
                        FloatingView.get().attach(activity);
                        Log.d(TAG, "Floating view attached to activity: " + activity.getClass().getSimpleName());
                    } catch (Exception e) {
                        Log.e(TAG, "Error attaching floating view to activity: " + e.getMessage());
                    }
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    try {
                        FloatingView.get().detach(activity);
                        Log.d(TAG, "Floating view detached from activity: " + activity.getClass().getSimpleName());
                    } catch (Exception e) {
                        Log.e(TAG, "Error detaching floating view from activity: " + e.getMessage());
                    }
                }
            });

            isInitialized = true;
            Log.d(TAG, "RockerManager initialized successfully - Floating GPS joystick is now active!");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing RockerManager: " + e.getMessage(), e);
        }
    }

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
            Log.d(TAG, "Floating view created successfully");
            return FloatingView.get().getView();
        } catch (Exception e) {
            Log.e(TAG, "Error creating floating view: " + e.getMessage());
            return null;
        }
    }

    private static void changeLocation(float distance, float angle, String packageName, int userId) {
        try {
            BLocation location = BLocationManager.get().getLocation(userId, packageName);
            if (location == null) {
                Log.w(TAG, "No current location found for package: " + packageName + ", userId: " + userId);
                return;
            }
            Log.d(TAG, "Changing location - Distance: " + distance + "m, Angle: " + angle + "°, Current: " + location.getLatitude() + ", " + location.getLongitude());

            // Compute local deltas in meters
            double dx = distance * Math.sin(angle * Math.PI / 180.0);
            double dy = distance * Math.cos(angle * Math.PI / 180.0);

            // Adjust for Earth's curvature at current latitude
            double ec = Eb + (Ea - Eb) * (90.0 - location.getLatitude()) / 90.0;
            double ed = ec * Math.cos(location.getLatitude() * Math.PI / 180.0);

            double newLng = (dx / ed + location.getLongitude() * Math.PI / 180.0) * 180.0 / Math.PI;
            double newLat = (dy / ec + location.getLatitude() * Math.PI / 180.0) * 180.0 / Math.PI;

            BLocation newLocation = new BLocation(newLat, newLng);
            BLocationManager.get().setLocation(userId, packageName, newLocation);
            Log.d(TAG, "Location updated - New: " + newLat + ", " + newLng);
        } catch (Exception e) {
            Log.e(TAG, "Error changing location: " + e.getMessage(), e);
        }
    }

    public static boolean isActive() {
        return isInitialized;
    }

    public static boolean checkPermissions(Context context) {
        try {
            boolean hasOverlayPermission = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasOverlayPermission = Settings.canDrawOverlays(context);
            }
            if (!hasOverlayPermission) {
                Log.w(TAG, "Overlay permission not granted - RockerManager cannot show floating view");
                return false;
            }
            boolean hasLocationPermission =
                    false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasLocationPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
            }
            if (!hasLocationPermission) {
                Log.w(TAG, "Location permission not granted - RockerManager cannot access location");
                return false;
            }
            Log.d(TAG, "All required permissions are granted");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking permissions: " + e.getMessage());
            return false;
        }
    }

    public static java.util.List<String> getRequiredPermissions() {
        return java.util.Arrays.asList(
                android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        );
    }

    public static void cleanup() {
        try {
            isInitialized = false;
            Log.d(TAG, "RockerManager cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup: " + e.getMessage());
        }
    }
}
