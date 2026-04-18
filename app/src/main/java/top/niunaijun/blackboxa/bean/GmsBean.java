package top.niunaijun.blackboxa.bean;

public class GmsBean {
    private final int userID;
    private final String userName;
    private boolean isInstalledGms;

    public GmsBean(int userID, String userName, boolean isInstalledGms) {
        this.userID = userID;
        this.userName = userName;
        this.isInstalledGms = isInstalledGms;
    }

    public int getUserID() { return userID; }
    public String getUserName() { return userName; }
    public boolean isInstalledGms() { return isInstalledGms; }
    public void setInstalledGms(boolean installed) { this.isInstalledGms = installed; }
}
