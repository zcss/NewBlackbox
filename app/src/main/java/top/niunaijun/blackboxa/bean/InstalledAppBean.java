package top.niunaijun.blackboxa.bean;

import android.graphics.drawable.Drawable;

public class InstalledAppBean {
    private final String name;
    private final Drawable icon;
    private final String packageName;
    private final String sourceDir;
    private final boolean isInstall;

    public InstalledAppBean(String name, Drawable icon, String packageName, String sourceDir, boolean isInstall) {
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
        this.sourceDir = sourceDir;
        this.isInstall = isInstall;
    }

    public String getName() { return name; }
    public Drawable getIcon() { return icon; }
    public String getPackageName() { return packageName; }
    public String getSourceDir() { return sourceDir; }
    public boolean isInstall() { return isInstall; }
}
