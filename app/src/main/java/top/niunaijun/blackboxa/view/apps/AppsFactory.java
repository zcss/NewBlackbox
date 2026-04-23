package top.niunaijun.blackboxa.view.apps;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.data.AppsRepository;

/**
 * AppsViewModel 工厂：注入 AppsRepository。
 */
public class AppsFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppsRepository appsRepository;

    /** 构造工厂并持有仓库实例。*/
    public AppsFactory(AppsRepository appsRepository) {
        this.appsRepository = appsRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    /** 创建 AppsViewModel 实例。*/
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AppsViewModel(appsRepository);
    }
}
