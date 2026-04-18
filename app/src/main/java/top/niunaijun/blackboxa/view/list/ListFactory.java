package top.niunaijun.blackboxa.view.list;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.data.AppsRepository;

public class ListFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppsRepository appsRepository;

    public ListFactory(AppsRepository appsRepository) {
        this.appsRepository = appsRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ListViewModel(appsRepository);
    }
}
