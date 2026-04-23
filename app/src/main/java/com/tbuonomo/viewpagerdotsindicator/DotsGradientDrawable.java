package com.tbuonomo.viewpagerdotsindicator;

import android.graphics.drawable.GradientDrawable;

public class DotsGradientDrawable extends GradientDrawable {
  public int currentColor = 0;

  @Override
  public void setColor(int argb) {
    super.setColor(argb);
    currentColor = argb;
  }
}