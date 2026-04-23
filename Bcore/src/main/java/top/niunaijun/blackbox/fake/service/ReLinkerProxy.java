package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


/**
 * ReLinker 代理：
 * - 拦截多重 loadLibrary 重载，记录并直接返回以规避 MissingLibraryException；
 * - 适配三方 ReLinker 在缺失 so 时的兜底行为，避免崩溃。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class ReLinkerProxy extends ClassInvocationStub {
    public static final String TAG = "ReLinkerProxy";

    public ReLinkerProxy() {
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

    
    @ProxyMethod("loadLibrary")
    public static class LoadLibrary extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Slog.d(TAG, "ReLinker: loadLibrary called, intercepting to prevent MissingLibraryException");
            
            
            
            return null;
        }
    }

    
    @ProxyMethod("loadLibrary")
    public static class LoadLibraryWithContext extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 1) {
                String libraryName = (String) args[1];
                Slog.d(TAG, "ReLinker: loadLibrary called for: " + libraryName);
            }
            
            
            return null;
        }
    }

    
    @ProxyMethod("loadLibrary")
    public static class LoadLibraryWithAllParams extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            if (args != null && args.length > 2) {
                String libraryName = (String) args[1];
                String version = (String) args[2];
                Slog.d(TAG, "ReLinker: loadLibrary called for: " + libraryName + " version: " + version);
            }
            
            
            return null;
        }
    }
}
