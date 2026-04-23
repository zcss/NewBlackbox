package top.niunaijun.blackboxa.util;

import top.niunaijun.blackboxa.data.AppsRepository;
import top.niunaijun.blackboxa.data.FakeLocationRepository;
import top.niunaijun.blackboxa.data.GmsRepository;
import top.niunaijun.blackboxa.view.apps.AppsFactory;
import top.niunaijun.blackboxa.view.fake.FakeLocationFactory;
import top.niunaijun.blackboxa.view.gms.GmsFactory;
import top.niunaijun.blackboxa.view.list.ListFactory;

/**
 * 简易依赖注入工具：集中管理 Repository 与对应 ViewModel 工厂。
 */
public final class InjectionUtil {
    private static final AppsRepository appsRepository = new AppsRepository();
    private static final GmsRepository gmsRepository = new GmsRepository();
    private static final FakeLocationRepository fakeLocationRepository = new FakeLocationRepository();

    private InjectionUtil() {}

    /** 获取应用列表 ViewModel 工厂。*/
    public static AppsFactory getAppsFactory() {
        return new AppsFactory(appsRepository);
    }

    /** 获取安装列表（选择 APK）ViewModel 工厂。*/
    public static ListFactory getListFactory() {
        return new ListFactory(appsRepository);
    }

    /** 获取 GMS 管理 ViewModel 工厂。*/
    public static GmsFactory getGmsFactory() {
        return new GmsFactory(gmsRepository);
    }

    /** 获取虚拟定位 ViewModel 工厂。*/
    public static FakeLocationFactory getFakeLocationFactory() {
        return new FakeLocationFactory(fakeLocationRepository);
    }
}
