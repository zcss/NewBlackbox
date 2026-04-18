package top.niunaijun.blackboxa.view.fake;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import top.niunaijun.blackboxa.data.FakeLocationRepository;

public class FakeLocationFactory extends ViewModelProvider.NewInstanceFactory {
    private final FakeLocationRepository repo;

    public FakeLocationFactory(FakeLocationRepository repo) {
        this.repo = repo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new FakeLocationViewModel(repo);
    }
}
