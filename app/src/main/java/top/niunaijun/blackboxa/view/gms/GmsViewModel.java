package top.niunaijun.blackboxa.view.gms;

import androidx.lifecycle.MutableLiveData;

import top.niunaijun.blackboxa.bean.GmsBean;
import top.niunaijun.blackboxa.bean.GmsInstallBean;
import top.niunaijun.blackboxa.data.GmsRepository;
import top.niunaijun.blackboxa.view.base.BaseViewModel;

import java.util.List;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;

/**
 * GMS 管理 ViewModel：查询安装状态并执行安装/卸载操作。
 */
public class GmsViewModel extends BaseViewModel {
    /** 数据仓库。*/
    private final GmsRepository mRepo;

    /** 用户与 GMS 安装状态列表。*/
    private final MutableLiveData<List<GmsBean>> mInstalledLiveData = new MutableLiveData<>();
    /** 单个用户安装/卸载操作结果。*/
    private final MutableLiveData<GmsInstallBean> mUpdateInstalledLiveData = new MutableLiveData<>();

    /** 注入数据仓库。*/
    public GmsViewModel(GmsRepository repo) {
        this.mRepo = repo;
    }

    /** 获取 GMS 安装状态列表 LiveData。*/
    public MutableLiveData<List<GmsBean>> getMInstalledLiveData() {
        return mInstalledLiveData;
    }

    /** 获取单用户安装/卸载结果 LiveData。*/
    public MutableLiveData<GmsInstallBean> getMUpdateInstalledLiveData() {
        return mUpdateInstalledLiveData;
    }

    /** 查询所有用户的 GMS 安装状态。*/
    public void getInstalledUser() {
        launchOnUI(object -> {
            mRepo.getGmsInstalledList(mInstalledLiveData);
        });
    }

    /** 为指定用户安装 GMS。*/
    public void installGms(final int userID) {
        launchOnUI(object -> {
            mRepo.installGms(userID, mUpdateInstalledLiveData);
        });
    }

    /** 为指定用户卸载 GMS。*/
    public void uninstallGms(final int userID) {
        launchOnUI(object -> {
            mRepo.uninstallGms(userID, mUpdateInstalledLiveData);
        });
    }
}
