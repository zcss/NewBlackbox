package top.niunaijun.blackboxa.util;

import top.niunaijun.blackboxa.data.AppsRepository;
import top.niunaijun.blackboxa.data.FakeLocationRepository;
import top.niunaijun.blackboxa.data.GmsRepository;
import top.niunaijun.blackboxa.view.apps.AppsFactory;
import top.niunaijun.blackboxa.view.fake.FakeLocationFactory;
import top.niunaijun.blackboxa.view.gms.GmsFactory;
import top.niunaijun.blackboxa.view.list.ListFactory;

public final class InjectionUtil {
    private static final AppsRepository appsRepository = new AppsRepository();
    private static final GmsRepository gmsRepository = new GmsRepository();
    private static final FakeLocationRepository fakeLocationRepository = new FakeLocationRepository();

    private InjectionUtil() {}

    public static AppsFactory getAppsFactory() {
        return new AppsFactory(appsRepository);
    }

    public static ListFactory getListFactory() {
        return new ListFactory(appsRepository);
    }

    public static GmsFactory getGmsFactory() {
        return new GmsFactory(gmsRepository);
    }

    public static FakeLocationFactory getFakeLocationFactory() {
        return new FakeLocationFactory(fakeLocationRepository);
    }
}
