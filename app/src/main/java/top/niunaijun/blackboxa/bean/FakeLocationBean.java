package top.niunaijun.blackboxa.bean;

import android.graphics.drawable.Drawable;

import top.niunaijun.blackbox.entity.location.BLocation;

public class FakeLocationBean {
    private final int userID;
    private final String name;
    private final Drawable icon;
    private final String packageName;
    private int fakeLocationPattern;
    private BLocation fakeLocation;

    public FakeLocationBean(int userID, String name, Drawable icon, String packageName, int fakeLocationPattern, BLocation fakeLocation) {
        this.userID = userID;
        this.name = name;
        this.icon = icon;
        this.packageName = packageName;
        this.fakeLocationPattern = fakeLocationPattern;
        this.fakeLocation = fakeLocation;
    }

    public int getUserID() { return userID; }
    public String getName() { return name; }
    public Drawable getIcon() { return icon; }
    public String getPackageName() { return packageName; }
    public int getFakeLocationPattern() { return fakeLocationPattern; }
    public void setFakeLocationPattern(int p) { this.fakeLocationPattern = p; }
    public BLocation getFakeLocation() { return fakeLocation; }
    public void setFakeLocation(BLocation loc) { this.fakeLocation = loc; }
}
