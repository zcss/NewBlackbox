package top.niunaijun.blackbox.utils.compat;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;

/**
 * ContentProvider 兼容封装：统一 call 调用、获取 UnstableClient 并支持重试（带超时）。
 */
public class ContentProviderCompat {

    /**
     * 调用 Provider.call，低于 17 直接使用 ContentResolver.call，17+ 使用 Client 并支持重试。
     */
    public static Bundle call(Context context, Uri uri, String method, String arg, Bundle extras, int retryCount) throws IllegalAccessException {
        if (VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.getContentResolver().call(uri, method, arg, extras);
        }
        ContentProviderClient client = acquireContentProviderClientRetry(context, uri, retryCount);
        try {
            if (client == null) {
                throw new IllegalAccessException();
            }
            return client.call(method, arg, extras);
        } catch (RemoteException e) {
            throw new IllegalAccessException(e.getMessage());
        } finally {
            releaseQuietly(client);
        }
    }

    private static ContentProviderClient acquireContentProviderClient(Context context, Uri uri) {
        try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return context.getContentResolver().acquireUnstableContentProviderClient(uri);
            }
            return context.getContentResolver().acquireContentProviderClient(uri);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 尝试多次获取 ProviderClient，并在 2s 超时后放弃。 */
    public static ContentProviderClient acquireContentProviderClientRetry(Context context, Uri uri, int retryCount) {
        ContentProviderClient client = acquireContentProviderClient(context, uri);
        if (client == null) {
            int retry = 0;
            long startTime = System.currentTimeMillis();
            long timeout = 2000; // 保护性超时
            while (retry < retryCount && client == null) {
                // 若超时则中止，避免无意义等待
                if (System.currentTimeMillis() - startTime > timeout) {
                    break;
                }
                SystemClock.sleep(200); // 间隔重试
                retry++;
                client = acquireContentProviderClient(context, uri);
            }
        }
        return client;
    }

    /** 通过 Authority 名称重试获取 ProviderClient。 */
    public static ContentProviderClient acquireContentProviderClientRetry(Context context, String name, int retryCount) {
        ContentProviderClient client = acquireContentProviderClient(context, name);
        if (client == null) {
            int retry = 0;
            long startTime = System.currentTimeMillis();
            long timeout = 2000; // 保护性超时
            while (retry < retryCount && client == null) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    break;
                }
                SystemClock.sleep(200); // 间隔重试
                retry++;
                client = acquireContentProviderClient(context, name);
            }
        }
        return client;
    }

    private static ContentProviderClient acquireContentProviderClient(Context context, String name) {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return context.getContentResolver().acquireUnstableContentProviderClient(name);
        }
        return context.getContentResolver().acquireContentProviderClient(name);
    }

    /** 安静释放 Client，兼容 N+ 的 close 与老版本 release。 */
    private static void releaseQuietly(ContentProviderClient client) {
        if (client != null) {
            try {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    client.close();
                } else {
                    client.release();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
