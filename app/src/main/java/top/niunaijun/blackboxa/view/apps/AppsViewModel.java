package top.niunaijun.blackboxa.view.apps;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import top.niunaijun.blackboxa.bean.AppInfo;
import top.niunaijun.blackboxa.data.AppsRepository;
import top.niunaijun.blackboxa.view.base.BaseViewModel;

/**
 * 应用列表 ViewModel：负责应用数据获取、排序更新与安装/卸载/清数据/启动。
 */
public class AppsViewModel extends BaseViewModel {
    private static final String TAG = "AppsViewModel";

    /** 数据仓库。*/
    private final AppsRepository repo;

    /** 应用列表数据。*/
    private final MutableLiveData<List<AppInfo>> appsLiveData = new MutableLiveData<>();
    /** 结果消息（安装/卸载/清理结果）。*/
    private final MutableLiveData<String> resultLiveData = new MutableLiveData<>();
    /** 启动应用结果。*/
    private final MutableLiveData<Boolean> launchLiveData = new MutableLiveData<>();
    /** 排序更新触发通知。*/
    private final MutableLiveData<Boolean> updateSortLiveData = new MutableLiveData<>();

    /** 注入数据仓库。*/
    public AppsViewModel(@NonNull AppsRepository repo) {
        this.repo = repo;
    }

    public MutableLiveData<List<AppInfo>> getAppsLiveData() { return appsLiveData; }
    public MutableLiveData<String> getResultLiveData() { return resultLiveData; }
    public MutableLiveData<Boolean> getLaunchLiveData() { return launchLiveData; }
    public MutableLiveData<Boolean> getUpdateSortLiveData() { return updateSortLiveData; }

    /** 获取已安装应用列表。*/
    public void getInstalledApps(final int userId) {
        launchOnUI(object -> {
            repo.getVmInstallList(userId, appsLiveData);
        });
    }

    /** 获取已安装应用列表（含重试）。*/
    public void getInstalledAppsWithRetry(final int userId) {
        getInstalledAppsWithRetry(userId, 3);
    }

    /** 获取已安装应用列表（指定最大重试次数）。*/
    public void getInstalledAppsWithRetry(final int userId, final int maxRetries) {
        final AtomicInteger retryCount = new AtomicInteger(0);
        final Handler handler = new Handler(Looper.getMainLooper());

        final Runnable attemptLoad = () -> launchOnUI(object -> {
            repo.getVmInstallList(userId, appsLiveData);
            List<AppInfo> current = appsLiveData.getValue();
            if ((current == null || current.isEmpty()) && retryCount.get() < maxRetries) {
                int next = retryCount.incrementAndGet();
                Log.d(TAG, "No apps loaded, retrying... (" + next + "/" + maxRetries + ")");
                handler.postDelayed(triggerAttempt, 1000);
            }
        });

        // Helper indirection to re-post the same runnable
        this.triggerAttempt = attemptLoad;
        attemptLoad.run();
    }

    // Field to hold retry runnable reference
    private Runnable triggerAttempt;

    /** 安装 APK。*/
    public void install(@NonNull final String source, final int userID) {
        launchOnUI(object -> {
            repo.installApk(source, userID, resultLiveData);
        });
    }

    /** 卸载应用。*/
    public void unInstall(@NonNull final String packageName, final int userID) {
        launchOnUI(object -> {
            repo.unInstall(packageName, userID, resultLiveData);
        });
    }

    /** 清除应用数据。*/
    public void clearApkData(@NonNull final String packageName, final int userID) {
        launchOnUI(object -> {
            repo.clearApkData(packageName, userID, resultLiveData);
        });
    }

    /** 启动应用。*/
    public void launchApk(@NonNull final String packageName, final int userID) {
        launchOnUI(object -> {
            repo.launchApk(packageName, userID, launchLiveData);
        });
    }

    /** 更新应用排序。*/
    public void updateApkOrder(final int userID, @NonNull final List<AppInfo> dataList) {
        launchOnUI(object -> {
            repo.updateApkOrder(userID, dataList);
        });
    }
}
