package top.niunaijun.blackbox.utils.compat;

import android.accounts.AccountManager;

/**
 * AccountManager 兼容常量：补充旧版本/隐藏字段常用键与错误码，便于跨版本判断。
 */
public class AccountManagerCompat {

    /** 认证失败时是否通知用户。 */
    public static final String KEY_NOTIFY_ON_FAILURE = "notifyOnAuthFailure";

    /** 调用包名。 */
    public static final String KEY_ANDROID_PACKAGE_NAME = "androidPackageName";

    public static final int ERROR_CODE_USER_RESTRICTED = 100;
    public static final int ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE = 101;

    /** 自定义 token 过期时间。 */
    public static final String KEY_CUSTOM_TOKEN_EXPIRY = "android.accounts.expiry";

    /** 上次成功认证时间。 */
    public static final String KEY_LAST_AUTHENTICATED_TIME = "lastAuthenticatedTime";
}
