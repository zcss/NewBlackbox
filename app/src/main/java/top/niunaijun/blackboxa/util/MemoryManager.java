package top.niunaijun.blackboxa.util;

import android.util.Log;

/**
 * 内存管理工具：用于监控内存使用并在必要时触发 GC。
 */
public final class MemoryManager {
    private static final String TAG = "MemoryManager";
    private static final double MEMORY_THRESHOLD = 0.8;   // 80%
    private static final double CRITICAL_MEMORY_THRESHOLD = 0.9; // 90%

    private MemoryManager() {}

    /**
     * 判断当前内存是否低于安全阈值。
     * @return true 表示内存使用率低于 80%
     */
    public static boolean isMemorySafe() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsage = (double) usedMemory / (double) maxMemory;
            return memoryUsage < MEMORY_THRESHOLD;
        } catch (Exception e) {
            Log.e(TAG, "Error checking memory: " + e.getMessage());
            return true; // fail open
        }
    }

    /**
     * 判断当前内存是否超过危险阈值。
     * @return true 表示内存使用率高于 90%
     */
    public static boolean isMemoryCritical() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsage = (double) usedMemory / (double) maxMemory;
            return memoryUsage > CRITICAL_MEMORY_THRESHOLD;
        } catch (Exception e) {
            Log.e(TAG, "Error checking critical memory: " + e.getMessage());
            return false; // conservative
        }
    }

    /**
     * 获取当前内存使用百分比（0-100）。
     */
    public static int getMemoryUsagePercentage() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsage = (double) usedMemory / (double) maxMemory;
            return (int) (memoryUsage * 100);
        } catch (Exception e) {
            Log.e(TAG, "Error getting memory usage: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 若内存达到危险阈值则尝试触发一次 GC。
     * @return 是否触发了 GC
     */
    public static boolean forceGarbageCollectionIfNeeded() {
        try {
            if (isMemoryCritical()) {
                Log.w(TAG, "Memory usage critical (" + getMemoryUsagePercentage() + "%), forcing garbage collection");
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during garbage collection: " + e.getMessage());
            return false;
        }
    }

    /**
     * 针对 RecyclerView 高滚动压力的简单内存优化（可能触发 GC）。
     */
    public static void optimizeMemoryForRecyclerView() {
        try {
            int memoryUsage = getMemoryUsagePercentage();
            if (memoryUsage > 70) {
                Log.d(TAG, "Memory usage high (" + memoryUsage + "%), optimizing for RecyclerView");
                System.gc();
                try {
                    Runtime.getRuntime().gc();
                } catch (Exception e) {
                    Log.w(TAG, "Could not force runtime GC: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error optimizing memory: " + e.getMessage());
        }
    }

    /**
     * 是否应跳过图标加载（内存紧张时返回 true）。
     */
    public static boolean shouldSkipIconLoading() {
        try {
            int memoryUsage = getMemoryUsagePercentage();
            return memoryUsage > 75; // skip when > 75%
        } catch (Exception e) {
            Log.e(TAG, "Error checking if should skip icon loading: " + e.getMessage());
            return false;
        }
    }

    /**
     * 返回一段简要的内存状态字符串。
     */
    public static String getMemoryInfo() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            return "Memory: " + (usedMemory / 1024 / 1024) + "MB used / " + (maxMemory / 1024 / 1024) + "MB max (" + getMemoryUsagePercentage() + "%)";
        } catch (Exception e) {
            return "Memory: Unknown (" + e.getMessage() + ")";
        }
    }
}
