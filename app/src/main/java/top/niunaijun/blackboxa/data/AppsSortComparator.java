package top.niunaijun.blackboxa.data;

import android.content.pm.ApplicationInfo;

import java.util.Comparator;
import java.util.List;

/**
 * 根据给定的包名顺序列表（sortedList）对 ApplicationInfo 进行排序的比较器。
 * 规则：
 * - 使用包名在 sortedList 中的下标进行比较，下标小的排前面
 * - 若某个包名不在 sortedList 中，indexOf 返回 -1，将会被排在列表更靠前的位置
 *   （注意：这意味着“未配置”的应用会排在“已配置”的应用之前）
 * - 若参与比较的对象为 null，返回 0，不改变相对顺序
 * 性能：每次比较都会执行 indexOf（O(n)），适合列表规模较小或一次性排序的场景。
 */
public final class AppsSortComparator implements Comparator<ApplicationInfo> {
    private final List<String> sortedList;

    /**
     * @param sortedList 偏好顺序的包名列表（优先级从前到后）
     */
    public AppsSortComparator(List<String> sortedList) {
        this.sortedList = sortedList;
    }

    /**
     * 基于包名在 sortedList 中的位置进行比较。
     * 未命中 sortedList 的应用 index 为 -1，从而排在更前面。
     */
    @Override
    public int compare(ApplicationInfo o1, ApplicationInfo o2) {
        if (o1 == null || o2 == null) return 0;
        int first = sortedList.indexOf(o1.packageName);
        int second = sortedList.indexOf(o2.packageName);
        return first - second;
    }
}
