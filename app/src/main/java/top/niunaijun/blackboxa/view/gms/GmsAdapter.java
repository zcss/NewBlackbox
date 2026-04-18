package top.niunaijun.blackboxa.view.gms;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import cbfg.rvadapter.RVHolder;
import cbfg.rvadapter.RVHolderFactory;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.GmsBean;
import top.niunaijun.blackboxa.databinding.ItemGmsBinding;

public class GmsAdapter extends RVHolderFactory {
    @Override
    public RVHolder<?> createViewHolder(ViewGroup parent, int viewType, Object item) {
        return new GmsVH(inflate(R.layout.item_gms, parent));
    }

    static class GmsVH extends RVHolder<GmsBean> {
        private final ItemGmsBinding binding;

        GmsVH(View itemView) {
            super(itemView);
            binding = ItemGmsBinding.bind(itemView);
        }

        @Override
        public void setContent(GmsBean item, boolean isSelected, Object payload) {
            binding.tvTitle.setText(item.getUserName());
            binding.checkbox.setChecked(item.isInstalledGms());
            binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    binding.getRoot().performClick();
                }
            });
        }
    }
}
