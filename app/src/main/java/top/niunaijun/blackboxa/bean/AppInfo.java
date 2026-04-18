package top.niunaijun.blackboxa.bean;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private final String name;
    private final Drawable icon;
    private final String packageName;
    private final String sourceDir;
    private final boolean isXpModule;

    public AppInfo(String name, Drawable icon, String packageName, String sourceDir, boolean isXpModule) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
        this.sourceDir = sourceDir;
        this.isXpModule = isXpModule;
    }

    public String getName() { return name; }
    public Drawable getIcon() { return icon; }
    public String getPackageName() { return packageName; }
    public String getSourceDir() { return sourceDir; }
    public boolean isXpModule() { return isXpModule; }
}
