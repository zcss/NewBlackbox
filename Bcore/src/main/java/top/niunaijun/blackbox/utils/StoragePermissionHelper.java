package top.niunaijun.blackbox.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 存储权限辅助：兼容不同 Android 版本的媒体/存储权限与“所有文件访问”授权流程。
 */
public class StoragePermissionHelper {
    private static final String TAG = "StoragePermissionHelper";

    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1001;
    public static final int REQUEST_CODE_MANAGE_STORAGE = 1002;

    /** Android 11+ 是否拥有“所有文件访问”能力；更低版本总为 true。 */
    public static boolean hasAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            // 更低版本无此概念
            return true;
        }
    }

    /** 是否具备读取（及必要时写入）外部存储的权限。 */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 分拆媒体权限，任一授予视为具备读取能力
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                   ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11：READ_EXTERNAL_STORAGE 或 All files access
            return Environment.isExternalStorageManager() ||
                   ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            // 更低版本：读写权限
            return ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /** 同时具备媒体权限与“所有文件访问”。 */
    public static boolean hasFullFileAccess(Context context) {
        return hasAllFilesAccess() && hasStoragePermission(context);
    }

    /** 引导至“所有文件访问”授权界面（Android 11+）。 */
    public static void requestAllFilesAccess(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
                } catch (Exception e) {
                    // 回退至通用页面
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
                }
            }
        }
    }

    /** 为指定包引导“所有文件访问”，用于代理场景。 */
    public static void requestAllFilesAccessForPackage(Activity activity, String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + packageName));
                activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
            }
        }
    }

    /** 申请存储/媒体读取权限。 */
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13：申请媒体读取三权限
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                    },
                    REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    /** 引导获取完整文件访问：优先权限，再引导 All files access。 */
    public static void requestFullFileAccess(Activity activity) {
        // 先申请媒体/读写权限
        if (!hasStoragePermission(activity)) {
            requestStoragePermission(activity);
        }
        // 再引导 All files access（Android 11+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            requestAllFilesAccess(activity);
        }
    }

    /** 是否应展示权限申请理由。 */
    public static boolean shouldShowStorageRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /** 默认的权限申请理由文案。 */
    public static String getPermissionRationale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return "This app needs access to all files to properly manage sandboxed applications. " +
                   "Please grant 'All files access' permission in the settings.";
        } else {
            return "This app needs storage permission to properly manage sandboxed applications. " +
                   "Please grant storage permission.";
        }
    }

    /** 处理权限申请回调。 */
    public static boolean handlePermissionResult(Activity activity, int requestCode,
            String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            // 任一权限通过则视为成功（高版本读取场景）
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    Slog.d(TAG, "Storage permission granted");
                    return true;
                }
            }
            Slog.w(TAG, "Storage permission denied");
            return false;
        }
        return false;
    }

    /** 处理 All files access 授权回调。 */
    public static boolean handleAllFilesAccessResult(int requestCode) {
        if (requestCode == REQUEST_CODE_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                boolean granted = Environment.isExternalStorageManager();
                if (granted) {
                    Slog.d(TAG, "All files access granted");
                } else {
                    Slog.w(TAG, "All files access denied");
                }
                return granted;
            }
        }
        return false;
    }
}
