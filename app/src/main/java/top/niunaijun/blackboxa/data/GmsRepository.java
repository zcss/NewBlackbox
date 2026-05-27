package top.niunaijun.blackboxa.data;

import androidx.lifecycle.MutableLiveData;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.app.AppManager;
import top.niunaijun.blackboxa.bean.GmsBean;
import top.niunaijun.blackboxa.bean.GmsInstallBean;
import top.niunaijun.blackboxa.util.ResUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * GMS 管理仓库。
 * 负责：
 * - 查询各虚拟用户下 GMS 的安装状态
 * - 安装/卸载 GMS，并将结果封装为 GmsInstallBean 通过 LiveData 回传
 * 注意：BlackBoxCore.get().getUsers() 返回的是 Kotlin/Java 对象，使用反射读取 id。
 */
public class GmsRepository {

    /**
     * 获取所有用户的 GMS 安装状态列表（用户名从 Remark{userId} 读取，默认 "User {id}"）。
     */
    public void getGmsInstalledList(MutableLiveData<List<GmsBean>> mInstalledLiveData) {
        List<GmsBean> userList = new ArrayList<>();
        for (Object it : BlackBoxCore.get().getUsers()) {
            int userId;
            String userName;
            try {
                java.lang.reflect.Method getId = it.getClass().getMethod("getId");
                userId = (Integer) getId.invoke(it);
                String defaultName = "User " + userId;
                String pref = AppManager.getMRemarkSharedPreferences().getString("Remark" + userId, defaultName);
                userName = pref != null ? pref : defaultName;
            } catch (Exception e) {
                continue;
            }
            boolean isInstalled = BlackBoxCore.get().isInstallGms(userId);
            GmsBean bean = new GmsBean(userId, userName, isInstalled);
            userList.add(bean);
        }
        mInstalledLiveData.postValue(userList);
    }

    /**
     * 安装当前用户的 GMS。结果封装为 GmsInstallBean：包含 userID、是否成功、消息文本。
     */
    public void installGms(int userID, MutableLiveData<GmsInstallBean> mUpdateInstalledLiveData) {
        Object installResult = BlackBoxCore.get().installGms(userID);
        boolean success;
        String msg = null;
        try {
            java.lang.reflect.Method mSuccess = installResult.getClass().getMethod("getSuccess");
            success = (Boolean) mSuccess.invoke(installResult);
            try {
                java.lang.reflect.Method mMsg = installResult.getClass().getMethod("getMsg");
                msg = (String) mMsg.invoke(installResult);
            } catch (NoSuchMethodException ignored) {}
        } catch (Exception e) {
            success = false;
        }
        String result = success ? ResUtil.getString(R.string.install_success)
                : ResUtil.getString(R.string.install_fail, msg != null ? msg : "");
        GmsInstallBean bean = new GmsInstallBean(userID, success, result);
        mUpdateInstalledLiveData.postValue(bean);
    }

    /**
     * 卸载当前用户的 GMS（若已安装）。返回卸载是否成功与对应提示文案。
     */
    public void uninstallGms(int userID, MutableLiveData<GmsInstallBean> mUpdateInstalledLiveData) {
        boolean isSuccess = false;
        if (BlackBoxCore.get().isInstallGms(userID)) {
            isSuccess = BlackBoxCore.get().uninstallGms(userID);
        }
        String result = isSuccess ? ResUtil.getString(R.string.uninstall_success)
                : ResUtil.getString(R.string.uninstall_fail);
        GmsInstallBean bean = new GmsInstallBean(userID, isSuccess, result);
        mUpdateInstalledLiveData.postValue(bean);
    }
}
