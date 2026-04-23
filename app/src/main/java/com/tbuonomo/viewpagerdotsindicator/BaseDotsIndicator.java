package com.tbuonomo.viewpagerdotsindicator;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import top.niunaijun.blackboxa.R;

public abstract class BaseDotsIndicator extends FrameLayout {
  public static final int DEFAULT_POINT_COLOR = Color.CYAN;

  public enum Type {
    DEFAULT(16f,
        8f,
        R.styleable.SpringDotsIndicator,
        R.styleable.SpringDotsIndicator_dotsColor,
        R.styleable.SpringDotsIndicator_dotsSize,
        R.styleable.SpringDotsIndicator_dotsSpacing,
        R.styleable.SpringDotsIndicator_dotsCornerRadius,
        R.styleable.SpringDotsIndicator_dotsClickable),
    SPRING(16f,
        4f,
        R.styleable.DotsIndicator,
        R.styleable.DotsIndicator_dotsColor,
        R.styleable.DotsIndicator_dotsSize,
        R.styleable.DotsIndicator_dotsSpacing,
        R.styleable.DotsIndicator_dotsCornerRadius,
        R.styleable.SpringDotsIndicator_dotsClickable),
    WORM(16f,
        4f,
        R.styleable.WormDotsIndicator,
        R.styleable.WormDotsIndicator_dotsColor,
        R.styleable.WormDotsIndicator_dotsSize,
        R.styleable.WormDotsIndicator_dotsSpacing,
        R.styleable.WormDotsIndicator_dotsCornerRadius,
        R.styleable.SpringDotsIndicator_dotsClickable);

    final float defaultSize;
    final float defaultSpacing;
    final int[] styleableId;
    final int dotsColorId;
    final int dotsSizeId;
    final int dotsSpacingId;
    final int dotsCornerRadiusId;
    final int dotsClickableId;

    Type(float defaultSize, float defaultSpacing, int[] styleableId, int dotsColorId,
         int dotsSizeId, int dotsSpacingId, int dotsCornerRadiusId, int dotsClickableId) {
      this.defaultSize = defaultSize;
      this.defaultSpacing = defaultSpacing;
      this.styleableId = styleableId;
      this.dotsColorId = dotsColorId;
      this.dotsSizeId = dotsSizeId;
      this.dotsSpacingId = dotsSpacingId;
      this.dotsCornerRadiusId = dotsCornerRadiusId;
      this.dotsClickableId = dotsClickableId;
    }
  }

  protected final List<ImageView> dots = new ArrayList<>();

  protected boolean dotsClickable = true;
  protected int dotsColor = DEFAULT_POINT_COLOR;
  protected float dotsSize;
  protected float dotsCornerRadius;
  protected float dotsSpacing;

  public BaseDotsIndicator(Context context) {
    this(context, null);
  }

  public BaseDotsIndicator(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BaseDotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    // Defaults from type
    dotsSize = dpToPxF(getType().defaultSize);
    dotsCornerRadius = dotsSize / 2f;
    dotsSpacing = dpToPxF(getType().defaultSpacing);

    if (attrs != null) {
      final var a = context.obtainStyledAttributes(attrs, getType().styleableId);

      dotsColor = a.getColor(getType().dotsColorId, DEFAULT_POINT_COLOR);
      dotsSize = a.getDimension(getType().dotsSizeId, dotsSize);
      dotsCornerRadius = a.getDimension(getType().dotsCornerRadiusId, dotsCornerRadius);
      dotsSpacing = a.getDimension(getType().dotsSpacingId, dotsSpacing);
      dotsClickable = a.getBoolean(getType().dotsClickableId, true);

      a.recycle();
    }
  }

  protected Pager pager = null;

  public interface Pager {
    boolean isNotEmpty();
    int getCurrentItem();
    boolean isEmpty();
    int getCount();
    void setCurrentItem(int item, boolean smoothScroll);
    void removeOnPageChangeListener();
    void addOnPageChangeListener(@NonNull OnPageChangeListenerHelper helper);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    refreshDots();
  }

  private void refreshDotsCount() {
    if (pager == null) return;
    if (dots.size() < pager.getCount()) {
      addDots(pager.getCount() - dots.size());
    } else if (dots.size() > pager.getCount()) {
      removeDots(dots.size() - pager.getCount());
    }
  }

  protected void refreshDotsColors() {
    for (int i = 0; i < dots.size(); i++) {
      refreshDotColor(i);
    }
  }

  protected int dpToPx(int dp) {
    return (int) (getContext().getResources().getDisplayMetrics().density * dp);
  }

  protected float dpToPxF(float dp) {
    return getContext().getResources().getDisplayMetrics().density * dp;
  }

  protected void addDots(int count) {
    for (int i = 0; i < count; i++) {
      addDot(i);
    }
  }

  private void removeDots(int count) {
    for (int i = 0; i < count; i++) {
      removeDot(i);
    }
  }

  public void refreshDots() {
    if (pager == null) return;
    post(() -> {
      refreshDotsCount();
      refreshDotsColors();
      refreshDotsSize();
      refreshOnPageChangedListener();
    });
  }

  private void refreshOnPageChangedListener() {
    if (pager != null && pager.isNotEmpty()) {
      pager.removeOnPageChangeListener();
      OnPageChangeListenerHelper helper = buildOnPageChangedListener();
      pager.addOnPageChangeListener(helper);
      helper.onPageScrolled(pager.getCurrentItem(), 0f);
    }
  }

  private void refreshDotsSize() {
    if (pager == null) return;
    for (int i = 0; i < pager.getCurrentItem() && i < dots.size(); i++) {
      setViewWidth(dots.get(i), (int) dotsSize);
    }
  }

  protected void setViewWidth(@NonNull View v, int width) {
    android.view.ViewGroup.LayoutParams lp = v.getLayoutParams();
    if (lp != null) {
      lp.width = width;
      v.requestLayout();
    }
  }

  protected static boolean isInBounds(@NonNull List<?> list, int index) {
    return index >= 0 && index < list.size();
  }

  protected static int getThemePrimaryColor(@NonNull Context context) {
    TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, value, true);
    return value.data;
  }

  public abstract void refreshDotColor(int index);
  public abstract void addDot(int index);
  public abstract void removeDot(int index);
  @NonNull public abstract OnPageChangeListenerHelper buildOnPageChangedListener();
  @NonNull protected abstract Type getType();

  @Deprecated // Use setDotsColor instead
  public void setPointsColor(int color) {
    this.dotsColor = color;
    refreshDotsColors();
  }

  public void setViewPager(@NonNull ViewPager viewPager) {
    if (viewPager.getAdapter() == null) {
      throw new IllegalStateException("You have to set an adapter to the view pager before initializing the dots indicator !");
    }

    viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
      @Override public void onChanged() {
        super.onChanged();
        refreshDots();
      }
    });

    pager = new Pager() {
      ViewPager.OnPageChangeListener onPageChangeListener = null;

      @Override public boolean isNotEmpty() {
        return viewPager.getAdapter() != null && viewPager.getAdapter().getCount() > 0;
      }

      @Override public int getCurrentItem() { return viewPager.getCurrentItem(); }

      @Override public boolean isEmpty() {
        return viewPager.getAdapter() != null && viewPager.getAdapter().getCount() == 0;
      }

      @Override public int getCount() { return viewPager.getAdapter() != null ? viewPager.getAdapter().getCount() : 0; }

      @Override public void setCurrentItem(int item, boolean smoothScroll) {
        viewPager.setCurrentItem(item, smoothScroll);
      }

      @Override public void removeOnPageChangeListener() {
        if (onPageChangeListener != null) viewPager.removeOnPageChangeListener(onPageChangeListener);
      }

      @Override public void addOnPageChangeListener(@NonNull OnPageChangeListenerHelper helper) {
        onPageChangeListener = new ViewPager.OnPageChangeListener() {
          @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            helper.onPageScrolled(position, positionOffset);
          }
          @Override public void onPageScrollStateChanged(int state) {}
          @Override public void onPageSelected(int position) {}
        };
        viewPager.addOnPageChangeListener(onPageChangeListener);
      }
    };

    refreshDots();
  }

  public void setViewPager2(@NonNull ViewPager2 viewPager2) {
    if (viewPager2.getAdapter() == null) {
      throw new IllegalStateException("You have to set an adapter to the view pager before initializing the dots indicator !");
    }

    viewPager2.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override public void onChanged() {
        super.onChanged();
        refreshDots();
      }
    });

    pager = new Pager() {
      ViewPager2.OnPageChangeCallback onPageChangeCallback = null;

      @Override public boolean isNotEmpty() {
        return viewPager2.getAdapter() != null && viewPager2.getAdapter().getItemCount() > 0;
      }

      @Override public int getCurrentItem() { return viewPager2.getCurrentItem(); }

      @Override public boolean isEmpty() {
        return viewPager2.getAdapter() != null && viewPager2.getAdapter().getItemCount() == 0;
      }

      @Override public int getCount() { return viewPager2.getAdapter() != null ? viewPager2.getAdapter().getItemCount() : 0; }

      @Override public void setCurrentItem(int item, boolean smoothScroll) {
        viewPager2.setCurrentItem(item, smoothScroll);
      }

      @Override public void removeOnPageChangeListener() {
        if (onPageChangeCallback != null) viewPager2.unregisterOnPageChangeCallback(onPageChangeCallback);
      }

      @Override public void addOnPageChangeListener(@NonNull OnPageChangeListenerHelper helper) {
        onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
          @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            helper.onPageScrolled(position, positionOffset);
          }
        };
        viewPager2.registerOnPageChangeCallback(onPageChangeCallback);
      }
    };

    refreshDots();
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
      setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
      setRotation(180f);
      requestLayout();
    }
  }
}