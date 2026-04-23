package top.niunaijun.blackboxa.view.gms;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.data.GmsRepository;

/**
 * GmsViewModel 工厂：注入 GmsRepository。
 */
public class GmsFactory extends ViewModelProvider.NewInstanceFactory {
    private final GmsRepository repo;

    /** 构造工厂并持有仓库实例。*/
    public GmsFactory(GmsRepository repo) {
        this.repo = repo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    /** 创建 GmsViewModel 实例。*/
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GmsViewModel(repo);
    }
}
