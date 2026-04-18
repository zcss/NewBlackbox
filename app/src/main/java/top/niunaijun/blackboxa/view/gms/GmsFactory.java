package top.niunaijun.blackboxa.view.gms;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.data.GmsRepository;

public class GmsFactory extends ViewModelProvider.NewInstanceFactory {
    private final GmsRepository repo;

    public GmsFactory(GmsRepository repo) {
        this.repo = repo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GmsViewModel(repo);
    }
}
