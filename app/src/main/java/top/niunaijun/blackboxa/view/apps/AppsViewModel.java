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

public class AppsViewModel extends BaseViewModel {
    private static final String TAG = "AppsViewModel";

    private final AppsRepository repo;

    private final MutableLiveData<List<AppInfo>> appsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> resultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> launchLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSortLiveData = new MutableLiveData<>();

    public AppsViewModel(@NonNull AppsRepository repo) {
        this.repo = repo;
    }

    public MutableLiveData<List<AppInfo>> getAppsLiveData() { return appsLiveData; }
    public MutableLiveData<String> getResultLiveData() { return resultLiveData; }
    public MutableLiveData<Boolean> getLaunchLiveData() { return launchLiveData; }
    public MutableLiveData<Boolean> getUpdateSortLiveData() { return updateSortLiveData; }

    public void getInstalledApps(final int userId) {
        launchOnUI(object -> {
            repo.getVmInstallList(userId, appsLiveData);
        });
    }

    public void getInstalledAppsWithRetry(final int userId) {
        getInstalledAppsWithRetry(userId, 3);
    }

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

    public void install(@NonNull final String source, final int userID) {
        launchOnUI(object -> {
            repo.installApk(source, userID, resultLiveData);
        });
    }

    public void unInstall(@NonNull final String packageName, final int userID) {
        launchOnUI(object -> {
            repo.unInstall(packageName, userID, resultLiveData);
        });
    }

    public void clearApkData(@NonNull final String packageName, final int userID) {
        launchOnUI(object -> {
            repo.clearApkData(packageName, userID, resultLiveData);
        });
    }

    public void launchApk(@NonNull final String packageName, final int userID) {
        launchOnUI(object -> {
            repo.launchApk(packageName, userID, launchLiveData);
        });
    }

    public void updateApkOrder(final int userID, @NonNull final List<AppInfo> dataList) {
        launchOnUI(object -> {
            repo.updateApkOrder(userID, dataList);
        });
    }
}
