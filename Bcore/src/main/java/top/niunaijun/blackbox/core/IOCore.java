package top.niunaijun.blackbox.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import top.niunaijun.blackbox.BlackBoxCore;

import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.core.settings.ProxySettingsCore;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.utils.TrieTree;


@SuppressLint("SdCardPath")
/**
 * IO 重定向核心：将应用访问的系统路径映射到沙盒目录，支持黑白名单、规则缓存与原生规则注入。
 */
public class IOCore {
    public static final String TAG = "IOCore";

    private static final IOCore sIOCore = new IOCore();
    // 重定向规则前缀Trie，存“原始路径前缀”，用于最长前缀匹配查找
    private static final TrieTree mTrieTree = new TrieTree();
    // 黑名单前缀Trie，命中则直接返回原路径或固定路径（跳过重定向）
    private static final TrieTree sBlackTree = new TrieTree();
    // 前缀到目标路径的映射表：key=原始前缀，value=重定向后的目标根路径
    private final Map<String, String> mRedirectMap = new LinkedHashMap<>();

    private static final Map<String, Map<String, String>> sCachePackageRedirect = new HashMap<>();

    public static IOCore get() {
        return sIOCore;
    }

    
    /** 注册路径重定向规则 */
    public void addRedirect(String origPath, String redirectPath) {
        if (TextUtils.isEmpty(origPath) || TextUtils.isEmpty(redirectPath) || mRedirectMap.get(origPath) != null)
            return;
        
        // 记录原始路径前缀到Trie，供查找时做最长前缀匹配
        mTrieTree.add(origPath);
        // 建立前缀->目标路径的映射，匹配后用它做字符串替换
        mRedirectMap.put(origPath, redirectPath);
        File redirectFile = new File(redirectPath);
        if (!redirectFile.exists()) {
            FileUtils.mkdirs(redirectPath);
        }
        NativeCore.addIORule(origPath, redirectPath);
    }

    /** 添加黑名单路径（跳过重定向，直接匹配返回） */
    public void addBlackRedirect(String path) {
        if (TextUtils.isEmpty(path))
            return;
        sBlackTree.add(path);
    }

    /** 查询重定向结果：优先黑名单，随后最长前缀匹配替换；在映射前先判断是否启用路径代理 */
    public String redirectPath(String path) {
        String pkg = BlackBoxCore.getAppPackageName();
        int userId = BlackBoxCore.getUserId();
        Slog.d(TAG,"redirectPath() ：" + path+ " pkg: "+pkg + " userId: "+userId);
        if (path.contains("/blackbox/")) { // 主程序不做替换
            Slog.d(TAG,"redirectPath() blackbox 主程序");
            return path;
        }
        if (TextUtils.isEmpty(path))
            return path;
        Slog.w(TAG,"pkg ：" + pkg);
        if (pkg != null && pkg.contains("top.niunaijun.blackbox")){
            Slog.w(TAG,"redirectPath() pkg 主程序");
            return path;
        }
        // 用户/包名维度的开关：未开启路径代理则直接返回原路径
        Slog.d(TAG,"userId ：" + userId);

//        if (!ProxySettingsCore.isPathEnabled(userId, pkg)) {
//            Slog.w(TAG,"pkg ： 不需要替换路径");
//            return path;
//        }

        Slog.e(TAG,"开始替换");
        String search = sBlackTree.search(path);
        if (!TextUtils.isEmpty(search))
            return search;
        String key = mTrieTree.search(path);
        if (!TextUtils.isEmpty(key))
            path = path.replace(key, Objects.requireNonNull(mRedirectMap.get(key)));

        return path;
    }

    public File redirectPath(File path) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr));
    }

    public String redirectPath(String path, Map<String, String> rule) {
        if (TextUtils.isEmpty(path))
            return path;
        Slog.w(TAG,"redirectPath() 002： "+ path);
        // 用户/包名维度的开关：未开启路径代理则直接返回原路径
        int userId = BlackBoxCore.getUserId();
        String pkg = BlackBoxCore.getAppPackageName();
        if (TextUtils.isEmpty(pkg) && BlackBoxCore.getContext() != null) {
            pkg = BlackBoxCore.getContext().getPackageName();
        }
        if (!ProxySettingsCore.isPathEnabled(userId, pkg)) {
            return path;
        }
        // 规则匹配：使用Trie做最长前缀查找，命中后用 mRedirectMap 进行替换
        String key = mTrieTree.search(path);
        if (!TextUtils.isEmpty(key))
            path = path.replace(key, Objects.requireNonNull(rule.get(key)));
        Slog.w(TAG,"redirectPath() 002 返回： "+ path);
        return path;
    }

    public File redirectPath(File path, Map<String, String> rule) {
        if (path == null)
            return null;
        String pathStr = path.getAbsolutePath();
        return new File(redirectPath(pathStr, rule));
    }

    

    /**
     * 启用路径重定向：构建规则（nativeLib/dataDir/profiles/sdcard等）、可选隐藏root、注入到Native层。
     */
    public void enableRedirect(Context context) {
        Slog.e(TAG,"enableRedirect() 启用路径代理 ");
        Map<String, String> rule = new LinkedHashMap<>();
        Set<String> blackRule = new HashSet<>();
        String packageName = context.getPackageName();
        Slog.e(TAG,"enableRedirect() packageName： "+packageName);
        try {
            ApplicationInfo packageInfo = BlackBoxCore.getBPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA, BlackBoxCore.getUserId());
            int systemUserId = BlackBoxCore.getHostUserId();
            rule.put(String.format("/data/data/%s/lib", packageName), packageInfo.nativeLibraryDir);
            rule.put(String.format("/data/user/%d/%s/lib", systemUserId, packageName), packageInfo.nativeLibraryDir);

            rule.put(String.format("/data/data/%s", packageName), packageInfo.dataDir);
            rule.put(String.format("/data/user/%d/%s", systemUserId, packageName), packageInfo.dataDir);

            
            File profilesRoot = new File(BEnvironment.getVirtualRoot(), "profiles");
            FileUtils.mkdirs(profilesRoot.getAbsolutePath());
            
            rule.put("/data/misc/profiles", profilesRoot.getAbsolutePath());

            File profilesCurDir = new File(profilesRoot, String.format("cur/%d/%s", BlackBoxCore.getUserId(), packageName));
            File profilesRefDir = new File(profilesRoot, String.format("ref/%d/%s", BlackBoxCore.getUserId(), packageName));
            FileUtils.mkdirs(profilesCurDir.getAbsolutePath());
            FileUtils.mkdirs(profilesRefDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/cur/%d/%s", BlackBoxCore.getUserId(), packageName), profilesCurDir.getAbsolutePath());
            rule.put(String.format("/data/misc/profiles/ref/%d/%s", BlackBoxCore.getUserId(), packageName), profilesRefDir.getAbsolutePath());

            if (BlackBoxCore.getContext().getExternalCacheDir() != null && context.getExternalCacheDir() != null) {
                File external = BEnvironment.getExternalUserDir(BlackBoxCore.getUserId());

                
                rule.put("/sdcard", external.getAbsolutePath());
                rule.put(String.format("/storage/emulated/%d", systemUserId), external.getAbsolutePath());

                blackRule.add("/sdcard/Pictures");
                blackRule.add(String.format("/storage/emulated/%d/Pictures", systemUserId));
            }
            if (BlackBoxCore.get().isHideRoot()) {
                hideRoot(rule);
            }
            proc(rule);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : rule.keySet()) {
            get().addRedirect(key, rule.get(key));
        }
        for (String s : blackRule) {
            get().addBlackRedirect(s);
        }
        NativeCore.enableIO();
    }

    private void hideRoot(Map<String, String> rule) {
        rule.put("/system/app/Superuser.apk", "/system/app/Superuser.apk-fake");
        rule.put("/sbin/su", "/sbin/su-fake");
        rule.put("/system/bin/su", "/system/bin/su-fake");
        rule.put("/system/xbin/su", "/system/xbin/su-fake");
        rule.put("/data/local/xbin/su", "/data/local/xbin/su-fake");
        rule.put("/data/local/bin/su", "/data/local/bin/su-fake");
        rule.put("/system/sd/xbin/su", "/system/sd/xbin/su-fake");
        rule.put("/system/bin/failsafe/su", "/system/bin/failsafe/su-fake");
        rule.put("/data/local/su", "/data/local/su-fake");
        rule.put("/su/bin/su", "/su/bin/su-fake");
    }

    private void proc(Map<String, String> rule) {
        int appPid = BlackBoxCore.getAppPid();
        int pid = Process.myPid();
        String selfProc = "/proc/self/";
        String proc = "/proc/" + pid + "/";

        String cmdline = new File(BEnvironment.getProcDir(appPid), "cmdline").getAbsolutePath();
        rule.put(proc + "cmdline", cmdline);
        rule.put(selfProc + "cmdline", cmdline);
    }
}
