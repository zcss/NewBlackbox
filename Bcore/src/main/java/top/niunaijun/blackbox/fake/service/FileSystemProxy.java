package top.niunaijun.blackbox.fake.service;

import java.io.File;
import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


/**
 * File 系统代理：
 * - 拦截 mkdirs/mkdir/isDirectory，对 Helium Crashpad/CrashReporter 等路径返回宽松结果，避免目录校验导致崩溃；
 * - 其他路径透传原生行为，异常时提供保守默认值。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class FileSystemProxy extends ClassInvocationStub {
    public static final String TAG = "FileSystemProxy";

    public FileSystemProxy() {
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

    
    @ProxyMethod("mkdirs")
    public static class Mkdirs extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                File file = (File) who;
                String path = file.getAbsolutePath();
                
                
                if (path.contains("Helium Crashpad") || path.contains("HeliumCrashReporter")) {
                    Slog.d(TAG, "FileSystem: mkdirs called for Helium crash path: " + path + ", returning true");
                    return true; 
                }
                
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "FileSystem: mkdirs failed, returning true", e);
                return true; 
            }
        }
    }

    
    @ProxyMethod("mkdir")
    public static class Mkdir extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                File file = (File) who;
                String path = file.getAbsolutePath();
                
                
                if (path.contains("Helium Crashpad") || path.contains("HeliumCrashReporter")) {
                    Slog.d(TAG, "FileSystem: mkdir called for Helium crash path: " + path + ", returning true");
                    return true; 
                }
                
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "FileSystem: mkdir failed, returning true", e);
                return true; 
            }
        }
    }

    
    @ProxyMethod("isDirectory")
    public static class IsDirectory extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                File file = (File) who;
                String path = file.getAbsolutePath();
                
                
                if (path.contains("Helium Crashpad") || path.contains("HeliumCrashReporter")) {
                    Slog.d(TAG, "FileSystem: isDirectory called for Helium crash path: " + path + ", returning true");
                    return true; 
                }
                
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "FileSystem: isDirectory failed, returning false", e);
                return false; 
            }
        }
    }
}
