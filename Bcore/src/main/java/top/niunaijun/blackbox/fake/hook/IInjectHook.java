package top.niunaijun.blackbox.fake.hook;


/**
 * Hook 注入接口：
 * - injectHook 执行实际注入/替换逻辑；
 * - isBadEnv 用于自检当前环境是否已失效需重注入。
 */
public interface IInjectHook {
    void injectHook();

    boolean isBadEnv();
}
