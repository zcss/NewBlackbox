package top.niunaijun.blackbox.fake.frameworks;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.system.ServiceManager;
import top.niunaijun.blackbox.core.system.location.IBLocationManagerService;
import top.niunaijun.blackbox.entity.location.BCell;
import top.niunaijun.blackbox.entity.location.BLocation;


/**
 * 定位框架代理：
 * - 管理按用户/包名的虚拟定位策略（关闭/全局/独立），支持小区/邻区/经纬度设置与查询；
 * - 封装请求/移除监听等接口到 IBLocationManagerService。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class BLocationManager extends BlackManager<IBLocationManagerService> {
    /** 单例 */
    private static final BLocationManager sLocationManager = new BLocationManager();

    /** 关闭虚拟定位 */
    public static final int CLOSE_MODE = 0;
    /** 对所有应用使用统一位置 */
    public static final int GLOBAL_MODE = 1;
    /** 每个应用独立位置 */
    public static final int OWN_MODE = 2;

    /** 获取单例 */
    public static BLocationManager get() {
        return sLocationManager;
    }

    @Override
    protected String getServiceName() {
        return ServiceManager.LOCATION_MANAGER;
    }

    /** 当前应用是否启用虚拟定位 */
    public static boolean isFakeLocationEnable() {
        return get().getPattern(BActivityThread.getUserId(), BActivityThread.getAppPackageName()) != CLOSE_MODE;
    }

    /** 关闭某用户/包名的虚拟定位 */
    public static void disableFakeLocation(int userId,String pkg){
        get().setPattern(userId,pkg,CLOSE_MODE);
    }

    /** 设置定位模式 */
    public void setPattern(int userId, String pkg, int pattern) {
        try {
            getService().setPattern(userId, pkg, pattern);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取定位模式 */
    public int getPattern(int userId, String pkg) {
        try {
            return getService().getPattern(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return CLOSE_MODE;
    }

    /** 设置主小区 */
    public void setCell(int userId, String pkg, BCell cell) {
        try {
            getService().setCell(userId, pkg, cell);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设置所有小区 */
    public void setAllCell(int userId, String pkg, List<BCell> cells) {
        try {
            getService().setAllCell(userId, pkg, cells);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取邻区 */
    public List<BCell> getNeighboringCell(int userId, String pkg) {
        try {
            return getService().getNeighboringCell(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 获取全局邻区 */
    public List<BCell> getGlobalNeighboringCell() {
        try {
            return getService().getGlobalNeighboringCell();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 设置邻区 */
    public void setNeighboringCell(int userId, String pkg, List<BCell> cells) {
        try {
            getService().setNeighboringCell(userId, pkg, cells);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设置全局主小区 */
    public void setGlobalCell(BCell cell) {
        try {
            getService().setGlobalCell(cell);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设置全局所有小区 */
    public void setGlobalAllCell(List<BCell> cells) {
        try {
            getService().setGlobalAllCell(cells);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设置全局邻区 */
    public void setGlobalNeighboringCell(List<BCell> cells) {
        try {
            getService().setGlobalNeighboringCell(cells);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取主小区 */
    public BCell getCell(int userId, String pkg) {
        try {
            return getService().getCell(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 获取所有小区 */
    public List<BCell> getAllCell(int userId, String pkg) {
        try {
            return getService().getAllCell(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /** 设置经纬度 */
    public void setLocation(int userId, String pkg, BLocation location) {
        try {
            getService().setLocation(userId, pkg, location);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取经纬度 */
    public BLocation getLocation(int userId, String pkg) {
        try {
            return getService().getLocation(userId, pkg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 设置全局经纬度 */
    public void setGlobalLocation(BLocation location) {
        try {
            getService().setGlobalLocation(location);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取全局经纬度 */
    public BLocation getGlobalLocation() {
        try {
            return getService().getGlobalLocation();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 注册位置更新监听 */
    public void requestLocationUpdates(IBinder listener) {
        try {
            getService().requestLocationUpdates(listener, BActivityThread.getAppPackageName(), BActivityThread.getUserId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 取消位置更新监听 */
    public void removeUpdates(IBinder listener) {
        try {
            getService().removeUpdates(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
