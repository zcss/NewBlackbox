package top.niunaijun.blackboxa.view.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackboxa.view.apps.AppsFragment;

/**
 * 主界面 ViewPager 的 Fragment 适配器。
 */
public class ViewPagerAdapter extends FragmentStateAdapter {
    /** 承载的 AppsFragment 列表。*/
    private List<AppsFragment> fragmentList = new ArrayList<>();

    /**
     * 构造适配器。
     * @param appCompatActivity 宿主 Activity
     */
    public ViewPagerAdapter(@NonNull AppCompatActivity appCompatActivity) {
        super(appCompatActivity);
    }

    /**
     * 替换数据集并刷新页面。
     * @param list 新的 AppsFragment 列表
     */
    public void replaceData(@NonNull List<AppsFragment> list) {
        this.fragmentList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    /** 创建指定位置的 Fragment。*/
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    /** 返回 Fragment 数量。*/
    public int getItemCount() {
        return fragmentList.size();
    }
}
