package top.niunaijun.blackboxa.view.list;

import android.view.View;
import android.view.ViewGroup;

import cbfg.rvadapter.RVHolder;
import cbfg.rvadapter.RVHolderFactory;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.InstalledAppBean;
import top.niunaijun.blackboxa.databinding.ItemPackageBinding;

public class ListAdapter extends RVHolderFactory {
    @Override
    public RVHolder<?> createViewHolder(ViewGroup parent, int viewType, Object item) {
        return new ListVH(inflate(R.layout.item_package, parent));
    }

    static class ListVH extends RVHolder<InstalledAppBean> {
        final ItemPackageBinding binding;

        ListVH(View itemView) {
            super(itemView);
            binding = ItemPackageBinding.bind(itemView);
        }

        @Override
        public void setContent(InstalledAppBean item, boolean isSelected, Object payload) {
            binding.icon.setImageDrawable(item.getIcon());
            binding.name.setText(item.getName());
            binding.packageName.setText(item.getPackageName());
            binding.cornerLabel.setVisibility(item.isInstall() ? View.VISIBLE : View.GONE);
        }
    }
}
