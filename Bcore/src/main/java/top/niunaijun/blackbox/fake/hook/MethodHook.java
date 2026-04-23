package top.niunaijun.blackbox.fake.hook;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.BlackBoxCore;


/**
 * 方法级 Hook 定义：
 * - 支持 before/after 两阶段拦截与替换返回值；
 * - isEnable 缺省仅在虚拟进程内生效；
 * - 子类实现 hook 执行核心逻辑。
 */
public abstract class MethodHook {
    protected String getMethodName() {
        return null;
    }

    protected Object afterHook(Object result) throws Throwable {
        return result;
    }

    protected Object beforeHook(Object who, Method method, Object[] args) throws Throwable {
        return null;
    }

    protected abstract Object hook(Object who, Method method, Object[] args) throws Throwable;

    protected boolean isEnable() {
        return BlackBoxCore.get().isBlackProcess();
    }
}
