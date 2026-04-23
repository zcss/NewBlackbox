package top.niunaijun.blackboxa.util;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * 数学与坐标工具：距离、夹角、弧度/角度转换等。
 */
public class MathUtil {

    public MathUtil() {
    }

    
    /** 计算两点间直线距离（取整）。*/
    public static int getDistance(PointF A, PointF B) {
        return (int) Math.sqrt(Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2));
    }

    
    /** 计算两点坐标（浮点）间直线距离（取整）。*/
    public static int getDistance(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    
    /** 从 A 指向 B 的方向上，截取指定长度后的点坐标。*/
    public static Point getPointByCutLength(Point A, Point B, int cutLength) {
        float radian = getRadian(A, B);
        return new Point(A.x + (int) (cutLength * Math.cos(radian)), A.y + (int) (cutLength * Math.sin(radian)));
    }

    
    /** 计算 A 指向 B 的弧度值。*/
    public static float getRadian(Point A, Point B) {
        float lenA = B.x - A.x;
        float lenB = B.y - A.y;
        float lenC = (float) Math.sqrt(lenA * lenA + lenB * lenB);
        float radian = (float) Math.acos(lenA / lenC);
        radian = radian * (B.y < A.y ? -1 : 1);
        return radian;
    }


    
    /** 角度转弧度。*/
    public static double angle2Radian(double angle) {
        return angle / 180 * Math.PI;
    }

    
    /** 弧度转角度。*/
    public static double radian2Angle(double radian) {
        return radian / Math.PI * 180;
    }
}