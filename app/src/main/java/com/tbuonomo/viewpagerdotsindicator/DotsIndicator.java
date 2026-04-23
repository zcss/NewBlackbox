package com.tbuonomo.viewpagerdotsindicator;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.tbuonomo.viewpagerdotsindicator.BaseDotsIndicator.Type;

import top.niunaijun.blackboxa.R;

public class DotsIndicator extends BaseDotsIndicator {
  public static final float DEFAULT_WIDTH_FACTOR = 2.5f;

  private LinearLayout linearLayout;
  private float dotsWidthFactor;
  private boolean progressMode;
  private float dotsElevation;

  public int selectedDotColor;
  private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

  public DotsIndicator(Context context) { this(context, null); }
  public DotsIndicator(Context context, AttributeSet attrs) { this(context, attrs, 0); }
  public DotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    linearLayout = new LinearLayout(getContext());
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    addView(linearLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    dotsWidthFactor = DEFAULT_WIDTH_FACTOR;

    if (attrs != null) {
      final var a = getContext().obtainStyledAttributes(attrs, R.styleable.DotsIndicator);
      selectedDotColor = a.getColor(R.styleable.DotsIndicator_selectedDotColor, DEFAULT_POINT_COLOR);
      dotsWidthFactor = a.getFloat(R.styleable.DotsIndicator_dotsWidthFactor, 2.5f);
      if (dotsWidthFactor < 1f) dotsWidthFactor = 2.5f;
      progressMode = a.getBoolean(R.styleable.DotsIndicator_progressMode, false);
      dotsElevation = a.getDimension(R.styleable.DotsIndicator_dotsElevation, 0f);
      a.recycle();
    }

    if (isInEditMode()) {
      addDots(5);
      refreshDots();
    }
  }

  @Override public void addDot(int index) {
    View dot = LayoutInflater.from(getContext()).inflate(R.layout.dot_layout, this, false);
    ImageView imageView = dot.findViewById(R.id.dot);
    ViewGroup.LayoutParams params = imageView.getLayoutParams();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      dot.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }

    params.height = (int) dotsSize;
    params.width = params.height;
    if (params instanceof ViewGroup.MarginLayoutParams) {
      ((ViewGroup.MarginLayoutParams) params).setMargins((int) dotsSpacing, 0, (int) dotsSpacing, 0);
    }
    DotsGradientDrawable background = new DotsGradientDrawable();
    background.setCornerRadius(dotsCornerRadius);
    int current = isInEditMode() ? (index == 0 ? selectedDotColor : dotsColor)
        : (pager.getCurrentItem() == index ? selectedDotColor : dotsColor);
    background.setColor(current);
    imageView.setBackground(background);

    dot.setOnClickListener(v -> {
      if (dotsClickable && pager != null && index < pager.getCount()) {
        pager.setCurrentItem(index, true);
      }
    });

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setPaddingHorizontal(dot, (int) (dotsElevation * 0.8f));
      setPaddingVertical(dot, (int) (dotsElevation * 2));
      imageView.setElevation(dotsElevation);
    }

    dots.add(imageView);
    linearLayout.addView(dot);
  }

  @Override public void removeDot(int index) {
    linearLayout.removeViewAt(linearLayout.getChildCount() - 1);
    dots.remove(dots.size() - 1);
  }

  @NonNull @Override public OnPageChangeListenerHelper buildOnPageChangedListener() {
    return new OnPageChangeListenerHelper() {
      @Override protected void onPageScrolled(int selectedPosition, int nextPosition, float positionOffset) {
        ImageView selectedDot = dots.get(selectedPosition);
        int selectedDotWidth = (int) (dotsSize + dotsSize * (dotsWidthFactor - 1f) * (1f - positionOffset));
        setViewWidth(selectedDot, selectedDotWidth);

        if (isInBounds(dots, nextPosition)) {
          ImageView nextDot = dots.get(nextPosition);
          int nextDotWidth = (int) (dotsSize + dotsSize * (dotsWidthFactor - 1f) * positionOffset);
          setViewWidth(nextDot, nextDotWidth);

          DotsGradientDrawable selectedBg = (DotsGradientDrawable) selectedDot.getBackground();
          DotsGradientDrawable nextBg = (DotsGradientDrawable) nextDot.getBackground();

          if (selectedDotColor != dotsColor) {
            int selectedColor = (Integer) argbEvaluator.evaluate(positionOffset, selectedDotColor, dotsColor);
            int nextColor = (Integer) argbEvaluator.evaluate(positionOffset, dotsColor, selectedDotColor);

            nextBg.setColor(nextColor);

            if (progressMode && selectedPosition <= pager.getCurrentItem()) {
              selectedBg.setColor(selectedDotColor);
            } else {
              selectedBg.setColor(selectedColor);
            }
          }
        }
        invalidate();
      }

      @Override protected void resetPosition(int position) {
        setViewWidth(dots.get(position), (int) dotsSize);
        refreshDotColor(position);
      }

      @Override protected int getPageCount() { return dots.size(); }
    };
  }

  @Override public void refreshDotColor(int index) {
    ImageView elevationItem = dots.get(index);
    if (elevationItem.getBackground() instanceof DotsGradientDrawable) {
      DotsGradientDrawable background = (DotsGradientDrawable) elevationItem.getBackground();
      if (index == pager.getCurrentItem() || (progressMode && index < pager.getCurrentItem())) {
        background.setColor(selectedDotColor);
      } else {
        background.setColor(dotsColor);
      }
      elevationItem.setBackground(background);
      elevationItem.invalidate();
    }
  }

  @Override protected Type getType() { return Type.DEFAULT; }

  @Deprecated // Use setSelectedDotColor instead
  public void setSelectedPointColor(int color) { this.selectedDotColor = color; }

  // Java equivalents of extension functions
  private static void setPaddingHorizontal(View v, int padding) {
    v.setPadding(padding, v.getPaddingTop(), padding, v.getPaddingBottom());
  }
  private static void setPaddingVertical(View v, int padding) {
    v.setPadding(v.getPaddingLeft(), padding, v.getPaddingRight(), padding);
  }
}