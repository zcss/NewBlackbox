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

public class GmsRepository {

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
