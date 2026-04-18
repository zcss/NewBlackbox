package top.niunaijun.blackboxa.view.apps;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.data.AppsRepository;

public class AppsFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppsRepository appsRepository;

    public AppsFactory(AppsRepository appsRepository) {
        this.appsRepository = appsRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AppsViewModel(appsRepository);
    }
}
