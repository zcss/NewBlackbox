package top.niunaijun.blackboxa.bean;

public class GmsInstallBean {
    private final int userID;
    private final boolean success;
    private final String msg;

    public GmsInstallBean(int userID, boolean success, String msg) {
        this.userID = userID;
        this.success = success;
        this.msg = msg;
    }

    public int getUserID() { return userID; }
    public boolean isSuccess() { return success; }
    public String getMsg() { return msg; }

    public boolean getSuccess() {
        return success;
    }
}
