package top.niunaijun.blackboxa.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

/**
 * 分辨率与显示度量工具：屏幕尺寸、单位换算、输入法显隐等。
 */
public class Resolution {
    private static final String TAG = "UtilsScreen";

    
    /** 获取屏幕宽度（像素）。*/
    public static int getScreenWidth(Context context) {
        return getScreenSize(context, null).x;
    }

    
    /** 获取屏幕高度（像素）。*/
    public static int getScreenHeight(Context context) {
        return getScreenSize(context, null).y;
    }

    
    @SuppressLint("NewApi")
    /** 获取屏幕尺寸（像素），兼容不同 API。*/
    public static Point getScreenSize(Context context, Point outSize) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point ret = outSize == null ? new Point() : outSize;
        
        if (Build.VERSION.SDK_INT >= 30) {
            
            android.view.WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
            android.graphics.Rect bounds = windowMetrics.getBounds();
            ret.x = bounds.width();
            ret.y = bounds.height();
        } else if (Build.VERSION.SDK_INT >= 13) {
            
            @SuppressWarnings("deprecation")
            final Display defaultDisplay = wm.getDefaultDisplay();
            defaultDisplay.getSize(ret);
        } else {
            
            @SuppressWarnings("deprecation")
            final Display defaultDisplay = wm.getDefaultDisplay();
            ret.x = defaultDisplay.getWidth();
            ret.y = defaultDisplay.getHeight();
        }
        return ret;
    }

    
    /** dp 转 px。*/
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    
    /** px 转 dp。*/
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    

    
    /** 获取屏幕 density。*/
    public static float getDensity(Context context) {
        float density = 0f;
        if (context== null) {
            return density;
        }
        try {
            density = context.getResources().getDisplayMetrics().density;
        } catch (Exception e) {

        }
        return density;
    }

    
    /** 校验当前屏幕像素是否为指定分辨率。*/
    public static boolean checkPix(Activity context, int width, int height) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            return metrics.widthPixels == width && metrics.heightPixels == height;
        } else {
            return getScreenPixWidth(context) == width && getScreenPixHeight(context) == height;
        }
    }

    
    /** 获取屏幕像素宽度。*/
    public static int getScreenPixWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    
    /** 获取屏幕像素高度。*/
    public static int getScreenPixHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    
    /** dp 转 px（整型）。*/
    public static int dipToPx(Context context, int dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    
    /** px 转 dp（整型）。*/
    public static int pxToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    
    /** sp 转 px（整型）。*/
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    
    /** 隐藏输入法。*/
    public static void hideInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    
    /** 显示输入法。*/
    public static void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    
    /** 延迟显示输入法。*/
    public static void showInputMethod(final View view, long delayMillis) {
        
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Resolution.showInputMethod(view);

            }
        }, delayMillis);
    }

    
    /** 判断是否处于非锁屏状态。*/
    public static boolean isScreenLocked(Context c) {
        KeyguardManager mKeyguardManager = (KeyguardManager) c
                .getSystemService(Context.KEYGUARD_SERVICE);
        boolean bResult = !mKeyguardManager.inKeyguardRestrictedInputMode();

        return bResult;
    }

    /** 获取状态栏高度（像素）。*/
    public static int getBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    
    
    /** 获取导航栏尺寸（像素），宽为屏幕宽，高手动计算差值。*/
    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getScreenSize(context, null);
        Point realScreenSize = getRealScreenSize(context);






        
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        
        return new Point();
    }


    /** 获取真实屏幕尺寸（包含系统栏，兼容不同 API）。*/
    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 30) {
            
            android.view.WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            android.graphics.Rect bounds = windowMetrics.getBounds();
            size.x = bounds.width();
            size.y = bounds.height();
        } else if (Build.VERSION.SDK_INT >= 17) {
            
            @SuppressWarnings("deprecation")
            Display display = windowManager.getDefaultDisplay();
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            
            @SuppressWarnings("deprecation")
            Display display = windowManager.getDefaultDisplay();
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
            }
        }

        return size;
    }
}
