package top.niunaijun.blackbox.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ProviderInfo;

import java.util.Objects;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;

import static android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE;

/**
 * 组件相关工具：安装包请求检测、同包判断、TaskAffinity 推导、Intent 过滤相等判定等。
 */
public class ComponentUtils {

    /** 是否是 apk 安装请求（pm 安装类型）。 */
    public static boolean isRequestInstall(Intent intent) {
        return "application/vnd.android.package-archive".equals(intent.getType());
    }

    /** Intent 是否指向本应用包（根据组件包名）。 */
    public static boolean isSelf(Intent intent) {
        ComponentName component = intent.getComponent();
        if (component == null || BlackBoxCore.getAppPackageName() == null) return false;
        return component.getPackageName().equals(BlackBoxCore.getAppPackageName());
    }

    /** 多个 Intent 是否都指向本应用包。 */
    public static boolean isSelf(Intent[] intent) {
        for (Intent intent1 : intent) {
            if (!isSelf(intent1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 计算 Activity 的任务亲和性：处理 SingleInstance、显式/应用级 taskAffinity 等情况。
     */
    public static String getTaskAffinity(ActivityInfo info) {
        if (info.launchMode == LAUNCH_SINGLE_INSTANCE) {
            return "-SingleInstance-" + info.packageName + "/" + info.name;
        } else if (info.taskAffinity == null && info.applicationInfo.taskAffinity == null) {
            return info.packageName;
        } else if (info.taskAffinity != null) {
            return info.taskAffinity;
        }
        return info.applicationInfo.taskAffinity;
    }

    /** ProviderInfo 可能包含多个 authority，用第一个为“主 authority”。 */
    public static String getFirstAuthority(ProviderInfo info) {
        if (info == null) {
            return null;
        }
        String[] authorities = info.authority.split(";");
        return authorities.length == 0 ? info.authority : authorities[0];
    }

    /** 对比 Intent 的过滤关键字段是否一致。 */
    public static boolean intentFilterEquals(Intent a, Intent b) {
        if (a != null && b != null) {
            if (!Objects.equals(a.getAction(), b.getAction())) {
                return false;
            }
            if (!Objects.equals(a.getData(), b.getData())) {
                return false;
            }
            if (!Objects.equals(a.getType(), b.getType())) {
                return false;
            }
            Object pkgA = a.getPackage();
            if (pkgA == null && a.getComponent() != null) {
                pkgA = a.getComponent().getPackageName();
            }
            String pkgB = b.getPackage();
            if (pkgB == null && b.getComponent() != null) {
                pkgB = b.getComponent().getPackageName();
            }
            if (!Objects.equals(pkgA, pkgB)) {
                return false;
            }
            if (!Objects.equals(a.getComponent(), b.getComponent())) {
                return false;
            }
            if (!Objects.equals(a.getCategories(), b.getCategories())) {
                return false;
            }
        }
        return true;
    }

    /** 返回组件进程名（为空时回填为包名并更新对象）。 */
    public static String getProcessName(ComponentInfo componentInfo) {
        String processName = componentInfo.processName;
        if (processName == null) {
            processName = componentInfo.packageName;
            componentInfo.processName = processName;
        }
        return processName;
    }

    /** 比较两个 ComponentInfo 是否表示同一组件（按包名与类名）。 */
    public static boolean isSameComponent(ComponentInfo first, ComponentInfo second) {
        if (first != null && second != null) {
            String pkg1 = first.packageName + "";
            String pkg2 = second.packageName + "";
            String name1 = first.name + "";
            String name2 = second.name + "";
            return pkg1.equals(pkg2) && name1.equals(name2);
        }
        return false;
    }

    /** 将 ComponentInfo 转为 ComponentName。 */
    public static ComponentName toComponentName(ComponentInfo componentInfo) {
        return new ComponentName(componentInfo.packageName, componentInfo.name);
    }
}
