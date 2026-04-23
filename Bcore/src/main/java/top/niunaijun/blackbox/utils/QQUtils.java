package top.niunaijun.blackbox.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * QQ 工具：通过反射提升 QLog 的日志级别，便于调试。
 */
public class QQUtils {
    public static void hackLog(Context context) {
        try {
            Class<?> aClass = context.getClassLoader().loadClass("com.tencent.qphone.base.util.QLog");
            Field uin_reportlog_level = aClass.getDeclaredField("UIN_REPORTLOG_LEVEL");
            uin_reportlog_level.setAccessible(true);
            uin_reportlog_level.set(null, 1000);
            Log.d("QQUtils", "hackLog: success");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
