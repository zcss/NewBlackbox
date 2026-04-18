package top.niunaijun.blackboxa.data;

import android.content.pm.ApplicationInfo;

import java.util.Comparator;
import java.util.List;

public final class AppsSortComparator implements Comparator<ApplicationInfo> {
    private final List<String> sortedList;

    public AppsSortComparator(List<String> sortedList) {
        this.sortedList = sortedList;
    }

    @Override
    public int compare(ApplicationInfo o1, ApplicationInfo o2) {
        if (o1 == null || o2 == null) return 0;
        int first = sortedList.indexOf(o1.packageName);
        int second = sortedList.indexOf(o2.packageName);
        return first - second;
    }
}
