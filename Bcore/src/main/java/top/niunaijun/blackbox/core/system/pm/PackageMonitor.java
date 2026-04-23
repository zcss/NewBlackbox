package top.niunaijun.blackbox.core.system.pm;


/**
 * 包事件监听：安装/卸载回调接口，便于其他子系统（如广播管理）动态更新。
 */
public interface PackageMonitor {
    void onPackageUninstalled(String packageName, boolean isRemove, int userId);

    void onPackageInstalled(String packageName, int userId);
}
