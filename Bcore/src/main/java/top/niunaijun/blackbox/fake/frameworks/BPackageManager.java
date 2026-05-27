package top.niunaijun.blackbox.fake.frameworks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.pm.IBPackageManagerService;
import top.niunaijun.blackbox.entity.pm.InstallOption;
import top.niunaijun.blackbox.entity.pm.InstallResult;
import top.niunaijun.blackbox.entity.pm.InstalledPackage;
import top.niunaijun.blackbox.utils.TransactionThrottler;


/**
 * 包管理框架代理：
 * - 统一封装解析/查询/安装/卸载/枚举等接口到 IBPackageManagerService；
 * - 内置失败节流与回退策略（fallback intent/info），提升稳定性；
 * - 关键操作避免自安装递归（阻断安装宿主包）。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BPackageManager extends BlackManager<IBPackageManagerService> {
    /** 单例 */
    private static final BPackageManager sPackageManager = new BPackageManager();
    /** 事务失败节流器 */
    private final TransactionThrottler transactionThrottler = new TransactionThrottler();
    /** 查找 apk 路径的递归保护标志 */
    private static volatile boolean sIsFindingApkPath = false;

    /** 获取单例 */
    public static BPackageManager get() {
        return sPackageManager;
    }

    /** 重置节流状态（便于恢复） */
    public void resetTransactionThrottler() {
        transactionThrottler.reset();
        Log.d(TAG, "Transaction throttler reset");
    }

    /** 是否进入回退模式（连续失败或服务不健康） */
    private boolean shouldUseFallbackMode() {
        return transactionThrottler.getFailureCount() >= 2 || !isServiceHealthy();
    }

    /** 强制重新初始化 Service（含清缓存与节流器） */
    public void forceReinitialize() {
        Log.d(TAG, "Force reinitializing PackageManager service");
        clearServiceCache();
        resetTransactionThrottler();

        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                Log.d(TAG, "Successfully reinitialized PackageManager service");
            } else {
                Log.w(TAG, "Failed to reinitialize PackageManager service");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during service reinitialization", e);
        }
    }

    /** 获取 Service（必要时尝试重建） */
    public IBPackageManagerService getServiceWithFallback() {
        IBPackageManagerService service = getService();
        if (service == null) {
            Log.w(TAG, "PackageManager service is null, attempting reinitialization");
            forceReinitialize();
            service = getService();
        }
        return service;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.PACKAGE_MANAGER;
    }

    /**
     * 获取某包的启动 Intent，使用双路径（正常查询/回退查询）。
     */
    public Intent getLaunchIntentForPackage(String packageName, int userId) {
        // 回退模式：直接使用宿主 PM 的结果或构造通用 Launcher Intent
        if (shouldUseFallbackMode()) {
            Log.w(TAG, "Using fallback launch intent for " + packageName + " due to service failures");
            return createFallbackLaunchIntent(packageName);
        }

        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve,
                0,
                intentToResolve.resolveTypeIfNeeded(BlackBoxCore.getContext().getContentResolver()),
                userId);

        // 没有 info 再尝试 launcher
        if (ris == null || ris.size() <= 0) {
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve,
                    0,
                    intentToResolve.resolveTypeIfNeeded(BlackBoxCore.getContext().getContentResolver()),
                    userId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName,
                ris.get(0).activityInfo.name);
        return intent;
    }

    /**
     * 回退路径：尽可能使用宿主返回的启动 Intent 或构造一个通用 Intent。
     */
    private Intent createFallbackLaunchIntent(String packageName) {
        try {
            // 先走宿主 PM
            Intent intent = BlackBoxCore.getContext().getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return intent;
            }
        } catch (Exception e) {
            Log.w(TAG, "Fallback launch intent failed for " + packageName, e);
        }

        // 再构造兜底 LAUNCHER Intent
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /** 解析 Service */
    public ResolveInfo resolveService(Intent intent, int flags, String resolvedType, int userId) {
        // 避免雪崩：在失败后短期内直接返回
        if (transactionThrottler.shouldThrottle()) {
            Log.w(TAG, "Throttling resolveService due to recent failures");
            return null;
        }

        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                ResolveInfo result = service.resolveService(intent, flags, resolvedType, userId);
                transactionThrottler.reset();
                return result;
            } else {
                Log.w(TAG, "PackageManager service is null, returning null for resolveService");
            }
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "PackageManager service died during resolveService, clearing service and retrying", e);
            transactionThrottler.recordFailure();
            clearServiceCache();
            try {
                IBPackageManagerService service = getService();
                if (service != null) {
                    ResolveInfo result = service.resolveService(intent, flags, resolvedType, userId);
                    transactionThrottler.reset(); // 重试成功即复位
                    return result;
                }
            } catch (Exception retryException) {
                Log.e(TAG, "Retry failed for resolveService", retryException);
                transactionThrottler.recordFailure();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in resolveService", e);
            transactionThrottler.recordFailure();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in resolveService", e);
            transactionThrottler.recordFailure();
        }
        return null;
    }

    /** 解析 Activity */
    public ResolveInfo resolveActivity(Intent intent, int flags, String resolvedType, int userId) {
        if (transactionThrottler.shouldThrottle()) {
            Log.w(TAG, "Throttling resolveActivity due to recent failures");
            return null;
        }

        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                ResolveInfo result = service.resolveActivity(intent, flags, resolvedType, userId);
                transactionThrottler.reset();
                return result;
            } else {
                Log.w(TAG, "PackageManager service is null, returning null for resolveActivity");
            }
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "PackageManager service died during resolveActivity, clearing service and retrying", e);
            transactionThrottler.recordFailure();
            clearServiceCache();
            try {
                IBPackageManagerService service = getService();
                if (service != null) {
                    ResolveInfo result = service.resolveActivity(intent, flags, resolvedType, userId);
                    transactionThrottler.reset(); // 重试成功即复位
                    return result;
                }
            } catch (Exception retryException) {
                Log.e(TAG, "Retry failed for resolveActivity", retryException);
                transactionThrottler.recordFailure();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in resolveActivity", e);
            transactionThrottler.recordFailure();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in resolveActivity", e);
            transactionThrottler.recordFailure();
        }
        return null;
    }

    /** 解析 ContentProvider */
    public ProviderInfo resolveContentProvider(String authority, int flags, int userId) {
        if (transactionThrottler.shouldThrottle()) {
            Log.w(TAG, "Throttling resolveContentProvider due to recent failures");
            return null;
        }

        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                ProviderInfo result = service.resolveContentProvider(authority, flags, userId);
                transactionThrottler.reset();
                return result;
            } else {
                Log.w(TAG, "PackageManager service is null, returning null for resolveContentProvider");
            }
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "PackageManager service died during resolveContentProvider, clearing service and retrying", e);
            transactionThrottler.recordFailure();
            clearServiceCache();
            try {
                IBPackageManagerService service = getService();
                if (service != null) {
                    ProviderInfo result = service.resolveContentProvider(authority, flags, userId);
                    transactionThrottler.reset(); // 重试成功即复位
                    return result;
                }
            } catch (Exception retryException) {
                Log.e(TAG, "Retry failed for resolveContentProvider", retryException);
                transactionThrottler.recordFailure();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in resolveContentProvider", e);
            transactionThrottler.recordFailure();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in resolveContentProvider", e);
            transactionThrottler.recordFailure();
        }
        return null;
    }

    /** 通用 Intent 解析 */
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        try {
            return getService().resolveIntent(intent, resolvedType, flags, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /** 应用信息（带回退） */
    public ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) {
        try {
            IBPackageManagerService service = getServiceWithFallback();
            if (service == null) {
                Log.w(TAG, "PackageManager service is null for getApplicationInfo, using fallback");
                return createFallbackApplicationInfo(packageName, flags, userId);
            }
            return service.getApplicationInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getApplicationInfo for " + packageName, e);
            return createFallbackApplicationInfo(packageName, flags, userId);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getApplicationInfo for " + packageName, e);
            return createFallbackApplicationInfo(packageName, flags, userId);
        }
    }

    /** 包信息（带回退） */
    public PackageInfo getPackageInfo(String packageName, int flags, int userId) {
        try {
            IBPackageManagerService service = getServiceWithFallback();
            if (service == null) {
                Log.w(TAG, "PackageManager service is null for getPackageInfo, using fallback");
                return createFallbackPackageInfo(packageName, flags, userId);
            }
            return service.getPackageInfo(packageName, flags, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getPackageInfo for " + packageName, e);
            return createFallbackPackageInfo(packageName, flags, userId);
        } catch (Exception e) {
            Log.e(TAG, "Exception in getPackageInfo for " + packageName, e);
            return createFallbackPackageInfo(packageName, flags, userId);
        }
    }

    /** ServiceInfo 查询 */
    public ServiceInfo getServiceInfo(ComponentName component, int flags, int userId) {
        try {
            IBPackageManagerService service = getService();
            if (service == null) {
                Log.w(TAG, "PackageManager service is null for getServiceInfo, returning null");
                return null;
            }
            return service.getServiceInfo(component, flags, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getServiceInfo for " + component, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getServiceInfo for " + component, e);
            return null;
        }
    }

    /** ReceiverInfo 查询 */
    public ActivityInfo getReceiverInfo(ComponentName componentName, int flags, int userId) {
        try {
            IBPackageManagerService service = getService();
            if (service == null) {
                Log.w(TAG, "PackageManager service is null for getReceiverInfo, returning null");
                return null;
            }
            return service.getReceiverInfo(componentName, flags, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getReceiverInfo for " + componentName, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getReceiverInfo for " + componentName, e);
            return null;
        }
    }

    /** ActivityInfo 查询 */
    public ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        try {
            IBPackageManagerService service = getService();
            if (service == null) {
                Log.w(TAG, "PackageManager service is null for getActivityInfo, returning null");
                return null;
            }
            return service.getActivityInfo(component, flags, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getActivityInfo for " + component, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getActivityInfo for " + component, e);
            return null;
        }
    }

    /** ProviderInfo 查询 */
    public ProviderInfo getProviderInfo(ComponentName component, int flags, int userId) {
        try {
            IBPackageManagerService service = getService();
            if (service == null) {
                Log.w(TAG, "PackageManager service is null for getProviderInfo, returning null");
                return null;
            }
            return service.getProviderInfo(component, flags, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in getProviderInfo for " + component, e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Exception in getProviderInfo for " + component, e);
            return null;
        }
    }

    /** 查询可响应 Activity */
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags, String resolvedType, int userId) {
        // 第一层节流：失败后直接返回空列表
        if (transactionThrottler.shouldThrottle()) {
            Log.w(TAG, "Throttling queryIntentActivities due to recent failures");
            return Collections.emptyList();
        }
        // 第二层：失败过多也直接返回
        if (transactionThrottler.getFailureCount() >= 2) {
            Log.w(TAG, "Too many failures, returning empty list for queryIntentActivities");
            return Collections.emptyList();
        }

        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                List<ResolveInfo> result = service.queryIntentActivities(intent, flags, resolvedType, userId);
                transactionThrottler.reset();
                return result;
            } else {
                Log.w(TAG, "PackageManager service is null, returning empty list for queryIntentActivities");
                return Collections.emptyList();
            }
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "PackageManager service died during queryIntentActivities, clearing cache and retrying", e);
            transactionThrottler.recordFailure();
            clearServiceCache();

            if (transactionThrottler.getFailureCount() < 3) {
                try {
                    IBPackageManagerService service = getService();
                    if (service != null) {
                        List<ResolveInfo> result = service.queryIntentActivities(intent, flags, resolvedType, userId);
                        transactionThrottler.reset(); // 重试成功复位
                        return result;
                    }
                } catch (Exception retryException) {
                    Log.e(TAG, "Retry failed for queryIntentActivities", retryException);
                    transactionThrottler.recordFailure();
                }
            } else {
                Log.w(TAG, "Skipping retry due to too many failures");
            }
            return Collections.emptyList();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in queryIntentActivities", e);
            transactionThrottler.recordFailure();
            crash(e);
        }
        return Collections.emptyList();
    }

    /** 查询可响应广播 */
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, String resolvedType, int userId) {
        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                return service.queryBroadcastReceivers(intent, flags, resolvedType, userId);
            } else {
                Log.w(TAG, "PackageManager service is null, returning empty list for queryBroadcastReceivers");
                return Collections.emptyList();
            }
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "PackageManager service died during queryBroadcastReceivers, clearing cache and retrying", e);
            clearServiceCache(); 
            try {
                
                IBPackageManagerService service = getService();
                if (service != null) {
                    return service.queryBroadcastReceivers(intent, flags, resolvedType, userId);
                }
            } catch (Exception retryException) {
                Log.e(TAG, "Retry failed for queryBroadcastReceivers", retryException);
            }
            return Collections.emptyList();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in queryBroadcastReceivers", e);
            crash(e);
        }
        return Collections.emptyList();
    }

    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags, int userId) {
        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                return service.queryContentProviders(processName, uid, flags, userId);
            } else {
                Log.w(TAG, "PackageManager service is null, returning empty list for queryContentProviders");
                return Collections.emptyList();
            }
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "PackageManager service died during queryContentProviders, clearing cache and retrying", e);
            clearServiceCache();
            try {
                IBPackageManagerService service = getService();
                if (service != null) {
                    return service.queryContentProviders(processName, uid, flags, userId);
                }
            } catch (Exception retryException) {
                Log.e(TAG, "Retry failed for queryContentProviders", retryException);
            }
            return Collections.emptyList();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in queryContentProviders", e);
            crash(e);
        }
        return Collections.emptyList();
    }

    /** 安装包（阻断自安装 BlackBox 宿主） */
    public InstallResult installPackageAsUser(String file, InstallOption option, int userId) {
        try {
            // 安全校验：避免在虚拟环境中克隆自身
            if (file != null && !file.isEmpty()) {
                try {
                    PackageInfo packageInfo = BlackBoxCore.getPackageManager().getPackageArchiveInfo(file, 0);
                    if (packageInfo != null) {
                        String packageName = packageInfo.packageName;
                        String hostPackageName = BlackBoxCore.getHostPkg();
                        if (packageName.equals(hostPackageName)) {
                            Log.w(TAG, "Attempt to install BlackBox app detected and blocked: " + packageName);
                            return new InstallResult().installError("Cannot clone BlackBox app from within BlackBox. This would create infinite recursion and is not allowed for security reasons.");
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not verify package info for: " + file, e);
                }
            }

            return getService().installPackageAsUser(file, option, userId);
        } catch (RemoteException e) {
            crash(e);
        }
        return null;
    }

    /** 已安装应用列表 */
    public List<ApplicationInfo> getInstalledApplications(int flags, int userId) {
        try {
            return getService().getInstalledApplications(flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /** 已安装包列表 */
    public List<PackageInfo> getInstalledPackages(int flags, int userId) {
        try {
            return getService().getInstalledPackages(flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /** 清除包数据 */
    public void clearPackage(String packageName, int userId) {
        try {
            getService().clearPackage(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 停止包 */
    public void stopPackage(String packageName, int userId) {
        try {
            getService().stopPackage(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 卸载（指定用户） */
    public void uninstallPackageAsUser(String packageName, int userId) {
        try {
            getService().uninstallPackageAsUser(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 卸载（所有用户） */
    public void uninstallPackage(String packageName) {
        try {
            getService().uninstallPackage(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 是否已安装（带回退） */
    public boolean isInstalled(String packageName, int userId) {
        if (shouldUseFallbackMode()) {
            Log.w(TAG, "Using fallback isInstalled check for " + packageName + " due to service failures");
            return isInstalledFallback(packageName);
        }

        try {
            IBPackageManagerService service = getService();
            if (service != null) {
                boolean result = service.isInstalled(packageName, userId);
                transactionThrottler.reset(); // 成功复位
                return result;
            } else {
                Log.w(TAG, "PackageManager service is null, returning false for isInstalled check");
            }
        } catch (android.os.DeadObjectException e) {
            Log.w(TAG, "PackageManager service died during isInstalled check, clearing service and retrying", e);
            transactionThrottler.recordFailure();
            clearServiceCache();
            try {
                IBPackageManagerService service = getService();
                if (service != null) {
                    boolean result = service.isInstalled(packageName, userId);
                    transactionThrottler.reset(); // 重试成功复位
                    return result;
                }
            } catch (Exception retryException) {
                Log.e(TAG, "Retry failed for isInstalled check", retryException);
                transactionThrottler.recordFailure();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in isInstalled check", e);
            transactionThrottler.recordFailure();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in isInstalled check", e);
            transactionThrottler.recordFailure();
        }
        return false;
    }

    /** 回退安装判断：使用宿主 PM 或特殊包名白名单 */
    private boolean isInstalledFallback(String packageName) {
        try {
            // 查询宿主是否可见该包
            BlackBoxCore.getContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Fallback isInstalled check failed for " + packageName + ", assuming not installed");
            // 兼容某些已知包名（历史原因）
            if (packageName != null && (packageName.equals("com.media.bestrecorder.audiorecorder") ||
                                       packageName.startsWith("top.niunaijun.blackbox"))) {
                Log.w(TAG, "Returning true for known app " + packageName + " despite fallback failure");
                return true;
            }
            return false;
        }
    }

    /** 已安装包（结构化） */
    public List<InstalledPackage> getInstalledPackagesAsUser(int userId) {
        try {
            return getService().getInstalledPackagesAsUser(userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /** 通过 uid 查询包名 */
    public String[] getPackagesForUid(int uid) {
        try {
            return getService().getPackagesForUid(uid, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }

    /** 打印异常（保留原语义） */
    private void crash(Throwable e) {
        e.printStackTrace();
    }

    /** 创建回退 ApplicationInfo */
    private ApplicationInfo createFallbackApplicationInfo(String packageName, int flags, int userId) {
        Log.w(TAG, "Creating fallback ApplicationInfo for " + packageName);
        ApplicationInfo info = new ApplicationInfo();
        info.packageName = packageName;
        info.flags = flags;
        info.uid = 0; // 未知 uid

        // 尝试定位实际 apk 路径，避免随意访问导致 I/O 异常
        String apkPath = findActualApkPath(packageName);
        if (apkPath != null) {
            info.sourceDir = apkPath;
            info.publicSourceDir = apkPath;
        } else {
            Log.w(TAG, "No APK found for " + packageName + ", using null paths to prevent I/O errors");
            info.sourceDir = null; // 显式为 null，后续访问需判空
            info.publicSourceDir = null;
        }

        info.dataDir = "/data/data/" + packageName;
        info.nativeLibraryDir = "/data/app-lib/" + packageName;
        info.metaData = new Bundle();
        info.splitNames = new String[]{};

        // 常见标志位（用于兼容）
        info.flags |= ApplicationInfo.FLAG_ALLOW_BACKUP;
        info.flags |= ApplicationInfo.FLAG_SUPPORTS_RTL;

        return info;
    }

    /**
     * 尝试查找真实的 apk 路径，避免通过 PM 再次查询导致递归。
     */
    private String findActualApkPath(String packageName) {
        if (sIsFindingApkPath) {
            Log.w(TAG, "findActualApkPath called recursively, returning null to prevent infinite loop.");
            return null;
        }
        sIsFindingApkPath = true;
        try {
            // 不调用 PM，直接尝试已知路径
            Log.d(TAG, "Skipping PackageManager call to prevent recursion for " + packageName);

            String[] commonPaths = {
                    // Android 11+ 可能存在的路径模式（hash 目录）
                    "/data/app/~~*/" + packageName + "-*/base.apk",
                    "/data/app/~~*/" + packageName + "*/base.apk",
                    // 旧路径
                    "/data/app/" + packageName + "-1/base.apk",
                    "/data/app/" + packageName + "-2/base.apk",
                    "/data/app/" + packageName + "/base.apk",
                    // 只读系统位
                    "/system/app/" + packageName + ".apk",
                    "/system/priv-app/" + packageName + ".apk",
                    "/system_ext/app/" + packageName + ".apk",
                    "/product/app/" + packageName + ".apk",
                    "/vendor/app/" + packageName + ".apk"
            };

            for (String path : commonPaths) {
                if (isValidApkPath(path)) {
                    Log.d(TAG, "Found existing APK at: " + path);
                    return path;
                }
            }

            String hashBasedPath = findHashBasedApkPath(packageName);
            if (hashBasedPath != null) {
                Log.d(TAG, "Found hash-based APK at: " + hashBasedPath);
                return hashBasedPath;
            }

            Log.w(TAG, "No existing APK found for " + packageName + ", using null path");
            return null;
        } finally {
            sIsFindingApkPath = false; // 清理标志
        }
    }

    /** 在 hash 目录中查找 base.apk */
    private String findHashBasedApkPath(String packageName) {
        try {
            File dataAppDir = new File("/data/app");
            if (!dataAppDir.exists() || !dataAppDir.isDirectory()) {
                return null;
            }

            // Android 11 的 hash 目录形如 "~~<base64>=="
            File[] hashDirs = dataAppDir.listFiles((dir, name) -> name.startsWith("~~") && name.endsWith("=="));
            if (hashDirs == null) {
                return null;
            }

            for (File hashDir : hashDirs) {
                if (!hashDir.isDirectory()) {
                    continue;
                }
                File[] packageDirs = hashDir.listFiles((dir, name) -> name.startsWith(packageName));
                if (packageDirs == null) {
                    continue;
                }
                for (File packageDir : packageDirs) {
                    if (!packageDir.isDirectory()) {
                        continue;
                    }
                    File baseApk = new File(packageDir, "base.apk");
                    if (isValidApkPath(baseApk.getAbsolutePath())) {
                        return baseApk.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Error searching for hash-based APK path for " + packageName + ": " + e.getMessage());
        }

        return null;
    }

    /** 判断路径是否可用 */
    private boolean isValidApkPath(String path) {
        try {
            // 忽略包含通配符的路径
            if (path.contains("*")) {
                return false;
            }
            File apkFile = new File(path);
            if (!apkFile.exists()) {
                return false;
            }
            if (!apkFile.canRead()) {
                Log.d(TAG, "APK file not readable: " + path);
                return false;
            }
            long fileSize = apkFile.length();
            if (fileSize < 1024) { // 简单大小判断
                Log.d(TAG, "APK file too small: " + path + " (size: " + fileSize + ")");
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.d(TAG, "Error checking APK path " + path + ": " + e.getMessage());
            return false;
        }
    }

    /** 创建回退 PackageInfo */
    private PackageInfo createFallbackPackageInfo(String packageName, int flags, int userId) {
        Log.w(TAG, "Creating fallback PackageInfo for " + packageName);
        PackageInfo info = new PackageInfo();
        info.packageName = packageName;
        info.versionCode = 1; // 兜底版本
        info.versionName = "1.0"; // 兜底版本名
        info.applicationInfo = createFallbackApplicationInfo(packageName, flags, userId);
        info.firstInstallTime = System.currentTimeMillis(); // 兜底时间
        info.lastUpdateTime = System.currentTimeMillis(); // 兜底时间
        info.installLocation = 0; // 自动
        info.gids = new int[]{}; // 空 gid
        info.splitNames = new String[]{}; // 无拆分
        info.signatures = new Signature[]{}; // 无签名
        return info;
    }
}
