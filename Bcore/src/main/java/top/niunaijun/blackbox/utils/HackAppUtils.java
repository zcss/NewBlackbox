package top.niunaijun.blackbox.utils;

/**
 * 针对特定 App 的兼容小工具：例如开启 QQ 的日志输出等级，便于调试。
 */
public class HackAppUtils {

    /** 若是 QQ 包名，则通过反射提升 QLog 的 UIN_REPORTLOG_LEVEL。 */
    public static void enableQQLogOutput(String packageName, ClassLoader classLoader) {
        if ("com.tencent.mobileqq".equals(packageName)) {
            try {
                Reflector.on("com.tencent.qphone.base.util.QLog", true, classLoader)
                        .field("UIN_REPORTLOG_LEVEL")
                        .set(100);
            } catch (Exception e) {
                e.printStackTrace();
                // 忽略失败，避免影响运行
            }
        }
    }
}
