package com.tbuonomo.viewpagerdotsindicator;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.tbuonomo.viewpagerdotsindicator.BaseDotsIndicator.Type;

import top.niunaijun.blackboxa.R;

public class WormDotsIndicator extends BaseDotsIndicator {
  private ImageView dotIndicatorView;
  private View dotIndicatorLayout;

  private int dotsStrokeWidth;
  private int dotIndicatorColor;
  private int dotsStrokeColor;

  private SpringAnimation dotIndicatorXSpring;
  private SpringAnimation dotIndicatorWidthSpring;
  private final LinearLayout strokeDotsLinearLayout;

  public WormDotsIndicator(Context context) { this(context, null); }
  public WormDotsIndicator(Context context, AttributeSet attrs) { this(context, attrs, 0); }
  public WormDotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    LayoutParams linearParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    int horizontalPadding = dpToPx(24);
    setPadding(horizontalPadding, 0, horizontalPadding, 0);
    setClipToPadding(false);
    strokeDotsLinearLayout = new LinearLayout(getContext());
    strokeDotsLinearLayout.setLayoutParams(linearParams);
    strokeDotsLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
    addView(strokeDotsLinearLayout);

    dotsStrokeWidth = dpToPx(2);
    dotIndicatorColor = getThemePrimaryColor(getContext());
    dotsStrokeColor = dotIndicatorColor;

    if (attrs != null) {
      final var a = getContext().obtainStyledAttributes(attrs, R.styleable.WormDotsIndicator);
      dotIndicatorColor = a.getColor(R.styleable.WormDotsIndicator_dotsColor, dotIndicatorColor);
      dotsStrokeColor = a.getColor(R.styleable.WormDotsIndicator_dotsStrokeColor, dotIndicatorColor);
      dotsStrokeWidth = (int) a.getDimension(R.styleable.WormDotsIndicator_dotsStrokeWidth, dotsStrokeWidth);
      a.recycle();
    }

    if (isInEditMode()) {
      addDots(5);
      addView(buildDot(false));
    }

    setUpDotIndicator();
  }

  private void setUpDotIndicator() {
    if (pager != null && pager.isEmpty()) return;

    if (dotIndicatorView != null && indexOfChild(dotIndicatorView) != -1) {
      removeView(dotIndicatorView);
    }

    dotIndicatorLayout = buildDot(false);
    dotIndicatorView = dotIndicatorLayout.findViewById(R.id.worm_dot);
    addView(dotIndicatorLayout);
    dotIndicatorXSpring = new SpringAnimation(dotIndicatorLayout, SpringAnimation.TRANSLATION_X);
    SpringForce springForceX = new SpringForce(0f);
    springForceX.setDampingRatio(1f);
    springForceX.setStiffness(300f);
    dotIndicatorXSpring.setSpring(springForceX);

    FloatPropertyCompat<View> floatPropertyCompat = new FloatPropertyCompat<View>("DotsWidth") {
      @Override public float getValue(View view) { return dotIndicatorView.getLayoutParams().width; }
      @Override public void setValue(View view, float value) {
        ViewGroup.LayoutParams params = dotIndicatorView.getLayoutParams();
        params.width = (int) value;
        dotIndicatorView.requestLayout();
      }
    };
    dotIndicatorWidthSpring = new SpringAnimation(dotIndicatorLayout, floatPropertyCompat);
    SpringForce springForceWidth = new SpringForce(0f);
    springForceWidth.setDampingRatio(1f);
    springForceWidth.setStiffness(300f);
    dotIndicatorWidthSpring.setSpring(springForceWidth);
  }

  @Override public void addDot(int index) {
    ViewGroup dot = buildDot(true);
    dot.setOnClickListener(v -> {
      if (dotsClickable && pager != null && index < pager.getCount()) {
        pager.setCurrentItem(index, true);
      }
    });

    dots.add((ImageView) dot.findViewById(R.id.worm_dot));
    strokeDotsLinearLayout.addView(dot);
  }

  private ViewGroup buildDot(boolean stroke) {
    ViewGroup dot = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.worm_dot_layout, this, false);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      dot.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }
    View dotImageView = dot.findViewById(R.id.worm_dot);
    dotImageView.setBackgroundResource(stroke ? R.drawable.worm_dot_stroke_background : R.drawable.worm_dot_background);
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dotImageView.getLayoutParams();
    params.height = (int) dotsSize;
    params.width = params.height;
    params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
    params.setMargins((int) dotsSpacing, 0, (int) dotsSpacing, 0);

    setUpDotBackground(stroke, dotImageView);
    return dot;
  }

  private void setUpDotBackground(boolean stroke, View dotImageView) {
    GradientDrawable dotBackground = (GradientDrawable) dotImageView.getBackground();
    if (stroke) {
      dotBackground.setStroke(dotsStrokeWidth, dotsStrokeColor);
    } else {
      dotBackground.setColor(dotIndicatorColor);
    }
    dotBackground.setCornerRadius(dotsCornerRadius);
  }

  @Override public void refreshDotColor(int index) {
    setUpDotBackground(true, dots.get(index));
  }

  @Override public void removeDot(int index) {
    strokeDotsLinearLayout.removeViewAt(strokeDotsLinearLayout.getChildCount() - 1);
    dots.remove(dots.size() - 1);
  }

  @NonNull @Override public OnPageChangeListenerHelper buildOnPageChangedListener() {
    return new OnPageChangeListenerHelper() {
      @Override protected int getPageCount() { return dots.size(); }

      @Override protected void onPageScrolled(int selectedPosition, int nextPosition, float positionOffset) {
        float x = ((ViewGroup) dots.get(selectedPosition).getParent()).getLeft();
        float nextX = ((ViewGroup) dots.get(nextPosition == -1 ? selectedPosition : nextPosition).getParent()).getLeft();
        float xFinalPosition;
        float widthFinalPosition;
        if (positionOffset <= 0.1f) {
          xFinalPosition = x;
          widthFinalPosition = dotsSize;
        } else if (positionOffset < 0.9f) {
          xFinalPosition = x;
          widthFinalPosition = nextX - x + dotsSize;
        } else {
          xFinalPosition = nextX;
          widthFinalPosition = dotsSize;
        }
        if (dotIndicatorXSpring != null) dotIndicatorXSpring.animateToFinalPosition(xFinalPosition);
        if (dotIndicatorWidthSpring != null) dotIndicatorWidthSpring.animateToFinalPosition(widthFinalPosition);
      }

      @Override protected void resetPosition(int position) { /* no-op */ }
    };
  }

  @Override protected Type getType() { return Type.WORM; }

  public void setDotIndicatorColor(int color) {
    if (dotIndicatorView != null) {
      dotIndicatorColor = color;
      setUpDotBackground(false, dotIndicatorView);
    }
  }

  public void setStrokeDotsIndicatorColor(int color) {
    dotsStrokeColor = color;
    for (View v : dots) setUpDotBackground(true, v);
  }
}