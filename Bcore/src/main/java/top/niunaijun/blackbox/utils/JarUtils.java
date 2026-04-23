package top.niunaijun.blackbox.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * JAR/文件工具：带进度的复制、文件哈希、JAR 粗略校验、删除/创建目录、推荐缓冲区大小。
 */
public class JarUtils {
    private static final String TAG = "JarUtils";

    /** 将输入流复制到目标文件（临时文件写入后原子替换），按进度定期日志。 */
    public static void copyFileWithProgress(InputStream input, File target, String fileName) throws IOException {
        if (input == null) {
            throw new IOException("Input stream is null");
        }
        if (target == null) {
            throw new IOException("Target file is null");
        }
        File targetDir = target.getParentFile();
        if (targetDir != null && !targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Failed to create target directory: " + targetDir);
        }
        FileOutputStream output = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile(target.getName(), ".tmp", targetDir);
            output = new FileOutputStream(tempFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            long lastLogTime = System.currentTimeMillis();
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
                if (totalBytes % 102400 == 0 && System.currentTimeMillis() - lastLogTime > 1000) {
                    Log.d(TAG, "Copying " + fileName + ": " + (totalBytes / 1024) + "KB");
                    lastLogTime = System.currentTimeMillis();
                }
            }
            output.flush();
            output.close();
            output = null;
            if (!tempFile.renameTo(target)) {
                throw new IOException("Failed to move temporary file to target: " + target);
            }
            Log.d(TAG, "Successfully copied " + fileName + " (" + totalBytes + " bytes)");
        } catch (IOException e) {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    Log.w(TAG, "Failed to delete temporary file: " + tempFile);
                }
            }
            throw e;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Log.w(TAG, "Failed to close output stream", e);
                }
            }
        }
    }

    /** 计算文件 SHA-256 哈希十六进制字符串。 */
    public static String calculateFileHash(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            fis.close();
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            Log.w(TAG, "Failed to calculate file hash", e);
            return null;
        }
    }

    /** 简单校验 JAR：存在至少一个条目且文件非空。 */
    public static boolean verifyJarFile(File jarFile) {
        if (jarFile == null || !jarFile.exists()) {
            return false;
        }
        if (jarFile.length() == 0) {
            return false;
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry entry;
            int entryCount = 0;
            while ((entry = zis.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > 10) {
                    break; // 足够判断
                }
                zis.closeEntry();
            }
            return entryCount > 0;
        } catch (IOException e) {
            Log.w(TAG, "JAR file verification failed: " + jarFile.getName(), e);
            return false;
        }
    }

    /** 返回 JAR 简要信息：大小/有效性/哈希（前缀）。 */
    public static String getJarInfo(File jarFile) {
        if (jarFile == null || !jarFile.exists()) {
            return "File not found";
        }
        StringBuilder info = new StringBuilder();
        info.append("Size: ").append(jarFile.length()).append(" bytes, ");
        if (verifyJarFile(jarFile)) {
            info.append("Valid JAR");
        } else {
            info.append("Invalid JAR");
        }
        String hash = calculateFileHash(jarFile);
        if (hash != null) {
            info.append(", Hash: ").append(hash.substring(0, Math.min(16, hash.length())));
        }
        return info.toString();
    }

    /** 尝试删除文件（最多三次，带递增退避）。 */
    public static boolean safeDelete(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        int retryCount = 0;
        while (retryCount < 3) {
            if (file.delete()) {
                return true;
            }
            retryCount++;
            if (retryCount < 3) {
                try {
                    Thread.sleep(100 * retryCount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        Log.w(TAG, "Failed to delete file after " + retryCount + " attempts: " + file);
        return false;
    }

    /** 创建目录（设置为读/写仅限当前进程）。 */
    public static boolean createDirectory(File dir) {
        if (dir == null) {
            return false;
        }
        if (dir.exists()) {
            return dir.isDirectory();
        }
        if (dir.mkdirs()) {
            dir.setReadable(true, true);
            dir.setWritable(true, true);
            return true;
        }
        return false;
    }

    /** 根据可用内存推荐缓冲区大小。 */
    public static int getOptimalBufferSize() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        if (maxMemory > 512 * 1024 * 1024) {
            return 32768; // 32KB
        } else if (maxMemory > 256 * 1024 * 1024) {
            return 16384; // 16KB
        } else {
            return 8192; // 8KB
        }
    }
}
