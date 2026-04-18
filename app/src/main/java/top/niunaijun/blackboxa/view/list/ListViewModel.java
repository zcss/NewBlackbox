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

public class ListViewModel extends BaseViewModel {
    private final AppsRepository repo;

    private final MutableLiveData<List<InstalledAppBean>> appsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    public ListViewModel(AppsRepository repo) {
        this.repo = repo;
    }

    public MutableLiveData<List<InstalledAppBean>> getAppsLiveData() {
        return appsLiveData;
    }

    public MutableLiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public void previewInstalledList() {
        launchOnUI(object -> {
            repo.previewInstallList();
        });
    }

    public void getInstallAppList(final int userID) {
        launchOnUI(object -> {
            repo.getInstalledAppList(userID, loadingLiveData, appsLiveData);
        });
    }
}
