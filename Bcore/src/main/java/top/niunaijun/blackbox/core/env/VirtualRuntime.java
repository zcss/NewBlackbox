package top.niunaijun.blackbox.core.env;

import android.content.pm.ApplicationInfo;

import black.android.ddm.BRDdmHandleAppName;
import black.android.os.BRProcess;

/**
 * 虚拟运行时：记录初始包名/进程名，并向底层设置进程标识与DDMS应用名。
 */
public class VirtualRuntime {

    private static String sInitialPackageName;
    private static String sProcessName;

    public static String getProcessName() {
        return sProcessName;
    }

    public static String getInitialPackageName() {
        return sInitialPackageName;
    }

    public static void setupRuntime(String processName, ApplicationInfo appInfo) {
        if (sProcessName != null) {
            return;
        }
        sInitialPackageName = appInfo.packageName;
        sProcessName = processName;
        BRProcess.get().setArgV0(processName);
        BRDdmHandleAppName.get().setAppName(processName, 0);
    }
}
