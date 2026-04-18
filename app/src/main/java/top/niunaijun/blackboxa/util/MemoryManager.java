package top.niunaijun.blackboxa.util;

import android.util.Log;

public final class MemoryManager {
    private static final String TAG = "MemoryManager";
    private static final double MEMORY_THRESHOLD = 0.8;   // 80%
    private static final double CRITICAL_MEMORY_THRESHOLD = 0.9; // 90%

    private MemoryManager() {}

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

    public static boolean shouldSkipIconLoading() {
        try {
            int memoryUsage = getMemoryUsagePercentage();
            return memoryUsage > 75; // skip when > 75%
        } catch (Exception e) {
            Log.e(TAG, "Error checking if should skip icon loading: " + e.getMessage());
            return false;
        }
    }

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
