package top.niunaijun.blackbox.fake.service;

import android.os.Build;
import android.os.Process;
import java.lang.reflect.Method;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


/**
 * Browser/进程引擎代理：
 * - 观测 Process.start/setArgV0/killProcess 等关键调用，记录参数用于问题定位；
 * - 不改变原有行为，仅做日志增强。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BrowserEngineProxy extends ClassInvocationStub {
    public static final String TAG = "BrowserEngineProxy";

    public BrowserEngineProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        return null;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    
    @ProxyMethod("start")
    public static class ProcessStart extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                
                Object result = method.invoke(who, args);
                
                
                if (args != null && args.length > 0) {
                    Slog.d(TAG, "Process.start() called with args count: " + args.length);
                }
                
                return result;
            } catch (Exception e) {
                Slog.e(TAG, "Process.start() failed: " + e.getMessage(), e);
                
                throw e;
            }
        }
    }

    
    @ProxyMethod("setArgV0")
    public static class SetArgV0 extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0) {
                String argV0 = (String) args[0];
                Slog.d(TAG, "Process.setArgV0() called: " + argV0);
            }
            return method.invoke(who, args);
        }
    }

    
    @ProxyMethod("killProcess")
    public static class KillProcess extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 0) {
                int pid = (int) args[0];
                Slog.d(TAG, "Process.killProcess() called for PID: " + pid);
            }
            return method.invoke(who, args);
        }
    }
}
