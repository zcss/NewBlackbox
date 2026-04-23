package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


/**
 * DeviceId 代理：
 * - 拦截获取/设置/校验/生成/存取设备 ID 的调用；异常或空对象时返回稳定默认值，避免上层崩溃；
 * - 仅用于兜底与隔离真实设备标识，不改变原有读取路径的正常行为。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class DeviceIdProxy extends ClassInvocationStub {
    public static final String TAG = "DeviceIdProxy";

    public DeviceIdProxy() {
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

    
    @ProxyMethod("getDeviceId")
    public static class GetDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (who == null) {
                    Slog.w(TAG, "GetDeviceId called on null object, returning default device ID");
                    return "default_device_id";
                }
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GetDeviceId error, returning default device ID: " + e.getMessage());
                return "default_device_id";
            }
        }
    }

    
    @ProxyMethod("setDeviceId")
    public static class SetDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (who == null) {
                    Slog.w(TAG, "SetDeviceId called on null object, ignoring");
                    return null;
                }
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "SetDeviceId error, ignoring: " + e.getMessage());
                return null;
            }
        }
    }

    
    @ProxyMethod("isValidDeviceId")
    public static class IsValidDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (who == null) {
                    Slog.w(TAG, "IsValidDeviceId called on null object, returning true");
                    return true;
                }
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "IsValidDeviceId error, returning true: " + e.getMessage());
                return true;
            }
        }
    }

    
    @ProxyMethod("generateDeviceId")
    public static class GenerateDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "GenerateDeviceId error, returning default device ID: " + e.getMessage());
                return "generated_device_id_" + System.currentTimeMillis();
            }
        }
    }

    
    @ProxyMethod("storeDeviceId")
    public static class StoreDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (who == null) {
                    Slog.w(TAG, "StoreDeviceId called on null object, ignoring");
                    return null;
                }
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "StoreDeviceId error, ignoring: " + e.getMessage());
                return null;
            }
        }
    }

    
    @ProxyMethod("retrieveDeviceId")
    public static class RetrieveDeviceId extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                if (who == null) {
                    Slog.w(TAG, "RetrieveDeviceId called on null object, returning default device ID");
                    return "retrieved_device_id";
                }
                return method.invoke(who, args);
            } catch (Exception e) {
                Slog.w(TAG, "RetrieveDeviceId error, returning default device ID: " + e.getMessage());
                return "retrieved_device_id";
            }
        }
    }
}
