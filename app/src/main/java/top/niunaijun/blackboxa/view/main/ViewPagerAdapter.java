package top.niunaijun.blackboxa.view.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackboxa.view.apps.AppsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<AppsFragment> fragmentList = new ArrayList<>();

    public ViewPagerAdapter(@NonNull AppCompatActivity appCompatActivity) {
        super(appCompatActivity);
    }

    public void replaceData(@NonNull List<AppsFragment> list) {
        this.fragmentList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
