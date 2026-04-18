package top.niunaijun.blackboxa.bean;

import android.graphics.drawable.Drawable;

public class XpModuleInfo {
    private final String name;
    private final String desc;
    private final String packageName;
    private final String version;
    private boolean enable;
    private final Drawable icon;

    public XpModuleInfo(String name, String desc, String packageName, String version, boolean enable, Drawable icon) {
        this.name = name;
        this.desc = desc;
        this.packageName = packageName;
        this.version = version;
        this.enable = enable;
        this.icon = icon;
    }

    public String getName() { return name; }
    public String getDesc() { return desc; }
    public String getPackageName() { return packageName; }
    public String getVersion() { return version; }
    public boolean isEnable() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }
    public Drawable getIcon() { return icon; }
}
