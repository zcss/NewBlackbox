package top.niunaijun.blackbox.fake.frameworks;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;
import android.os.RemoteException;

import java.util.Map;

import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.accounts.IBAccountManagerService;


/**
 * 账户框架代理：
 * - 封装 IBAccountManagerService 的 Binder 调用，提供按用户隔离的账户读写/鉴权/可见性管理；
 * - 远程异常时返回安全默认值或空结果，避免调用方崩溃。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BAccountManager extends BlackManager<IBAccountManagerService> {
    /** 单例 */
    private static final BAccountManager sBAccountManager = new BAccountManager();

    /** 获取单例 */
    public static BAccountManager get() {
        return sBAccountManager;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.ACCOUNT_MANAGER;
    }

    /** 获取账户密码 */
    public String getPassword(Account account) {
        try {
            return getService().getPassword(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 获取账户自定义数据 */
    public String getUserData(Account account, String key) {
        try {
            return getService().getUserData(account, key, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 获取支持的认证器类型 */
    public AuthenticatorDescription[] getAuthenticatorTypes() {
        try {
            return getService().getAuthenticatorTypes(BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询某包下的账户 */
    public Account[] getAccountsForPackage(String packageName, int uid) {
        try {
            return getService().getAccountsForPackage(packageName, uid, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 按类型查询某包下账户 */
    public Account[] getAccountsByTypeForPackage(String type, String packageName) {
        try {
            return getService().getAccountsByTypeForPackage(type, packageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 获取当前用户的账户（可传类型过滤） */
    public Account[] getAccountsAsUser(String type) {
        try {
            return getService().getAccountsAsUser(type, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 通过类型和特性获取账户（异步回调） */
    public void getAccountByTypeAndFeatures(IAccountManagerResponse response, String accountType,
                                            String[] features) {
        try {
            getService().getAccountByTypeAndFeatures(response, accountType, features, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 按特性查询账户（异步回调） */
    public void getAccountsByFeatures(IAccountManagerResponse response, String accountType,
                               String[] features) {
        try {
            getService().getAccountsByFeatures(response, accountType, features, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 明文创建账户 */
    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        try {
            return getService().addAccountExplicitly(account, password, extras, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 异步移除账户（可触发 UI） */
    public void removeAccountAsUser(IAccountManagerResponse response, Account account,
                             boolean expectActivityLaunch) {
        try {
            getService().removeAccountAsUser(response, account, expectActivityLaunch, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 显式移除账户 */
    public boolean removeAccountExplicitly(Account account) {
        try {
            return getService().removeAccountExplicitly(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 复制账户到其他用户 */
    public void copyAccountToUser(IAccountManagerResponse response, Account account,
                           int userFrom, int userTo) {
        try {
            getService().copyAccountToUser(response, account, userFrom, userTo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 失效指定 token */
    public void invalidateAuthToken(String accountType, String authToken) {
        try {
            getService().invalidateAuthToken(accountType, authToken, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 读取缓存 token */
    public String peekAuthToken(Account account, String authTokenType) {
        try {
            return getService().peekAuthToken(account, authTokenType, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 设置 token */
    public void setAuthToken(Account account, String authTokenType, String authToken) {
        try {
            getService().setAuthToken(account, authTokenType, authToken, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设置密码 */
    public void setPassword(Account account, String password) {
        try {
            getService().setPassword(account, password, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 清除密码 */
    public void clearPassword(Account account) {
        try {
            getService().clearPassword(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设置自定义键值 */
    public void setUserData(Account account, String key, String value) {
        try {
            getService().setUserData(account, key, value, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 授权某 uid 访问指定 token 类型 */
    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) {
        try {
            getService().updateAppPermission(account, authTokenType, uid, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 异步获取 token */
    public void getAuthToken(IAccountManagerResponse response, Account account,
                      String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch,
                      Bundle options) {
        try {
            getService().getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 异步新增账户 */
    public void addAccount(IAccountManagerResponse response, String accountType,
                    String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch,
                    Bundle options) {
        try {
            getService().addAccount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 异步新增账户到当前用户 */
    public void addAccountAsUser(IAccountManagerResponse response, String accountType,
                          String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch,
                          Bundle options) {
        try {
            getService().addAccountAsUser(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 更新凭据 */
    public void updateCredentials(IAccountManagerResponse response, Account account,
                           String authTokenType, boolean expectActivityLaunch, Bundle options) {
        try {
            getService().updateCredentials(response, account, authTokenType, expectActivityLaunch, options, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 编辑账户属性 */
    public void editProperties(IAccountManagerResponse response, String accountType,
                        boolean expectActivityLaunch) {
        try {
            getService().editProperties(response, accountType, expectActivityLaunch, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 确认凭据 */
    public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account,
                                  Bundle options, boolean expectActivityLaunch) {
        try {
            getService().confirmCredentialsAsUser(response, account, options, expectActivityLaunch, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 账户标记为已认证 */
    public boolean accountAuthenticated(Account account) {
        try {
            return getService().accountAuthenticated(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 查询 token 标签 */
    public void getAuthTokenLabel(IAccountManagerResponse response, String accountType,
                           String authTokenType) {
        try {
            getService().getAuthTokenLabel(response, accountType, authTokenType, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 查询包与账户可见性映射 */
    public Map getPackagesAndVisibilityForAccount(Account account) {
        try {
            return getService().getPackagesAndVisibilityForAccount(account, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 创建账户并设置可见性 */
    public boolean addAccountExplicitlyWithVisibility(Account account, String password, Bundle extras,
                                               Map visibility) {
        try {
            return getService().addAccountExplicitlyWithVisibility(account, password, extras, visibility, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 设置某包对账户的可见性 */
    public boolean setAccountVisibility(Account account, String packageName, int newVisibility) {
        try {
            return getService().setAccountVisibility(account, packageName, newVisibility, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 查询某包对账户的可见性 */
    public int getAccountVisibility(Account account, String packageName) {
        try {
            return getService().getAccountVisibility(account, packageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // 3 = VISIBILITY_UNDEFINED 的常见语义
        return 3;
    }

    /** 查询某包的账户与可见性 */
    public Map getAccountsAndVisibilityForPackage(String packageName, String accountType) {
        try {
            return getService().getAccountsAndVisibilityForPackage(packageName, accountType, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 注册账户监听 */
    public void registerAccountListener(String[] accountTypes, String opPackageName) {
        try {
            getService().registerAccountListener(accountTypes, opPackageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 取消账户监听 */
    public void unregisterAccountListener(String[] accountTypes, String opPackageName) {
        try {
            getService().unregisterAccountListener(accountTypes, opPackageName, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
