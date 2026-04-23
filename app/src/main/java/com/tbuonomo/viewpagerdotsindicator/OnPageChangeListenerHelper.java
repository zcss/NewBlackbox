package com.tbuonomo.viewpagerdotsindicator;

public abstract class OnPageChangeListenerHelper {
  private int lastLeftPosition = -1;
  private int lastRightPosition = -1;

  protected abstract int getPageCount();

  public void onPageScrolled(int position, float positionOffset) {
    float offset = position + positionOffset;
    float lastPageIndex = getPageCount() - 1;
    if (offset == lastPageIndex) {
      offset = lastPageIndex - .0001f;
    }
    int leftPosition = (int) offset;
    int rightPosition = leftPosition + 1;

    if (rightPosition > lastPageIndex || leftPosition == -1) {
      return;
    }

    onPageScrolled(leftPosition, rightPosition, offset % 1f);

    if (lastLeftPosition != -1) {
      if (leftPosition > lastLeftPosition) {
        for (int i = lastLeftPosition; i < leftPosition; i++) {
          resetPosition(i);
        }
      }

      if (rightPosition < lastRightPosition) {
        resetPosition(lastRightPosition);
        for (int i = rightPosition + 1; i <= lastRightPosition; i++) {
          resetPosition(i);
        }
      }
    }

    lastLeftPosition = leftPosition;
    lastRightPosition = rightPosition;
  }

  protected abstract void onPageScrolled(int selectedPosition, int nextPosition, float positionOffset);
  protected abstract void resetPosition(int position);
}