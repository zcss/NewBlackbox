package top.niunaijun.blackboxa.view.list;

import android.view.View;
import android.view.ViewGroup;

import cbfg.rvadapter.RVHolder;
import cbfg.rvadapter.RVHolderFactory;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.InstalledAppBean;
import top.niunaijun.blackboxa.databinding.ItemPackageBinding;

/**
 * 安装应用选择列表适配器。
 */
public class ListAdapter extends RVHolderFactory {
    public static int HOLDER_PATH_PROXY = 1;
    public static int HOLDER_DEFAULT = 0;
    private int type = 0;
    public ListAdapter(){

    }
    public ListAdapter(int type){
        this.type = type;
    }

    /** 创建 ViewHolder。*/
    @Override
    public RVHolder<?> createViewHolder(ViewGroup parent, int viewType, Object item) {
        if (type == 1) {
            return new ListVHPath(inflate(R.layout.item_package, parent));
        } else {
            return new ListVH(inflate(R.layout.item_package, parent));
        }
    }

    /** 列表项 ViewHolder。*/
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
    static class ListVHPath extends RVHolder<InstalledAppBean> {
        final ItemPackageBinding binding;

        ListVHPath(View itemView) {
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
