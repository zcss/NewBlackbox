package top.niunaijun.blackbox.utils;

import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 事务限流：记录失败次数的滑动窗口，超过阈值在短期内节流，避免雪崩重试。
 */
public class TransactionThrottler {
    private static final String TAG = "TransactionThrottler";

    private static final int MAX_FAILURES_PER_WINDOW = 50; // 窗口内最大失败次数
    private static final long WINDOW_DURATION_MS = 15000; // 统计窗口（毫秒）
    private static final long THROTTLE_DURATION_MS = 1000; // 触发后至少节流时长

    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong windowStartTime = new AtomicLong(0);
    private final AtomicLong lastThrottleTime = new AtomicLong(0);

    /** 是否应节流（基于窗口统计与上次节流时间）。 */
    public boolean shouldThrottle() {
        long currentTime = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        if (currentTime - windowStart > WINDOW_DURATION_MS) {
            failureCount.set(0);
            windowStartTime.set(currentTime);
            return false;
        }
        long lastThrottle = lastThrottleTime.get();
        if (currentTime - lastThrottle < THROTTLE_DURATION_MS) {
            return true;
        }
        if (failureCount.get() >= MAX_FAILURES_PER_WINDOW) {
            lastThrottleTime.set(currentTime);
            Log.w(TAG, "Throttling transactions due to " + failureCount.get() + " failures in window");
            return true;
        }
        return false;
    }

    /** 记录一次失败，维持窗口统计。 */
    public void recordFailure() {
        long currentTime = System.currentTimeMillis();
        long windowStart = windowStartTime.get();
        if (windowStart == 0) {
            windowStartTime.set(currentTime);
        }
        if (currentTime - windowStart > WINDOW_DURATION_MS) {
            failureCount.set(1);
            windowStartTime.set(currentTime);
        } else {
            failureCount.incrementAndGet();
        }
        Log.d(TAG, "Recorded transaction failure, count: " + failureCount.get());
    }

    /** 清空统计。 */
    public void reset() {
        failureCount.set(0);
        windowStartTime.set(0);
        lastThrottleTime.set(0);
        Log.d(TAG, "Reset transaction throttler");
    }

    public int getFailureCount() {
        return failureCount.get();
    }
}
