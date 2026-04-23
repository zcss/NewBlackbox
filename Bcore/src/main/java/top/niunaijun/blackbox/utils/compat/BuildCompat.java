package top.niunaijun.blackbox.utils.compat;

import android.os.Build;

/**
 * 构建版本与 ROM 兼容判断：提供预览 SDK 获取、各主版本判断、主流 ROM 识别。
 */
public class BuildCompat {

    /** 获取 PREVIEW_SDK_INT，低版本上返回 0。 */
    public static int getPreviewSDKInt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return Build.VERSION.PREVIEW_SDK_INT;
            } catch (Throwable e) {

            }
        }
        return 0;
    }

    /** Android U 开发者预览或正式版。 */
    public static boolean isU() {
        return Build.VERSION.SDK_INT >= 33 || (Build.VERSION.SDK_INT >= 32 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 13（Tiramisu）或预览。 */
    public static boolean isTiramisu() {
        return Build.VERSION.SDK_INT >= 32 || (Build.VERSION.SDK_INT >= 31 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 12（S）或预览。 */
    public static boolean isS() {
        return Build.VERSION.SDK_INT >= 31 || (Build.VERSION.SDK_INT >= 30 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 11（R）或预览。 */
    public static boolean isR() {
        return Build.VERSION.SDK_INT >= 30 || (Build.VERSION.SDK_INT >= 29 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 10（Q）或预览。 */
    public static boolean isQ() {
        return Build.VERSION.SDK_INT >= 29 || (Build.VERSION.SDK_INT >= 28 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 9（Pie）或预览。 */
    public static boolean isPie() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || (Build.VERSION.SDK_INT >= 27 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 8（Oreo）或预览。 */
    public static boolean isOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || (Build.VERSION.SDK_INT >= 25 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 7.1（N MR1）或预览。 */
    public static boolean isN_MR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 || (Build.VERSION.SDK_INT >= 24 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 7.0（N）或预览。 */
    public static boolean isN() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || (Build.VERSION.SDK_INT >= 23 && Build.VERSION.PREVIEW_SDK_INT == 1);
    }

    /** Android 6.0（M）。 */
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /** Android 5.0（L）。 */
    public static boolean isL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /** 是否三星 ROM。 */
    public static boolean isSamsung() {
        return "samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER);
    }

    /** 是否华为 EMUI。 */
    public static boolean isEMUI() {
        if (Build.DISPLAY.toUpperCase().startsWith("EMUI")) {
            return true;
        }
        String property = SystemPropertiesCompat.get("ro.build.version.emui");
        return property != null && property.contains("EmotionUI");
    }

    /** 是否小米 MIUI。 */
    public static boolean isMIUI() {
        return SystemPropertiesCompat.getInt("ro.miui.ui.version.code", 0) > 0;
    }

    /** 是否魅族 Flyme。 */
    public static boolean isFlyme() {
        return Build.DISPLAY.toLowerCase().contains("flyme");
    }

    /** 是否 OPPO ColorOS。 */
    public static boolean isColorOS() {
        return SystemPropertiesCompat.isExist("ro.build.version.opporom")
                || SystemPropertiesCompat.isExist("ro.rom.different.version");
    }

    /** 是否 360 UI。 */
    public static boolean is360UI() {
        String property = SystemPropertiesCompat.get("ro.build.uiversion");
        return property != null && property.toUpperCase().contains("360UI");
    }

    /** 是否乐视 ROM。 */
    public static boolean isLetv() {
        return Build.MANUFACTURER.equalsIgnoreCase("Letv");
    }

    /** 是否 vivo ROM。 */
    public static boolean isVivo() {
        return SystemPropertiesCompat.isExist("ro.vivo.os.build.display.id");
    }

    private static ROMType sRomType;

    /** 返回 ROM 类型（缓存）。 */
    public static ROMType getROMType() {
        if (sRomType == null) {
            if (isEMUI()) {
                sRomType = ROMType.EMUI;
            } else if (isMIUI()) {
                sRomType = ROMType.MIUI;
            } else if (isFlyme()) {
                sRomType = ROMType.FLYME;
            } else if (isColorOS()) {
                sRomType = ROMType.COLOR_OS;
            } else if (is360UI()) {
                sRomType = ROMType._360;
            } else if (isLetv()) {
                sRomType = ROMType.LETV;
            } else if (isVivo()) {
                sRomType = ROMType.VIVO;
            } else if (isSamsung()) {
                sRomType = ROMType.SAMSUNG;
            } else {
                sRomType = ROMType.OTHER;
            }
        }
        return sRomType;
    }

    /** 主流 ROM 类型枚举。 */
    public enum ROMType {
        EMUI,
        MIUI,
        FLYME,
        COLOR_OS,
        LETV,
        VIVO,
        _360,
        SAMSUNG,
        OTHER
    }
}
