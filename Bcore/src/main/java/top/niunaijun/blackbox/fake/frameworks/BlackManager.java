package top.niunaijun.blackbox.fake.frameworks;

import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.utils.Reflector;


/**
 * 虚拟框架管理基类：
 * - 基于服务名从 BlackBoxCore 获取 Binder，并通过 $Stub.asInterface 构建接口；
 * - 内置 binder 健康检查、linkToDeath 监听与创建节流；
 * - 提供清缓存与健康探测，供各 B*Manager 复用。
 * 仅添加中文注释，不改动任何逻辑。
 */
public abstract class BlackManager<Service extends IInterface> {
    /** 日志 TAG */
    public static final String TAG = "BlackManager";

    /** 缓存的服务接口实例 */
    private Service mService;
    /** 是否标记过创建服务失败，用于退避重试 */
    private final AtomicBoolean mServiceCreationFailed = new AtomicBoolean(false);
    /** 上次失败后的重试时间戳 */
    private long mLastRetryTime = 0;
    /** 上次成功创建服务的时间戳 */
    private long mLastServiceCreationTime = 0;
    /** 失败后最短重试间隔（毫秒） */
    private static final long RETRY_TIMEOUT_MS = 2000;
    /** 连续创建服务的最短间隔（毫秒），用于限流 */
    private static final long MIN_SERVICE_CREATION_INTERVAL_MS = 50;

    /** 全局服务失败次数（预留，便于全局节流） */
    private static final AtomicInteger globalServiceFailureCount = new AtomicInteger(0);
    /** 全局失败计数的重置间隔（毫秒）（预留） */
    private static final long GLOBAL_FAILURE_RESET_INTERVAL_MS = 30000;
    /** 上次全局失败重置时间（预留） */
    private static long lastGlobalFailureReset = 0;

    /** 返回服务在 ServiceManager 中的注册名 */
    protected abstract String getServiceName();

    /**
     * 获取并缓存远端服务接口。
     * - 具备失败退避、限流与 binder 健康检查；
     * - 在服务死亡时自动清空缓存。
     * @return 可用的 Service 实例或 null
     */
    public Service getService() {
        // 若最近创建失败，遵循退避间隔
        if (mServiceCreationFailed.get()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastRetryTime < RETRY_TIMEOUT_MS) {
                Log.d(TAG, "Skipping service creation for " + getServiceName() + " due to recent failure");
                return null;
            }
            mServiceCreationFailed.set(false);
        }

        // 复用健康的缓存实例
        if (mService != null && mService.asBinder().pingBinder() && mService.asBinder().isBinderAlive()) {
            return mService;
        }

        // 控制创建频率
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastServiceCreationTime < MIN_SERVICE_CREATION_INTERVAL_MS) {
            Log.d(TAG, "Rate limiting service creation for " + getServiceName());
            return mService; // 返回旧值（可能为 null）
        }

        try {
            IBinder binder = BlackBoxCore.get().getService(getServiceName());
            if (binder == null) {
                Log.w(TAG, "Failed to get binder for service: " + getServiceName());
                markServiceCreationFailed();
                return null;
            }

            // 基础健康校验
            if (!binder.isBinderAlive()) {
                Log.w(TAG, "Binder is not alive for service: " + getServiceName());
                markServiceCreationFailed();
                return null;
            }

            String stubClassName = getTClass().getName() + "$Stub";
            Log.d(TAG, "Creating service for: " + stubClassName);

            mService = Reflector.on(stubClassName).method("asInterface", IBinder.class)
                    .call(binder);

            if (mService != null) {
                // 二次健康检查
                try {
                    if (!mService.asBinder().isBinderAlive()) {
                        Log.w(TAG, "Service binder is not alive after creation: " + getServiceName());
                        mService = null;
                        markServiceCreationFailed();
                        return null;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error checking service binder health: " + getServiceName(), e);
                    mService = null;
                    markServiceCreationFailed();
                    return null;
                }

                final Service serviceRef = mService; // 注册死亡监听
                try {
                    serviceRef.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            try {
                                if (serviceRef != null && serviceRef.asBinder() != null) {
                                    serviceRef.asBinder().unlinkToDeath(this, 0);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error unlinking death recipient for " + getServiceName(), e);
                            }
                            mService = null;
                            Log.w(TAG, "Service died: " + getServiceName());
                        }
                    }, 0);
                } catch (Exception e) {
                    Log.w(TAG, "Error linking death recipient for " + getServiceName(), e);
                    // 忽略死亡监听失败，保留实例
                }

                Log.d(TAG, "Successfully created service: " + getServiceName());
                mServiceCreationFailed.set(false);
                mLastServiceCreationTime = currentTime;
            } else {
                Log.w(TAG, "Failed to create service instance for: " + getServiceName());
                markServiceCreationFailed();
            }

            return mService;
        } catch (Throwable e) {
            Log.e(TAG, "Error creating service for " + getServiceName(), e);
            markServiceCreationFailed();
            return null;
        }
    }

    /** 标记创建失败并记录重试时间 */
    private void markServiceCreationFailed() {
        mServiceCreationFailed.set(true);
        mLastRetryTime = System.currentTimeMillis();
    }

    /** 清除服务缓存，下次调用将重新获取 */
    public void clearServiceCache() {
        mService = null;
        Log.d(TAG, "Cleared service cache for " + getServiceName());
    }

    /**
     * 快速探测当前缓存服务是否健康。
     * @return true 表示可用
     */
    public boolean isServiceHealthy() {
        if (mService == null) {
            return false;
        }
        try {
            return mService.asBinder().pingBinder() && mService.asBinder().isBinderAlive();
        } catch (Exception e) {
            Log.w(TAG, "Service health check failed for " + getServiceName(), e);
            return false;
        }
    }

    /**
     * 反射获取泛型参数的 Class（用于拼接 $Stub）。
     */
    private Class<Service> getTClass() {
        return (Class<Service>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
