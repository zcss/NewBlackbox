package top.niunaijun.blackbox.fake.frameworks;

import android.net.Uri;
import android.os.RemoteException;
import android.os.storage.StorageVolume;

import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.os.IBStorageManagerService;


/**
 * 存储框架代理：
 * - 封装获取卷列表与文件 URI 的接口到 IBStorageManagerService；
 * - 远程异常返回空集合/空值，保证调用侧稳态。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BStorageManager extends BlackManager<IBStorageManagerService> {
    /** 单例 */
    private static final BStorageManager sStorageManager = new BStorageManager();

    /** 获取单例 */
    public static BStorageManager get() {
        return sStorageManager;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.STORAGE_MANAGER;
    }

    /** 获取卷列表 */
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags, int userId) {
        try {
            return getService().getVolumeList(uid, packageName, flags, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new StorageVolume[]{};
    }

    /** 将文件路径转换为 content Uri */
    public Uri getUriForFile(String file) {
        try {
            return getService().getUriForFile(file);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
