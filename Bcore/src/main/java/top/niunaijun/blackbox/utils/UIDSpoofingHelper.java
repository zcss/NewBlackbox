package top.niunaijun.blackbox.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.utils.Slog;

/**
 * UID 欺骗辅助：根据操作场景选择合适的 UID（系统/目标包），并提供日志与校验辅助。
 */
public class UIDSpoofingHelper {
    private static final String TAG = "UIDSpoofingHelper";

    /** 返回系统 UID（失败兜底 1000）。 */
    public static int getSystemUID() {
        try {
            return Process.SYSTEM_UID;
        } catch (Exception e) {
            Slog.w(TAG, "Failed to get system UID, using fallback", e);
            return 1000; // fallback
        }
    }

    /** 获取指定包的 UID，失败时兜底为系统 UID。 */
    public static int getPackageUID(String packageName) {
        try {
            Context context = BlackBoxCore.getContext();
            if (context != null) {
                PackageManager pm = context.getPackageManager();
                if (pm != null) {
                    int uid = pm.getPackageUid(packageName, 0);
                    if (uid > 0) {
                        Slog.d(TAG, "Found real UID for package " + packageName + ": " + uid);
                        return uid;
                    }
                }
            }
            Slog.w(TAG, "Package UID not found for " + packageName + ", using system UID");
            return getSystemUID();
        } catch (Exception e) {
            Slog.w(TAG, "Failed to get package UID for " + packageName + ", using system UID", e);
            return getSystemUID();
        }
    }

    /** JobScheduler 等场景建议使用包 UID，失败兜底系统 UID。 */
    public static int getJobSchedulingUID(String targetPackage) {
        try {
            int packageUid = getPackageUID(targetPackage);
            if (packageUid > 0) {
                Slog.d(TAG, "Using package UID for job scheduling: " + packageUid);
                return packageUid;
            }
            Slog.w(TAG, "Using system UID for job scheduling as fallback");
            return getSystemUID();
        } catch (Exception e) {
            Slog.w(TAG, "Failed to get job scheduling UID, using system UID", e);
            return getSystemUID();
        }
    }

    /** 判断当前 UID 与目标包 UID 是否不一致，从而需要“伪装”。 */
    public static boolean needsUIDSpoofing(String operation, String targetPackage) {
        try {
            int currentUid = Process.myUid();
            int targetUid = getPackageUID(targetPackage);
            boolean needsSpoofing = currentUid != targetUid;
            Slog.d(TAG, "UID spoofing check: current=" + currentUid +
                       ", target=" + targetUid + ", needsSpoofing=" + needsSpoofing);
            return needsSpoofing;
        } catch (Exception e) {
            Slog.w(TAG, "Failed to check UID spoofing need, assuming yes", e);
            return true;
        }
    }

    /** 根据操作类型选择推荐 UID。 */
    public static int getBestUIDForOperation(String operation, String targetPackage) {
        try {
            switch (operation) {
                case "job_schedule":
                case "job_enqueue":
                    return getJobSchedulingUID(targetPackage);
                case "content_provider":
                case "settings_access":
                    return getSystemUID();
                case "package_operation":
                    return getPackageUID(targetPackage);
                default:
                    int packageUid = getPackageUID(targetPackage);
                    if (packageUid > 0) {
                        return packageUid;
                    }
                    return getSystemUID();
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to get best UID for operation " + operation + ", using system UID", e);
            return getSystemUID();
        }
    }

    /** 打印当前/推荐 UID 与是否需要伪装的信息。 */
    public static void logUIDInfo(String operation, String targetPackage) {
        try {
            int currentUid = Process.myUid();
            int bestUid = getBestUIDForOperation(operation, targetPackage);
            boolean needsSpoofing = needsUIDSpoofing(operation, targetPackage);
            Slog.d(TAG, String.format("UID Info - Operation: %s, Package: %s, Current: %d, Best: %d, NeedsSpoofing: %s",
                    operation, targetPackage, currentUid, bestUid, needsSpoofing));
        } catch (Exception e) {
            Slog.w(TAG, "Failed to log UID info", e);
        }
    }
}
