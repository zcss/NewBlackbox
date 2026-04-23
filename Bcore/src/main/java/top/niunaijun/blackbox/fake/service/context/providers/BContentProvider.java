package top.niunaijun.blackbox.fake.service.context.providers;

import android.os.IInterface;


/**
 * BContentProvider：定义内容提供者包装接口，用于按应用包名包装系统 IContentProvider。
 * 仅添加中文注释，不改动任何逻辑。
 */
public interface BContentProvider {
    IInterface wrapper(final IInterface contentProviderProxy, final String appPkg);
}
