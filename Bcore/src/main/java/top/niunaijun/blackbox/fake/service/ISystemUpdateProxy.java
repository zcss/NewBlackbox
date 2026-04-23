package top.niunaijun.blackbox.fake.service;


import black.android.os.BRServiceManager;
import black.android.view.BRIAutoFillManagerStub;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;


/**
 * SystemUpdate 代理：
 * - 替换 system_update 服务；透明透传调用，用于隔离环境差异；
 * - 保持系统更新相关接口在虚拟环境下不崩溃。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class ISystemUpdateProxy extends BinderInvocationStub {
    public ISystemUpdateProxy() {
        super(BRServiceManager.get().getService("system_update"));
    }

    @Override
    protected Object getWho() {
        return BRIAutoFillManagerStub.get().asInterface(BRServiceManager.get().getService("system_update"));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService("system_update");
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }
}
