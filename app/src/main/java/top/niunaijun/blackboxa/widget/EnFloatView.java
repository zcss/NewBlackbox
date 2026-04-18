package top.niunaijun.blackboxa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.imuxuan.floatingview.FloatingMagnetView;

import top.niunaijun.blackboxa.R;

public class EnFloatView extends FloatingMagnetView {
    private static final String TAG = "RockerManager";

    public interface LocationListener { void onLocation(float angle, float distance); }

    private RockerView rockerView;
    private LocationListener mListener;

    public EnFloatView(Context context) {
        super(context);
        init(context);
    }

    public EnFloatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EnFloatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context mContext) {
        inflate(mContext, R.layout.view_float_rocker, this);
        initRockerView();
    }

    private void initRockerView() {
        rockerView = findViewById(R.id.rocker);
        if (rockerView != null) {
            rockerView.setListener((type, currentAngle, currentDistance) -> {
                if (type == RockerView.EVENT_CLOCK && currentAngle != -1F) {
                    float realAngle = currentAngle;
                    float realDistance = currentDistance * 0.001F;
                    if (mListener != null) mListener.onLocation(realAngle, realDistance);
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (rockerView != null) rockerView.setCanMove(false);
        } else if (event != null && event.getAction() == MotionEvent.ACTION_UP) {
            if (rockerView != null) rockerView.setCanMove(true);
        }
        return super.onTouchEvent(event);
    }

    public void setListener(LocationListener listener) { this.mListener = listener; }
}
