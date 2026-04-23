package top.niunaijun.blackboxa.view.base;

import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;

/**
 * 基础 ViewModel：提供简单的后台执行器与 UI 调度方法。
 */
public class BaseViewModel extends ViewModel {

    /** 简单的后台线程池，用于替代协程在 Java 环境的调度。*/
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 在后台线程执行任务（保持与原调用签名兼容）。
     * @param block 回调任务（入参可为空）
     */
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
    /**
     * 在后台线程执行双参回调（与 Kotlin 侧调用签名保持一致）。
     * @param block Kotlin Function2 形式的回调
     */
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

    /** ViewModel 清理时关闭线程池。*/
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
    /** 简单线程回调接口。*/
    public interface ThreadCallBack<T>{
        /** 执行回调方法。*/
        void invoke(T t);
    }
}


