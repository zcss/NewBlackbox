package top.niunaijun.blackbox.fake.service;


import black.android.os.BRServiceManager;
import black.android.service.persistentdata.BRIPersistentDataBlockServiceStub;
import top.niunaijun.blackbox.fake.hook.BinderInvocationStub;
import top.niunaijun.blackbox.fake.service.base.ValueMethodProxy;


/**
 * PersistentDataBlock 代理：
 * - 替换 persistent_data_block 服务；常见读写/容量/解锁接口返回安全默认值；
 * - 防止无硬件支持或权限不足导致的异常。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class IPersistentDataBlockServiceProxy extends BinderInvocationStub {

    public static final String NAME = "persistent_data_block";

    public IPersistentDataBlockServiceProxy() {
        super(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected Object getWho() {
        return BRIPersistentDataBlockServiceStub.get().asInterface(BRServiceManager.get().getService(NAME));
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        replaceSystemService(NAME);
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    @Override
    protected void onBindMethod() {
        super.onBindMethod();
        addMethodHook(new ValueMethodProxy("write", -1));
        addMethodHook(new ValueMethodProxy("read", new byte[0]));
        addMethodHook(new ValueMethodProxy("wipe", null));
        addMethodHook(new ValueMethodProxy("getDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("getMaximumDataBlockSize", 0));
        addMethodHook(new ValueMethodProxy("setOemUnlockEnabled", 0));
        addMethodHook(new ValueMethodProxy("getOemUnlockEnabled", false));
    }
}
