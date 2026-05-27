package top.niunaijun.blackboxa.data;

import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.AbiUtils;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.app.AppManager;
import top.niunaijun.blackboxa.bean.AppInfo;
import top.niunaijun.blackboxa.bean.InstalledAppBean;
import top.niunaijun.blackboxa.util.MemoryManager;
import top.niunaijun.blackboxa.util.ResUtil;

/**
 * 应用数据仓库。
 * 负责：
 * - 预扫描系统已安装应用，过滤系统应用/ABI 不支持/BlackBox 自身
 * - 查询虚拟用户空间应用列表并按偏好排序
 * - 安装、卸载、启动、清理应用，并通过 LiveData 回传结果
 * 线程安全：对 mInstalledList 的访问通过 synchronized 保护。
 */
public class AppsRepository {
    private static final String TAG = "AppsRepository";
    private final List<AppInfo> mInstalledList = new ArrayList<>();

    /** 安全获取应用名称，异常时回落为包名。 */
    private String safeLoadAppLabel(ApplicationInfo applicationInfo) {
        try {
            CharSequence label = BlackBoxCore.getPackageManager().getApplicationLabel(applicationInfo);
            return label != null ? label.toString() : applicationInfo.packageName;
        } catch (Exception e) {
            Log.w(TAG, "Failed to load label for " + applicationInfo.packageName + ": " + e.getMessage());
            return applicationInfo.packageName;
        }
    }

    /**
     * 内存友好的图标加载：必要时缩放到 96x96，内存紧张或异常时返回 null 以避免 OOM。
     */
    private Drawable safeLoadAppIcon(ApplicationInfo applicationInfo) {
        try {
            if (MemoryManager.shouldSkipIconLoading()) {
                Log.w(TAG, "Memory usage high (" + MemoryManager.getMemoryUsagePercentage() + "%), skipping icon for " + applicationInfo.packageName);
                return null;
            }
            Drawable icon = BlackBoxCore.getPackageManager().getApplicationIcon(applicationInfo);
            if (icon instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                if (bitmap != null && (bitmap.getWidth() > 96 || bitmap.getHeight() > 96)) {
                    try {
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, true);
                        return new BitmapDrawable(
                                BlackBoxCore.getPackageManager().getResourcesForApplication(applicationInfo.packageName),
                                scaledBitmap
                        );
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to scale icon for " + applicationInfo.packageName + ": " + e.getMessage());
                        return icon;
                    }
                }
            }
            return icon;
        } catch (Exception e) {
            Log.w(TAG, "Failed to load icon for " + applicationInfo.packageName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * 预扫描系统已安装应用并缓存到 mInstalledList。
     * 过滤：系统应用、ABI 不支持、BlackBox 自身应用。
     */
    public void previewInstallList() {
        try {
            synchronized (mInstalledList) {
                List<ApplicationInfo> installedApplications = BlackBoxCore.getPackageManager().getInstalledApplications(0);
                List<AppInfo> installedList = new ArrayList<>();
                for (ApplicationInfo installedApplication : installedApplications) {
                    try {
                        File file = new File(installedApplication.sourceDir);
                        if ((installedApplication.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                            continue;
                        }
                        if (!AbiUtils.isSupport(file)) continue;
                        if (BlackBoxCore.get().isBlackBoxApp(installedApplication.packageName)) {
                            Log.d(TAG, "Filtering out BlackBox app: " + installedApplication.packageName);
                            continue;
                        }
                        boolean isXpModule = false;
                        AppInfo info = new AppInfo(
                                safeLoadAppLabel(installedApplication),
                                safeLoadAppIcon(installedApplication),
                                installedApplication.packageName,
                                installedApplication.sourceDir,
                                isXpModule
                        );
                        installedList.add(info);
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing app " + installedApplication.packageName + ": " + e.getMessage());
                    }
                }
                mInstalledList.clear();
                mInstalledList.addAll(installedList);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in previewInstallList: " + e.getMessage());
        }
    }

    /**
     * 将预扫描结果映射为 InstalledAppBean 列表，并标记 userID 的安装状态，
     * 通过 LiveData 发送 loading 和结果。
     *
     * @param userID 目标用户 ID
     * @param loadingLiveData 加载状态 LiveData
     * @param appsLiveData 应用列表 LiveData
     */
    public void getInstalledAppList(int userID,
                                    MutableLiveData<Boolean> loadingLiveData,
                                    MutableLiveData<List<InstalledAppBean>> appsLiveData) {
        try {
            loadingLiveData.postValue(true);
            synchronized (mInstalledList) {
                BlackBoxCore blackBoxCore = BlackBoxCore.get();
                Log.d(TAG, mInstalledList.toString());
                List<InstalledAppBean> newInstalledList = new ArrayList<>();
                for (AppInfo it : mInstalledList) {
                    newInstalledList.add(new InstalledAppBean(
                            it.getName(),
                            it.getIcon(),
                            it.getPackageName(),
                            it.getSourceDir(),
                            blackBoxCore.isInstalled(it.getPackageName(), userID)
                    ));
                }
                appsLiveData.postValue(newInstalledList);
                loadingLiveData.postValue(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getInstalledAppList: " + e.getMessage());
            loadingLiveData.postValue(false);
            appsLiveData.postValue(new ArrayList<>());
        }
    }

    /**
     * 获取虚拟用户空间的安装列表并按偏好排序（SharedPreferences: AppList{userId}）。
     * - 内存紧张时尝试触发 GC
     * - 获取安装列表最多重试 3 次
     * 结果通过 appsLiveData.postValue 返回。
     *
     * @param userId 目标虚拟用户 ID
     * @param appsLiveData 应用列表 LiveData
     */
    public void getVmInstallList(int userId, MutableLiveData<List<AppInfo>> appsLiveData) {
        try {
            if (MemoryManager.isMemoryCritical()) {
                Log.w(TAG, "Memory critical (" + MemoryManager.getMemoryUsagePercentage() + "%), forcing garbage collection");
                MemoryManager.forceGarbageCollectionIfNeeded();
            }
            BlackBoxCore blackBoxCore = BlackBoxCore.get();
            List<?> users = blackBoxCore.getUsers();
            Log.d(TAG, "getVmInstallList: userId=" + userId + ", total users=" + users.size());
            String sortListData = AppManager.getMRemarkSharedPreferences().getString("AppList" + userId, "");
            List<String> sortList = null;
            if (sortListData != null && !sortListData.isEmpty()) {
                sortList = java.util.Arrays.asList(sortListData.split(","));
            }

            List<ApplicationInfo> applicationList = null;
            int retryCount = 0;
            int maxRetries = 3;
            while (applicationList == null && retryCount < maxRetries) {
                try {
                    applicationList = blackBoxCore.getInstalledApplications(0, userId);
                    if (applicationList == null) {
                        Log.w(TAG, "getVmInstallList: Attempt " + (retryCount + 1) + " returned null, retrying...");
                        retryCount++;
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getVmInstallList: Error getting applications on attempt " + (retryCount + 1) + ": " + e.getMessage());
                    retryCount++;
                    if (retryCount < maxRetries) {
                        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                    }
                }
            }

            if (applicationList == null) {
                Log.e(TAG, "getVmInstallList: applicationList is null for userId=" + userId + " after " + maxRetries + " attempts");
                appsLiveData.postValue(new ArrayList<>());
                return;
            }

            Log.d(TAG, "getVmInstallList: userId=" + userId + ", applicationList.size=" + applicationList.size());
            List<AppInfo> appInfoList = new ArrayList<>();

            List<ApplicationInfo> sortedApplicationList;
            if (sortList != null && !sortList.isEmpty()) {
                try {
                    sortedApplicationList = new ArrayList<>(applicationList);
                    sortedApplicationList.sort(new AppsSortComparator(sortList));
                } catch (Exception e) {
                    Log.e(TAG, "getVmInstallList: Error sorting applications: " + e.getMessage());
                    sortedApplicationList = applicationList;
                }
            } else {
                sortedApplicationList = applicationList;
            }

            for (int index = 0; index < sortedApplicationList.size(); index++) {
                ApplicationInfo applicationInfo = sortedApplicationList.get(index);
                try {
                    if (index > 0 && index % 25 == 0) {
                        if (MemoryManager.isMemoryCritical()) {
                            Log.w(TAG, "Memory critical during processing, forcing GC");
                            MemoryManager.forceGarbageCollectionIfNeeded();
                        }
                    }
                    if (applicationInfo == null) {
                        Log.w(TAG, "getVmInstallList: Skipping null applicationInfo at index " + index);
                        continue;
                    }
                    if (applicationInfo.packageName == null || applicationInfo.packageName.trim().isEmpty()) {
                        Log.w(TAG, "getVmInstallList: Skipping app with null/blank package name at index " + index);
                        continue;
                    }
                    AppInfo info = new AppInfo(
                            safeLoadAppLabel(applicationInfo),
                            safeLoadAppIcon(applicationInfo),
                            applicationInfo.packageName,
                            applicationInfo.sourceDir != null ? applicationInfo.sourceDir : "",
                            false
                    );
                    appInfoList.add(info);
                    if (index > 0 && index % 50 == 0) {
                        Log.d(TAG, "getVmInstallList: Processed " + index + "/" + sortedApplicationList.size() + " apps - " + MemoryManager.getMemoryInfo());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getVmInstallList: Error processing app at index " + index + " (" + (applicationInfo != null ? applicationInfo.packageName : "null") + "): " + e.getMessage());
                }
            }

            Log.d(TAG, "getVmInstallList: processed " + appInfoList.size() + " apps - " + MemoryManager.getMemoryInfo());
            try {
                appsLiveData.postValue(appInfoList);
            } catch (Exception e) {
                Log.e(TAG, "getVmInstallList: Error posting to LiveData: " + e.getMessage());
                try {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        try {
                            appsLiveData.postValue(appInfoList);
                        } catch (Exception e2) {
                            Log.e(TAG, "getVmInstallList: Fallback posting also failed: " + e2.getMessage());
                        }
                    });
                } catch (Exception e3) {
                    Log.e(TAG, "getVmInstallList: Could not schedule fallback posting: " + e3.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in getVmInstallList: " + e.getMessage());
            try {
                appsLiveData.postValue(new ArrayList<>());
            } catch (Exception e2) {
                Log.e(TAG, "getVmInstallList: Error posting empty list: " + e2.getMessage());
            }
        }
    }

    /**
     * 安装 APK（支持文件路径或 URL）。
     * 保护：阻止在 BlackBox 内安装 BlackBox 自身以避免递归/安全问题。
     * 成功会更新排序列表并返回 R.string.install_success，失败返回具体错误。
     *
     * @param source 本地路径或 URL
     * @param userId 目标用户 ID
     * @param resultLiveData 结果消息 LiveData
     */
    public void installApk(String source, int userId, MutableLiveData<String> resultLiveData) {
        try {
            if (source.contains("blackbox") || source.contains("niunaijun") || source.contains("vspace") || source.contains("virtual")) {
                try {
                    BlackBoxCore blackBoxCore = BlackBoxCore.get();
                    String hostPackageName = BlackBoxCore.getHostPkg();
                    if (!URLUtil.isValidUrl(source)) {
                        File file = new File(source);
                        if (file.exists()) {
                            android.content.pm.PackageInfo packageInfo = BlackBoxCore.getPackageManager().getPackageArchiveInfo(source, 0);
                            if (packageInfo != null && hostPackageName.equals(packageInfo.packageName)) {
                                resultLiveData.postValue("Cannot install BlackBox app from within BlackBox. This would create infinite recursion and is not allowed for security reasons.");
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not verify if this is BlackBox app: " + e.getMessage());
                }
            }

            BlackBoxCore blackBoxCore = BlackBoxCore.get();
            Object installResult;
            if (URLUtil.isValidUrl(source)) {
                Uri uri = Uri.parse(source);
                installResult = blackBoxCore.installPackageAsUser(uri, userId);
            } else {
                installResult = blackBoxCore.installPackageAsUser(source, userId);
            }
            Log.e(TAG,"installApk() 安装成功: "+installResult.toString());

            // installResult 是 core 中的 Kotlin 数据类，使用反射访问属性
            boolean success;
            String packageName = null;
            String msg = null;
            try {
                java.lang.reflect.Method mSuccess = installResult.getClass().getMethod("getSuccess");
                success = (Boolean) mSuccess.invoke(installResult);
                try {
                    java.lang.reflect.Method mPkg = installResult.getClass().getMethod("getPackageName");
                    packageName = (String) mPkg.invoke(installResult);
                } catch (NoSuchMethodException ignored) {}
                try {
                    java.lang.reflect.Method mMsg = installResult.getClass().getMethod("getMsg");
                    msg = (String) mMsg.invoke(installResult);
                } catch (NoSuchMethodException ignored) {}
            } catch (Exception reflectEx) {
                Log.w(TAG, "installApk: could not reflect install result, assuming failure: " + reflectEx.getMessage());
                success = false;
            }

            if (success) { // 安装成功
                Log.e(TAG,"installApk() 安装成功", new RuntimeException());
                if (packageName != null) updateAppSortList(userId, packageName, true);
                resultLiveData.postValue(ResUtil.getString(R.string.install_success));
            } else {
                // 安装失败
                Log.e(TAG,"installApk() 安装失败", new RuntimeException());
                resultLiveData.postValue(ResUtil.getString(R.string.install_fail, msg != null ? msg : ""));
            }
            scanUser();
        } catch (Exception e) {
            Log.e(TAG, "Error installing APK: " + e.getMessage());
            resultLiveData.postValue("Installation failed: " + e.getMessage());
        }
    }

    /**
     * 卸载应用（userID 作用域），并更新排序与用户清理。
     *
     * @param packageName 包名
     * @param userID 用户 ID
     * @param resultLiveData 结果消息 LiveData
     */
    public void unInstall(String packageName, int userID, MutableLiveData<String> resultLiveData) {
        try {
            BlackBoxCore.get().uninstallPackageAsUser(packageName, userID);
            updateAppSortList(userID, packageName, false);
            scanUser();
            resultLiveData.postValue(ResUtil.getString(R.string.uninstall_success));
        } catch (Exception e) {
            Log.e(TAG, "Error uninstalling APK: " + e.getMessage());
            resultLiveData.postValue("Uninstallation failed: " + e.getMessage());
        }
    }

    /**
     * 启动应用（userId 作用域）。
     * 结果通过 launchLiveData 返回。
     */
    public void launchApk(String packageName, int userId, MutableLiveData<Boolean> launchLiveData) {
        try {
            boolean result = BlackBoxCore.get().launchApk(packageName, userId);
            launchLiveData.postValue(result);
        } catch (Exception e) {
            Log.e(TAG, "Error launching APK: " + e.getMessage());
            launchLiveData.postValue(false);
        }
    }

    /**
     * 清理应用数据（userID 作用域）。
     *
     * @param packageName 包名
     * @param userID 用户 ID
     * @param resultLiveData 结果消息 LiveData
     */
    public void clearApkData(String packageName, int userID, MutableLiveData<String> resultLiveData) {
        try {
            BlackBoxCore.get().clearPackage(packageName, userID);
            resultLiveData.postValue(ResUtil.getString(R.string.clear_success));
        } catch (Exception e) {
            Log.e(TAG, "Error clearing APK data: " + e.getMessage());
            resultLiveData.postValue("Clear failed: " + e.getMessage());
        }
    }

    /**
     * 清理无应用的尾部用户：若最后一个用户已无安装应用，则删除并移除其备注/排序缓存，递归继续。
     */
    private void scanUser() {
        try {
            BlackBoxCore blackBoxCore = BlackBoxCore.get();
            List<?> userList = blackBoxCore.getUsers();
            if (userList.isEmpty()) {
                return;
            }
            // assume last() exists via index
            Object lastUser = userList.get(userList.size() - 1);
            int id;
            try {
                java.lang.reflect.Method getId = lastUser.getClass().getMethod("getId");
                id = (Integer) getId.invoke(lastUser);
            } catch (Exception e) {
                Log.w(TAG, "scanUser: could not get user id: " + e.getMessage());
                return;
            }
            if (blackBoxCore.getInstalledApplications(0, id).isEmpty()) {
                blackBoxCore.deleteUser(id);
                AppManager.getMRemarkSharedPreferences().edit().remove("Remark" + id).remove("AppList" + id).apply();
                scanUser();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in scanUser: " + e.getMessage());
        }
    }

    /**
     * 维护“AppList{userID}”顺序缓存。
     * @param isAdd true 表示新增/追加；false 表示移除
     */
    private void updateAppSortList(int userID, String pkg, boolean isAdd) {
        try {
            String savedSortList = AppManager.getMRemarkSharedPreferences().getString("AppList" + userID, "");
            LinkedHashSet<String> sortSet = new LinkedHashSet<>();
            if (savedSortList != null && !savedSortList.isEmpty()) {
                String[] parts = savedSortList.split(",");
                for (String p : parts) sortSet.add(p);
            }
            if (isAdd) sortSet.add(pkg); else sortSet.remove(pkg);
            AppManager.getMRemarkSharedPreferences().edit().putString("AppList" + userID, String.join(",", sortSet)).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error updating app sort list: " + e.getMessage());
        }
    }

    /**
     * 批量更新排序缓存“AppList{userID}”，按 dataList 顺序保存包名。
     */
    public void updateApkOrder(int userID, List<AppInfo> dataList) {
        try {
            List<String> pkgs = new ArrayList<>();
            for (AppInfo it : dataList) pkgs.add(it.getPackageName());
            AppManager.getMRemarkSharedPreferences().edit().putString("AppList" + userID, String.join(",", pkgs)).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error updating APK order: " + e.getMessage());
        }
    }
}
