package top.niunaijun.blackboxa.view.fake;

import android.view.View;
import android.view.ViewGroup;

import cbfg.rvadapter.RVHolder;
import cbfg.rvadapter.RVHolderFactory;
import top.niunaijun.blackbox.fake.frameworks.BLocationManager;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.FakeLocationBean;
import top.niunaijun.blackboxa.databinding.ItemFakeBinding;
import top.niunaijun.blackboxa.util.ResUtil;

public class FakeLocationAdapter extends RVHolderFactory {
    @Override
    public RVHolder<?> createViewHolder(ViewGroup parent, int viewType, Object item) {
        return new FakeLocationVH(inflate(R.layout.item_fake, parent));
    }

    public static class FakeLocationVH extends RVHolder<FakeLocationBean> {
        private final ItemFakeBinding binding;

        public FakeLocationVH(View itemView) {
            super(itemView);
            binding = ItemFakeBinding.bind(itemView);
        }

        @Override
        public void setContent(FakeLocationBean item, boolean isSelected, Object payload) {
            binding.icon.setImageDrawable(item.getIcon());
            binding.name.setText(item.getName());
            if (item.getFakeLocation() == null || item.getFakeLocationPattern() == BLocationManager.CLOSE_MODE) {
                binding.fakeLocation.setText(ResUtil.getString(R.string.real_location));
            } else {
                binding.fakeLocation.setText(String.format("%f, %f", item.getFakeLocation().getLatitude(), item.getFakeLocation().getLongitude()));
            }
            binding.cornerLabel.setVisibility(View.VISIBLE);
        }
    }
}
