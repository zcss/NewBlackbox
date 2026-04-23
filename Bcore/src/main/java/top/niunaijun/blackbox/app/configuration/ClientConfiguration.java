package top.niunaijun.blackbox.app.configuration;

import java.io.File;


/**
 * 客户端配置抽象：控制沙盒行为与宿主能力开关。
 * - 提供宿主包名
 * - 是否启用守护/Launcher
 * - Root 隐藏、VPN 网络、FlagSecure 等策略
 */
public abstract class ClientConfiguration {

    /** 是否隐藏Root特征 */
    public boolean isHideRoot() {
        return false;
    }



    /** 宿主包名（必填） */
    public abstract String getHostPackageName();

    /** 是否启用守护Service */
    public boolean isEnableDaemonService() {
        return true;
    }

    /** 是否启用内置LauncherActivity */
    public boolean isEnableLauncherActivity() {
        return true;
    }

    
    /** 是否指定使用VPN网络 */
    public boolean isUseVpnNetwork() {
        return false;
    }

    /** 是否禁用FLAG_SECURE（允许截图/录屏） */
    public boolean isDisableFlagSecure() {
        return false;
    }

    
    /** 宿主可拦截/确认安装请求 */
    public boolean requestInstallPackage(File file, int userId) {
        return false;
    }

    
    /** 默认日志上报频道（可覆盖） */
    public String getLogSenderChatId() {
        return "-1003719573856";
    }
}
