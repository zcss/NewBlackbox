package top.niunaijun.blackboxa.view.list;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import top.niunaijun.blackboxa.bean.InstalledAppBean;
import top.niunaijun.blackboxa.data.AppsRepository;
import top.niunaijun.blackboxa.view.base.BaseViewModel;

/**
 * 安装列表 ViewModel：负责获取用户已安装应用列表并管理加载态。
 */
public class ListViewModel extends BaseViewModel {
    /** 数据仓库。*/
    private final AppsRepository repo;

    /** 应用列表数据。*/
    private final MutableLiveData<List<InstalledAppBean>> appsLiveData = new MutableLiveData<>();
    /** 加载状态。*/
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    /** 注入数据仓库。*/
    public ListViewModel(AppsRepository repo) {
        this.repo = repo;
    }

    /** 获取应用列表 LiveData。*/
    public MutableLiveData<List<InstalledAppBean>> getAppsLiveData() {
        return appsLiveData;
    }

    /** 获取加载状态 LiveData。*/
    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    /** 预热安装列表，便于后续快速展示。*/
    public void previewInstalledList() {
        launchOnUI(object -> {
            repo.previewInstallList();
        });
    }

    /** 获取指定用户的已安装应用列表。*/
    public void getInstallAppList(final int userID) {
        launchOnUI(object -> {
            repo.getInstalledAppList(userID, loadingLiveData, appsLiveData);
        });
    }
}
