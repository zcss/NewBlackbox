package top.niunaijun.blackboxa.view.fake;

import androidx.lifecycle.MutableLiveData;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import top.niunaijun.blackbox.entity.location.BLocation;
import top.niunaijun.blackboxa.bean.FakeLocationBean;
import top.niunaijun.blackboxa.data.FakeLocationRepository;
import top.niunaijun.blackboxa.view.base.BaseViewModel;

import java.util.List;

/**
 * 虚拟定位 ViewModel：负责列表获取、模式设置与坐标更新。
 */
public class FakeLocationViewModel extends BaseViewModel {
    /** 数据仓库。*/
    private final FakeLocationRepository mRepo;

    /** 虚拟定位页面数据。*/
    private final MutableLiveData<List<FakeLocationBean>> appsLiveData = new MutableLiveData<>();

    /** 注入数据仓库。*/
    public FakeLocationViewModel(FakeLocationRepository repo) {
        this.mRepo = repo;
    }

    /** 获取页面 LiveData。*/
    public MutableLiveData<List<FakeLocationBean>> getAppsLiveData() {
        return appsLiveData;
    }

    public void getInstallAppList(int userID) {
        launchOnUI(object ->  {
            mRepo.getInstalledAppList(userID, appsLiveData);
        });
    }

    public void setPattern(int userId, String pkg, int pattern) {
        launchOnUI(object ->  {
            mRepo.setPattern(userId, pkg, pattern);
        });
    }

    public void setLocation(int userId, String pkg, BLocation location) {
        launchOnUI(object ->  {
            mRepo.setLocation(userId, pkg, location);
        });
    }
}
