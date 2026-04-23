package top.niunaijun.blackbox.fake;

import top.niunaijun.jnihook.ReflectCore;


/**
 * Fake 核心入口：
 * - 初始化 JNI Hook 桥（ReflectCore）到 ActivityThread，用于后续反射/代理加速；
 * - 仅在进程启动时调用一次。
 */
public class FakeCore {
    public static void init() {
        ReflectCore.set(android.app.ActivityThread.class);
    }
}
