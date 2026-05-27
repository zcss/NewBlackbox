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

/**
 * 伪定位数据仓库。
 * 负责：
 * - 管理每个用户/应用的伪定位策略（pattern）与具体坐标（BLocation）
 * - 查询虚拟用户空间已安装应用并拼装 FakeLocationBean 列表
 * - 通过 LiveData 回传数据以驱动 UI
 */
public class FakeLocationRepository {
    private static final String TAG = "FakeLocationRepository";

    /** 设置伪定位策略。 */
    public void setPattern(int userId, String pkg, int pattern) {
        BLocationManager.get().setPattern(userId, pkg, pattern);
    }

    /** 获取伪定位策略。 */
    private int getPattern(int userId, String pkg) {
        return BLocationManager.get().getPattern(userId, pkg);
    }

    /** 获取伪定位坐标。 */
    private BLocation getLocation(int userId, String pkg) {
        return BLocationManager.get().getLocation(userId, pkg);
    }

    /** 设置伪定位坐标。 */
    public void setLocation(int userId, String pkg, BLocation location) {
        BLocationManager.get().setLocation(userId, pkg, location);
    }

    /**
     * 查询 userID 下已安装应用，并为每个应用填充当前伪定位配置。
     * 结果通过 appsFakeLiveData.postValue 返回。
     */
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
