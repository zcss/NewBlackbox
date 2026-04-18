package top.niunaijun.blackboxa.data;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.location.BLocation;
import top.niunaijun.blackbox.fake.frameworks.BLocationManager;
import top.niunaijun.blackboxa.bean.FakeLocationBean;

public class FakeLocationRepository {
    private static final String TAG = "FakeLocationRepository";

    public void setPattern(int userId, String pkg, int pattern) {
        BLocationManager.get().setPattern(userId, pkg, pattern);
    }

    private int getPattern(int userId, String pkg) {
        return BLocationManager.get().getPattern(userId, pkg);
    }

    private BLocation getLocation(int userId, String pkg) {
        return BLocationManager.get().getLocation(userId, pkg);
    }

    public void setLocation(int userId, String pkg, BLocation location) {
        BLocationManager.get().setLocation(userId, pkg, location);
    }

    public void getInstalledAppList(int userID, MutableLiveData<List<FakeLocationBean>> appsFakeLiveData) {
        List<FakeLocationBean> installedList = new ArrayList<>();
        List<ApplicationInfo> installedApplications = BlackBoxCore.get().getInstalledApplications(0, userID);
        for (ApplicationInfo installedApplication : installedApplications) {
            FakeLocationBean info = new FakeLocationBean(
                    userID,
                    installedApplication.loadLabel(BlackBoxCore.getPackageManager()).toString(),
                    installedApplication.loadIcon(BlackBoxCore.getPackageManager()),
                    installedApplication.packageName,
                    getPattern(userID, installedApplication.packageName),
                    getLocation(userID, installedApplication.packageName)
            );
            installedList.add(info);
        }
        Log.d(TAG, installedList.toString());
        appsFakeLiveData.postValue(installedList);
    }
}
