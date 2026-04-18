package top.niunaijun.blackboxa.view.base;

import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;

public class BaseViewModel extends ViewModel {

    // Simple background executor replacing Kotlin coroutines for Java compatibility
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Keep the same signature used by existing callers
    public void launchOnUI(final ThreadCallBack block) {
        executor.execute(() -> {
            try {
                // We don't use CoroutineScope/Continuation in this Java shim; pass nulls
                block.invoke(null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
    public void launchOnUI2(final Function2<?, ? super Continuation<? super Unit>, Object> block) {
        executor.execute(() -> {
            try {
                // We don't use CoroutineScope/Continuation in this Java shim; pass nulls
                block.invoke(null, null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
    public interface ThreadCallBack<T>{
        public void invoke(T t);
    }
}


