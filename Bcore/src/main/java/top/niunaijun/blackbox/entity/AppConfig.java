package top.niunaijun.blackbox.entity;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;



/**
 * 应用进程配置：用于跨进程序列化传递沙盒进程的关键标识信息。
 */
public class AppConfig implements Parcelable {
    public static final String KEY = "BlackBox_client_config";

    /** 目标包名 */
    public String packageName;
    /** 进程名（可能为:remote等） */
    public String processName;
    /** BlackBox进程ID（虚拟PID） */
    public int bpid;
    /** BlackBox UID（含user隔离的虚拟UID） */
    public int buid;
    /** 宿主真实UID */
    public int uid;
    /** 虚拟用户ID */
    public int userId;
    /** 调用方BlackBox UID（跨进程时记录来源） */
    public int callingBUid;
    /** 进程与服务绑定令牌 */
    public IBinder token;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.processName);
        dest.writeInt(this.bpid);
        dest.writeInt(this.buid);
        dest.writeInt(this.uid);
        dest.writeInt(this.userId);
        dest.writeInt(this.callingBUid);
        dest.writeStrongBinder(token);
    }

    public AppConfig() {
    }

    protected AppConfig(Parcel in) {
        this.packageName = in.readString();
        this.processName = in.readString();
        this.bpid = in.readInt();
        this.buid = in.readInt();
        this.uid = in.readInt();
        this.userId = in.readInt();
        this.callingBUid = in.readInt();
        this.token = in.readStrongBinder();
    }

    public static final Parcelable.Creator<AppConfig> CREATOR = new Parcelable.Creator<AppConfig>() {
        @Override
        public AppConfig createFromParcel(Parcel source) {
            return new AppConfig(source);
        }

        @Override
        public AppConfig[] newArray(int size) {
            return new AppConfig[size];
        }
    };
}
