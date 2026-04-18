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

public class GmsViewModel extends BaseViewModel {
    private final GmsRepository mRepo;

    private final MutableLiveData<List<GmsBean>> mInstalledLiveData = new MutableLiveData<>();
    private final MutableLiveData<GmsInstallBean> mUpdateInstalledLiveData = new MutableLiveData<>();

    public GmsViewModel(GmsRepository repo) {
        this.mRepo = repo;
    }

    public MutableLiveData<List<GmsBean>> getMInstalledLiveData() {
        return mInstalledLiveData;
    }

    public MutableLiveData<GmsInstallBean> getMUpdateInstalledLiveData() {
        return mUpdateInstalledLiveData;
    }

    public void getInstalledUser() {
        launchOnUI(object -> {
            mRepo.getGmsInstalledList(mInstalledLiveData);
        });
    }

    public void installGms(final int userID) {
        launchOnUI(object -> {
            mRepo.installGms(userID, mUpdateInstalledLiveData);
        });
    }

    public void uninstallGms(final int userID) {
        launchOnUI(object -> {
            mRepo.uninstallGms(userID, mUpdateInstalledLiveData);
        });
    }
}
