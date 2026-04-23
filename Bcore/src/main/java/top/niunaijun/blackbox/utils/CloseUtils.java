package top.niunaijun.blackbox.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * 关闭工具：静默关闭多个 Closeable 实例，忽略 IO 异常。
 */
public class CloseUtils {
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
