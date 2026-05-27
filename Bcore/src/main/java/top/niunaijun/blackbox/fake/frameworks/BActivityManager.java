package top.niunaijun.blackbox.fake.frameworks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;

import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.am.IBActivityManagerService;
import top.niunaijun.blackbox.entity.AppConfig;
import top.niunaijun.blackbox.entity.UnbindRecord;
import top.niunaijun.blackbox.entity.am.PendingResultData;
import top.niunaijun.blackbox.entity.am.RunningAppProcessInfo;
import top.niunaijun.blackbox.entity.am.RunningServiceInfo;
import top.niunaijun.blackbox.utils.Slog;


/**
 * 虚拟 AMS 框架代理：
 * - 封装对 IBActivityManagerService 的 Binder 调用，提供重试与 DeadObject 处理；
 * - 统一 Activity/Service/Provider/Broadcast 的入口；
 * - 供各 Hook 代理在进程内调用。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BActivityManager extends BlackManager<IBActivityManagerService> {
    private static final String TAG = "BActivityManager";
    /** 单例 */
    private static final BActivityManager sActivityManager = new BActivityManager();

    /** 获取单例 */
    public static BActivityManager get() {
        return sActivityManager;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.ACTIVITY_MANAGER;
    }

    /**
     * 初始化/附加虚拟进程，返回进程环境配置。
     */
    public AppConfig initProcess(String packageName, String processName, int userId) {
        int retryCount = 0;
        final int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                IBActivityManagerService service = getService();
                if (service != null) {
                    AppConfig result = service.initProcess(packageName, processName, userId);
                    if (result != null) {
                        return result;
                    } else {
                        Slog.w(TAG, "initProcess returned null for package: " + packageName + ", process: " + processName + ", retry " + (retryCount + 1) + "/" + maxRetries);
                    }
                } else {
                    Slog.w(TAG, "ActivityManager service is null for initProcess, retry " + (retryCount + 1) + "/" + maxRetries);
                }
            } catch (DeadObjectException e) {
                Slog.w(TAG, "ActivityManager service died during initProcess, clearing cache and retrying " + (retryCount + 1) + "/" + maxRetries, e);
                clearServiceCache();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in initProcess", e);
                break;
            } catch (Exception e) {
                Slog.e(TAG, "Unexpected error in initProcess", e);
                break;
            }
            retryCount++;
        }

        Slog.e(TAG, "Failed to initProcess after " + maxRetries + " retries for package: " + packageName + ", process: " + processName);
        return null;
    }

    /** 请求重启指定进程 */
    public void restartProcess(String packageName, String processName, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.restartProcess(packageName, processName, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 启动 Activity（带重试与死亡处理） */
    public void startActivity(Intent intent, int userId) {
        int retryCount = 0;
        final int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                IBActivityManagerService service = getService();
                if (service != null) {
                    service.startActivity(intent, userId);
                    return; // 成功即返回
                } else {
                    Slog.w(TAG, "ActivityManager service is null, retry " + (retryCount + 1) + "/" + maxRetries);

                    try {
                        Thread.sleep(200 * (retryCount + 1)); // 线性退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (DeadObjectException e) {
                Slog.w(TAG, "ActivityManager service died, clearing cache and retrying " + (retryCount + 1) + "/" + maxRetries);
                clearServiceCache(); // 立即清理缓存
                try {
                    Thread.sleep(100); // 小延迟再试
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in startActivity", e);
                break; // 远端异常不再重试
            } catch (Exception e) {
                Slog.e(TAG, "Unexpected error in startActivity", e);
                break;
            }
            retryCount++;
        }

        Slog.e(TAG, "Failed to start activity after " + maxRetries + " retries");
    }

    /** 通过 AMS 路径启动 Activity（与系统交互参数） */
    public int startActivityAms(int userId, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int flags, Bundle options) {
        int retryCount = 0;
        final int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                IBActivityManagerService service = getService();
                if (service != null) {
                    return service.startActivityAms(userId, intent, resolvedType, resultTo, resultWho, requestCode, flags, options);
                } else {
                    Slog.w(TAG, "ActivityManager service is null, retry " + (retryCount + 1) + "/" + maxRetries);
                }
            } catch (DeadObjectException e) {
                Slog.w(TAG, "ActivityManager service died, clearing cache and retrying " + (retryCount + 1) + "/" + maxRetries);
                clearServiceCache();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in startActivityAms", e);
                break;
            } catch (Exception e) {
                Slog.e(TAG, "Unexpected error in startActivityAms", e);
                break;
            }
            retryCount++;
        }

        Slog.e(TAG, "Failed to start activity AMS after " + maxRetries + " retries");
        return -1;
    }

    /** 批量启动 Activities */
    public int startActivities(int userId, Intent[] intent, String[] resolvedType, IBinder resultTo, Bundle options) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                return service.startActivities(userId, intent, resolvedType, resultTo, options);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** 启动 Service（支持前台要求） */
    public ComponentName startService(Intent intent, String resolvedType, boolean requireForeground, int userId) {
        int retryCount = 0;
        final int maxRetries = 3;

        while (retryCount < maxRetries) {
            try {
                IBActivityManagerService service = getService();
                if (service != null) {
                    return service.startService(intent, resolvedType, requireForeground, userId);
                } else {
                    Slog.w(TAG, "ActivityManager service is null, retry " + (retryCount + 1) + "/" + maxRetries);
                }
            } catch (DeadObjectException e) {
                Slog.w(TAG, "ActivityManager service died, clearing cache and retrying " + (retryCount + 1) + "/" + maxRetries);
                clearServiceCache();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in startService", e);
                break;
            } catch (Exception e) {
                Slog.e(TAG, "Unexpected error in startService", e);
                break;
            }
            retryCount++;
        }

        Slog.e(TAG, "Failed to start service after " + maxRetries + " retries");
        return null;
    }

    /** 停止 Service */
    public int stopService(Intent intent, String resolvedType, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                return service.stopService(intent, resolvedType, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** 绑定 Service */
    public Intent bindService(Intent service, IBinder binder, String resolvedType, int userId) {
        try {
            IBActivityManagerService serviceManager = getService();
            if (serviceManager != null) {
                return serviceManager.bindService(service, binder, resolvedType, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 解绑 Service */
    public void unbindService(IBinder binder, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.unbindService(binder, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 停止 Service Token */
    public void stopServiceToken(ComponentName componentName, IBinder token, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.stopServiceToken(componentName, token, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Service onStartCommand 回调分发 */
    public void onStartCommand(Intent proxyIntent, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.onStartCommand(proxyIntent, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Service onUnbind 回调分发 */
    public UnbindRecord onServiceUnbind(Intent proxyIntent, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                return service.onServiceUnbind(proxyIntent, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Service onDestroy 回调分发 */
    public void onServiceDestroy(Intent proxyIntent, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.onServiceDestroy(proxyIntent, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取 ContentProvider Client binder */
    public IBinder acquireContentProviderClient(ProviderInfo providerInfo) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                return service.acquireContentProviderClient(providerInfo);
            } else {
                Slog.w(TAG, "ActivityManager service is null for acquireContentProviderClient");
            }
        } catch (DeadObjectException e) {
            Slog.w(TAG, "ActivityManager service died during acquireContentProviderClient, clearing cache", e);
            clearServiceCache();
        } catch (RemoteException e) {
            Slog.e(TAG, "RemoteException in acquireContentProviderClient", e);
        } catch (Exception e) {
            Slog.e(TAG, "Unexpected error in acquireContentProviderClient", e);
        }
        return null;
    }

    /** 发送广播 */
    public Intent sendBroadcast(Intent intent, String resolvedType, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                return service.sendBroadcast(intent, resolvedType, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询 Service binder（不触发绑定） */
    public IBinder peekService(Intent intent, String resolvedType, int userId) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                return service.peekService(intent, resolvedType, userId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Activity 生命周期：创建 */
    public void onActivityCreated(int taskId, IBinder token, IBinder activityRecord) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.onActivityCreated(taskId, token, activityRecord);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Activity 生命周期：恢复 */
    public void onActivityResumed(IBinder token) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.onActivityResumed(token);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Activity 生命周期：销毁 */
    public void onActivityDestroyed(IBinder token) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.onActivityDestroyed(token);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Activity 结束 */
    public void onFinishActivity(IBinder token) {
        try {
            IBActivityManagerService service = getService();
            if (service != null) {
                service.onFinishActivity(token);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取运行中进程信息 */
    public RunningAppProcessInfo getRunningAppProcesses(String callerPackage, int userId) throws RemoteException {
        try {
            return getService().getRunningAppProcesses(callerPackage, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 获取运行中服务信息 */
    public RunningServiceInfo getRunningServices(String callerPackage, int userId) throws RemoteException {
        try {
            return getService().getRunningServices(callerPackage, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 调度广播接收 */
    public void scheduleBroadcastReceiver(Intent intent, PendingResultData pendingResultData, int userId) throws RemoteException {
        getService().scheduleBroadcastReceiver(intent, pendingResultData, userId);
    }

    /** 结束广播 */
    public void finishBroadcast(PendingResultData data) {
        try {
            getService().finishBroadcast(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 查询调用包名 */
    public String getCallingPackage(IBinder token, int userId) {
        try {
            return getService().getCallingPackage(token, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询调用 Activity */
    public ComponentName getCallingActivity(IBinder token, int userId) {
        try {
            return getService().getCallingActivity(token, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 生成 IntentSender */
    public void getIntentSender(IBinder target, String packageName, int uid) {
        try {
            getService().getIntentSender(target, packageName, uid, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 查询 IntentSender 对应包名 */
    public String getPackageForIntentSender(IBinder target) {
        try {
            return getService().getPackageForIntentSender(target, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询 IntentSender 对应 uid */
    public int getUidForIntentSender(IBinder target) {
        try {
            return getService().getUidForIntentSender(target, BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
