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
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.tbuonomo.viewpagerdotsindicator.BaseDotsIndicator.Type;

import top.niunaijun.blackboxa.R;

public class SpringDotsIndicator extends BaseDotsIndicator {
  private static final float DEFAULT_DAMPING_RATIO = 0.5f;
  private static final int DEFAULT_STIFFNESS = 300;

  private View dotIndicatorView;

  private float dotsStrokeWidth;
  private int dotsStrokeColor;
  private int dotIndicatorColor;
  private float stiffness;
  private float dampingRatio;

  private final float dotIndicatorSize;
  private SpringAnimation dotIndicatorSpring;
  private final LinearLayout strokeDotsLinearLayout;

  public SpringDotsIndicator(Context context) { this(context, null); }
  public SpringDotsIndicator(Context context, AttributeSet attrs) { this(context, attrs, 0); }
  public SpringDotsIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    float horizontalPadding = dpToPxF(24f);
    setClipToPadding(false);
    setPadding((int) horizontalPadding, 0, (int) horizontalPadding, 0);
    strokeDotsLinearLayout = new LinearLayout(getContext());
    strokeDotsLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
    addView(strokeDotsLinearLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    dotsStrokeWidth = dpToPxF(2f);
    dotIndicatorColor = getThemePrimaryColor(getContext());
    dotsStrokeColor = dotIndicatorColor;
    stiffness = DEFAULT_STIFFNESS;
    dampingRatio = DEFAULT_DAMPING_RATIO;

    if (attrs != null) {
      final var a = getContext().obtainStyledAttributes(attrs, R.styleable.SpringDotsIndicator);
      dotIndicatorColor = a.getColor(R.styleable.SpringDotsIndicator_dotsColor, dotIndicatorColor);
      dotsStrokeColor = a.getColor(R.styleable.SpringDotsIndicator_dotsStrokeColor, dotIndicatorColor);
      stiffness = a.getFloat(R.styleable.SpringDotsIndicator_stiffness, stiffness);
      dampingRatio = a.getFloat(R.styleable.SpringDotsIndicator_dampingRatio, dampingRatio);
      dotsStrokeWidth = a.getDimension(R.styleable.SpringDotsIndicator_dotsStrokeWidth, dotsStrokeWidth);
      a.recycle();
    }

    dotIndicatorSize = dotsSize;

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

    dotIndicatorView = buildDot(false);
    addView(dotIndicatorView);
    dotIndicatorSpring = new SpringAnimation(dotIndicatorView, SpringAnimation.TRANSLATION_X);
    SpringForce springForce = new SpringForce(0f);
    springForce.setDampingRatio(dampingRatio);
    springForce.setStiffness(stiffness);
    dotIndicatorSpring.setSpring(springForce);
  }

  @Override public void addDot(int index) {
    ViewGroup dot = buildDot(true);
    dot.setOnClickListener(v -> {
      if (dotsClickable && pager != null && index < pager.getCount()) {
        pager.setCurrentItem(index, true);
      }
    });

    dots.add((ImageView) dot.findViewById(R.id.spring_dot));
    strokeDotsLinearLayout.addView(dot);
  }

  private ViewGroup buildDot(boolean stroke) {
    ViewGroup dot = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.spring_dot_layout, this, false);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      dot.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }

    ImageView dotView = dot.findViewById(R.id.spring_dot);
    dotView.setBackgroundResource(stroke ? R.drawable.spring_dot_stroke_background : R.drawable.spring_dot_background);
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) dotView.getLayoutParams();
    params.height = (int) (stroke ? dotsSize : dotIndicatorSize);
    params.width = params.height;
    params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
    params.setMargins((int) dotsSpacing, 0, (int) dotsSpacing, 0);

    setUpDotBackground(stroke, dotView);
    return dot;
  }

  private void setUpDotBackground(boolean stroke, View dotView) {
    GradientDrawable dotBackground = (GradientDrawable) ((ImageView) dotView.findViewById(R.id.spring_dot)).getBackground();
    if (stroke) {
      dotBackground.setStroke((int) dotsStrokeWidth, dotsStrokeColor);
    } else {
      dotBackground.setColor(dotIndicatorColor);
    }
    dotBackground.setCornerRadius(dotsCornerRadius);
  }

  @Override public void removeDot(int index) {
    strokeDotsLinearLayout.removeViewAt(strokeDotsLinearLayout.getChildCount() - 1);
    dots.remove(dots.size() - 1);
  }

  @Override public void refreshDotColor(int index) {
    setUpDotBackground(true, dots.get(index));
  }

  @NonNull @Override public OnPageChangeListenerHelper buildOnPageChangedListener() {
    return new OnPageChangeListenerHelper() {
      @Override protected int getPageCount() { return dots.size(); }

      @Override protected void onPageScrolled(int selectedPosition, int nextPosition, float positionOffset) {
        float distance = dotsSize + dotsSpacing * 2f;
        int x = ((ViewGroup) dots.get(selectedPosition).getParent()).getLeft();
        float globalPositionOffsetPixels = x + distance * positionOffset;
        if (dotIndicatorSpring != null) {
          dotIndicatorSpring.animateToFinalPosition(globalPositionOffsetPixels);
        }
      }

      @Override protected void resetPosition(int position) { /* no-op */ }
    };
  }

  @Override protected Type getType() { return Type.SPRING; }

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