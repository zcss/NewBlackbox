package top.niunaijun.blackbox.fake.frameworks;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.buser.BUserInfo;
import top.niunaijun.blackbox.core.system.buser.IBUserManagerService;
import top.niunaijun.blackbox.utils.Slog;


/**
 * 用户框架代理：
 * - 提供用户创建/删除/枚举等接口到 IBUserManagerService，含 DeadObject 重试；
 * - 服务不可用或异常时返回空/空集合，打印警告日志。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BUserManager extends BlackManager<IBUserManagerService> {
    private static final String TAG = "BUserManager";
    private static final BUserManager sUserManager = new BUserManager();

    public static BUserManager get() {
        return sUserManager;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.USER_MANAGER;
    }

    public BUserInfo createUser(int userId) {
        try {
            IBUserManagerService service = getService();
            if (service != null) {
                return service.createUser(userId);
            } else {
                Slog.w(TAG, "UserManager service is null, cannot create user");
            }
        } catch (DeadObjectException e) {
            Slog.w(TAG, "UserManager service died during createUser, clearing cache and retrying", e);
            clearServiceCache();
            try {
                Thread.sleep(100);
                IBUserManagerService service = getService();
                if (service != null) {
                    return service.createUser(userId);
                }
            } catch (Exception retryException) {
                Slog.e(TAG, "Failed to create user after retry", retryException);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException in createUser", e);
        } catch (Exception e) {
            Slog.e(TAG, "Unexpected error in createUser", e);
        }
        return null;
    }

    public void deleteUser(int userId) {
        try {
            IBUserManagerService service = getService();
            if (service != null) {
                service.deleteUser(userId);
            } else {
                Slog.w(TAG, "UserManager service is null, cannot delete user");
            }
        } catch (DeadObjectException e) {
            Slog.w(TAG, "UserManager service died during deleteUser, clearing cache and retrying", e);
            clearServiceCache();
            try {
                Thread.sleep(100);
                IBUserManagerService service = getService();
                if (service != null) {
                    service.deleteUser(userId);
                }
            } catch (Exception retryException) {
                Slog.e(TAG, "Failed to delete user after retry", retryException);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException in deleteUser", e);
        } catch (Exception e) {
            Slog.e(TAG, "Unexpected error in deleteUser", e);
        }
    }

    public List<BUserInfo> getUsers() {
        try {
            IBUserManagerService service = getService();
            if (service != null) {
                return service.getUsers();
            } else {
                Slog.w(TAG, "UserManager service is null, returning empty list");
            }
        } catch (DeadObjectException e) {
            Slog.w(TAG, "UserManager service died during getUsers, clearing cache and retrying", e);
            clearServiceCache();
            try {
                Thread.sleep(100);
                IBUserManagerService service = getService();
                if (service != null) {
                    return service.getUsers();
                }
            } catch (Exception retryException) {
                Slog.e(TAG, "Failed to get users after retry", retryException);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException in getUsers", e);
        } catch (Exception e) {
            Slog.e(TAG, "Unexpected error in getUsers", e);
        }
        return Collections.emptyList();
    }
}
