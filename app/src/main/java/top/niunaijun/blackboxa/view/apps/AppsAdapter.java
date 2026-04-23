package top.niunaijun.blackboxa.view.apps;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import cbfg.rvadapter.RVHolder;
import cbfg.rvadapter.RVHolderFactory;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.AppInfo;
import top.niunaijun.blackboxa.databinding.ItemAppBinding;

/**
 * 应用图标列表适配器：负责创建并绑定应用项的 ViewHolder。
 */
public class AppsAdapter extends RVHolderFactory {
    private static final String TAG = "AppsAdapter";
    private static final int MAX_ICON_SIZE = 96;
    private static final int DEFAULT_ICON_COLOR = Color.parseColor("#CCCCCC");

    /** 创建 ViewHolder。*/
    @Override
    public RVHolder<?> createViewHolder(ViewGroup parent, int viewType, Object item) {
        try {
            return new AppsVH(inflate(R.layout.item_app, parent));
        } catch (Exception e) {
            Log.e(TAG, "Error creating ViewHolder: " + e.getMessage());
            return new FallbackAppsVH(inflate(R.layout.item_app, parent));
        }
    }

    /** 正常项 ViewHolder。*/
    static class AppsVH extends RVHolder<AppInfo> {
        final ItemAppBinding binding;
        private Drawable currentIcon;
        private boolean isAttached = false;

        AppsVH(@NonNull View itemView) {
            super(itemView);
            binding = ItemAppBinding.bind(itemView);
            try {
                binding.icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override public boolean onPreDraw() {
                        if (isAttached) {
                            itemView.getViewTreeObserver().removeOnPreDrawListener(this);
                        }
                        return true;
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ViewHolder: " + e.getMessage());
            }
        }

        /** 绑定应用名称、图标与角标状态。*/
        @Override
        public void setContent(AppInfo item, boolean isSelected, Object payload) {
            try {
                setIconSafely(item.getIcon(), item.getPackageName());
                binding.name.setText(item.getName() != null ? item.getName() : "Unknown App");
                binding.cornerLabel.setVisibility(item.isXpModule() ? View.VISIBLE : View.INVISIBLE);
                isAttached = true;
            } catch (Exception e) {
                Log.e(TAG, "Error setting content for " + item.getPackageName() + ": " + e.getMessage());
                setSafeDefaults();
            }
        }

        private void setIconSafely(Drawable icon, String packageName) {
            try {
                if (icon != null) {
                    Drawable optimized = optimizeIcon(icon);
                    binding.icon.setImageDrawable(optimized);
                    currentIcon = optimized;
                } else {
                    binding.icon.setImageDrawable(createDefaultIcon());
                    currentIcon = null;
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to set icon for " + packageName + ": " + e.getMessage());
                binding.icon.setImageDrawable(createDefaultIcon());
                currentIcon = null;
            }
        }

        private Drawable optimizeIcon(Drawable icon) {
            try {
                if (icon instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                    if (bitmap != null && (bitmap.getWidth() > MAX_ICON_SIZE || bitmap.getHeight() > MAX_ICON_SIZE)) {
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, MAX_ICON_SIZE, MAX_ICON_SIZE, true);
                        return new BitmapDrawable(itemView.getResources(), scaled);
                    }
                }
                return icon;
            } catch (Exception e) {
                Log.w(TAG, "Error optimizing icon: " + e.getMessage());
                return icon;
            }
        }

        private Drawable createDefaultIcon() {
            try {
                return new ColorDrawable(DEFAULT_ICON_COLOR);
            } catch (Exception e) {
                Log.w(TAG, "Error creating default icon: " + e.getMessage());
                return new ColorDrawable(Color.GRAY);
            }
        }

        private void setSafeDefaults() {
            try {
                binding.icon.setImageDrawable(createDefaultIcon());
                binding.name.setText("Unknown App");
                binding.cornerLabel.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                Log.e(TAG, "Error setting safe defaults: " + e.getMessage());
            }
        }
    }

    /** 兜底项 ViewHolder（创建失败或异常时使用）。*/
    static class FallbackAppsVH extends RVHolder<AppInfo> {
        final ItemAppBinding binding;
        FallbackAppsVH(@NonNull View itemView) {
            super(itemView);
            binding = ItemAppBinding.bind(itemView);
        }
        @Override public void setContent(AppInfo item, boolean isSelected, Object payload) {
            try {
                binding.icon.setImageDrawable(new ColorDrawable(DEFAULT_ICON_COLOR));
                binding.name.setText(item.getName() != null ? item.getName() : "Unknown App");
                binding.cornerLabel.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                Log.e(TAG, "Error in fallback ViewHolder: " + e.getMessage());
            }
        }
    }
}
