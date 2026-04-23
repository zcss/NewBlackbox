package top.niunaijun.blackbox.fake.service;

import android.util.Log;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;


/**
 * ResourcesManager 代理：
 * - 拦截 loadApkAssets/loadOverlayFromPath，屏蔽 resource-cache/@idmap/.frro/systemui 等路径，避免资源缓存/叠加导致崩溃或显示异常；
 * - 其余路径透传到原方法，保持系统行为不变。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class ResourcesManagerProxy extends ClassInvocationStub {
    public static final String TAG = "ResourcesManagerProxy";

    private static final String RESOURCES_MANAGER_CLASS = "android.app.ResourcesManager";

    public ResourcesManagerProxy() {
        try {
            Class.forName(RESOURCES_MANAGER_CLASS);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "ResourcesManager class not found: " + e.getMessage());
        }
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

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
    }

    @ProxyMethod("loadApkAssets")
    public static class LoadApkAssets extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String path = (String) args[0];
            
            
            if (path != null && (path.contains("resource-cache") || 
                                path.contains("@idmap") || 
                                path.contains(".frro") ||
                                path.contains("systemui") ||
                                path.contains("data@resource-cache@"))) {
                Log.d(TAG, "Blocking problematic ApkAssets load: " + path);
                
                return null;
            }
            
            
            return method.invoke(who, args);
        }
    }

    @ProxyMethod("loadOverlayFromPath")
    public static class LoadOverlayFromPath extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String path = (String) args[0];
            
            
            if (path != null && (path.contains("resource-cache") || 
                                path.contains("@idmap") || 
                                path.contains(".frro") ||
                                path.contains("systemui") ||
                                path.contains("data@resource-cache@"))) {
                Log.d(TAG, "Blocking problematic overlay path: " + path);
                
                return null;
            }
            
            
            return method.invoke(who, args);
        }
    }
}
