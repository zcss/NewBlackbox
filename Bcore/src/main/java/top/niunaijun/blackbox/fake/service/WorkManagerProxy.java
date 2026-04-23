package top.niunaijun.blackbox.fake.service;

import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


/**
 * WorkManager 代理：
 * - 通过宿主 Context 反射获取实例，拦截 enqueue/cancel/getWorkInfos 等方法，异常时返回安全默认值；
 * - 主要用于日志观测与在无依赖场景下提供空实现，避免崩溃。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class WorkManagerProxy extends ClassInvocationStub {
    public static final String TAG = "WorkManagerProxy";

    public WorkManagerProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        try {
            Context context = BlackBoxCore.getContext();
            if (context != null) {
                
                Class<?> workManagerClass = Class.forName("androidx.work.WorkManager");
                Method getInstanceMethod = workManagerClass.getMethod("getInstance", Context.class);
                return getInstanceMethod.invoke(null, context);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to get WorkManager instance", e);
        }
        return null;
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    
    @ProxyMethod("enqueue")
    public static class Enqueue extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "WorkManager: enqueue() called");
                
                
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] != null) {
                            Slog.d(TAG, "WorkManager: args[" + i + "] = " + args[i].getClass().getSimpleName() + ": " + args[i]);
                        }
                    }
                }
                
                
                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: enqueue() failed, returning mock result", e);
                
                
                return createMockWorkResult();
            }
        }
        
        private Object createMockWorkResult() {
            try {
                
                Class<?> workResultClass = Class.forName("androidx.work.Operation");
                
                return null; 
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: Failed to create mock result", e);
                return null;
            }
        }
    }

    
    @ProxyMethod("enqueueUniqueWork")
    public static class EnqueueUniqueWork extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "WorkManager: enqueueUniqueWork() called");
                
                
                if (args != null && args.length > 0) {
                    String workName = (String) args[0];
                    Slog.d(TAG, "WorkManager: Unique work name: " + workName);
                }
                
                
                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: enqueueUniqueWork() failed, returning mock result", e);
                
                
                return createMockWorkResult();
            }
        }
        
        private Object createMockWorkResult() {
            try {
                
                Class<?> workResultClass = Class.forName("androidx.work.Operation");
                
                return null; 
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: Failed to create mock result", e);
                return null;
            }
        }
    }

    
    @ProxyMethod("enqueueUniquePeriodicWork")
    public static class EnqueueUniquePeriodicWork extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "WorkManager: enqueueUniquePeriodicWork() called");
                
                
                if (args != null && args.length > 0) {
                    String workName = (String) args[0];
                    Slog.d(TAG, "WorkManager: Periodic work name: " + workName);
                }
                
                
                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: enqueueUniquePeriodicWork() failed, returning mock result", e);
                
                
                return createMockWorkResult();
            }
        }
        
        private Object createMockWorkResult() {
            try {
                
                Class<?> workResultClass = Class.forName("androidx.work.Operation");
                
                return null; 
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: Failed to create mock result", e);
                return null;
            }
        }
    }

    
    @ProxyMethod("cancelAllWork")
    public static class CancelAllWork extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "WorkManager: cancelAllWork() called");
                
                
                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: cancelAllWork() failed, ignoring", e);
                
                
                return null;
            }
        }
    }

    
    @ProxyMethod("cancelWorkById")
    public static class CancelWorkById extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "WorkManager: cancelWorkById() called");
                
                
                if (args != null && args.length > 0) {
                    String workId = (String) args[0];
                    Slog.d(TAG, "WorkManager: Cancelling work ID: " + workId);
                }
                
                
                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: cancelWorkById() failed, ignoring", e);
                
                
                return null;
            }
        }
    }

    
    @ProxyMethod("getWorkInfos")
    public static class GetWorkInfos extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                Slog.d(TAG, "WorkManager: getWorkInfos() called");
                
                
                return method.invoke(who, args);
                
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: getWorkInfos() failed, returning empty list", e);
                
                
                return createEmptyWorkInfoList();
            }
        }
        
        private Object createEmptyWorkInfoList() {
            try {
                
                Class<?> workInfoListClass = Class.forName("androidx.work.WorkInfo");
                
                return java.util.Collections.emptyList();
            } catch (Exception e) {
                Slog.w(TAG, "WorkManager: Failed to create empty work info list", e);
                return java.util.Collections.emptyList();
            }
        }
    }
}
