package top.niunaijun.blackboxa.view.fake;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.data.FakeLocationRepository;

/**
 * FakeLocationViewModel 工厂：注入虚拟定位仓库。
 */
public class FakeLocationFactory extends ViewModelProvider.NewInstanceFactory {
    private final FakeLocationRepository repo;

    /** 构造工厂并持有仓库实例。*/
    public FakeLocationFactory(FakeLocationRepository repo) {
        this.repo = repo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    /** 创建 FakeLocationViewModel 实例。*/
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new FakeLocationViewModel(repo);
    }
}
