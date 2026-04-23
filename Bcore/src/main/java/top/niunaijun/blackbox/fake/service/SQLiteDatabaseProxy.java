package top.niunaijun.blackbox.fake.service;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.Slog;


/**
 * SQLiteDatabase 代理：
 * - 针对 DurableJob 等已知表/SQL 的查询与执行做安全兜底（返回空/忽略），其余透传；
 * - 降低三方任务持久化表异常导致的崩溃概率。
 * 仅添加中文注释，不改动任何逻辑。
 */
public class SQLiteDatabaseProxy extends ClassInvocationStub {
    public static final String TAG = "SQLiteDatabaseProxy";

    public SQLiteDatabaseProxy() {
        super();
    }

    @Override
    protected Object getWho() {
        return null; 
    }

    @Override
    protected void inject(Object baseInvocation, Object proxyInvocation) {
        
    }

    @Override
    public boolean isBadEnv() {
        return false;
    }

    
    @ProxyMethod("rawQuery")
    public static class RawQuery extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                String sql = (String) args[0];
                if (sql != null && sql.contains("DurableJob")) {
                    Slog.w(TAG, "SQLiteDatabase: rawQuery called with DurableJob table, returning empty cursor");
                    
                    return null;
                }
                return method.invoke(who, args);
            } catch (SQLiteException e) {
                Slog.w(TAG, "SQLiteDatabase: rawQuery failed, returning null", e);
                return null;
            } catch (Exception e) {
                Slog.e(TAG, "SQLiteDatabase: rawQuery error", e);
                return method.invoke(who, args);
            }
        }
    }

    
    @ProxyMethod("query")
    public static class Query extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                String table = (String) args[0];
                if (table != null && table.equals("DurableJob")) {
                    Slog.w(TAG, "SQLiteDatabase: query called on DurableJob table, returning empty cursor");
                    
                    return null;
                }
                return method.invoke(who, args);
            } catch (SQLiteException e) {
                Slog.w(TAG, "SQLiteDatabase: query failed, returning null", e);
                return null;
            } catch (Exception e) {
                Slog.e(TAG, "SQLiteDatabase: query error", e);
                return method.invoke(who, args);
            }
        }
    }

    
    @ProxyMethod("execSQL")
    public static class ExecSQL extends MethodHook {
        @Override
        protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            try {
                String sql = (String) args[0];
                if (sql != null && sql.contains("DurableJob")) {
                    Slog.w(TAG, "SQLiteDatabase: execSQL called with DurableJob table, ignoring");
                    
                    return null;
                }
                return method.invoke(who, args);
            } catch (SQLiteException e) {
                Slog.w(TAG, "SQLiteDatabase: execSQL failed, ignoring", e);
                return null;
            } catch (Exception e) {
                Slog.e(TAG, "SQLiteDatabase: execSQL error", e);
                return method.invoke(who, args);
            }
        }
    }
}
