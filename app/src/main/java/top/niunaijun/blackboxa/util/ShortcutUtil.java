package top.niunaijun.blackboxa.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.EditText;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.app.App;
import top.niunaijun.blackboxa.app.AppManager;
import top.niunaijun.blackboxa.bean.AppInfo;
import top.niunaijun.blackboxa.view.main.ShortcutActivity;

public final class ShortcutUtil {
    private ShortcutUtil() {}

    public static void createShortcut(Context context, int userID, AppInfo info) {
        if (context == null || info == null) return;

        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            final String defaultLabel = info.getName() + userID;

            final EditText input = new EditText(context);
            input.setHint(R.string.shortcut_name);
            input.setText(defaultLabel);

            new AlertDialog.Builder(context)
                    .setTitle(R.string.app_shortcut)
                    .setView(input)
                    .setPositiveButton(R.string.done, (dialog, which) -> {
                        String label = input.getText() != null ? input.getText().toString() : defaultLabel;

                        Intent intent = new Intent(context, ShortcutActivity.class)
                                .setAction(Intent.ACTION_MAIN)
                                .putExtra("pkg", info.getPackageName())
                                .putExtra("userId", userID);

                        Bitmap iconBitmap = ensureBitmap(info.getIcon(), 96, 96);
                        IconCompat iconCompat = IconCompat.createWithBitmap(iconBitmap);

                        ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, info.getPackageName() + userID)
                                .setIntent(intent)
                                .setShortLabel(label)
                                .setLongLabel(label)
                                .setIcon(iconCompat)
                                .build();

                        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
                        showAllowPermissionDialog(context);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            ToastEx.toast(R.string.cannot_create_shortcut);
        }
    }

    private static void showAllowPermissionDialog(Context context) {
        try {
            if (!AppManager.getMBlackBoxLoader().showShortcutPermissionDialog()) {
                return;
            }

            new AlertDialog.Builder(context)
                    .setTitle(R.string.try_add_shortcut)
                    .setMessage(R.string.add_shortcut_fail_msg)
                    .setPositiveButton(R.string.done, null)
                    .setNegativeButton(R.string.permission_setting, (d, w) ->
                            ContextUtil.openAppSystemSettings(App.getContext()))
                    .setNeutralButton(R.string.no_reminders, (d, w) ->
                            AppManager.getMBlackBoxLoader().invalidShortcutPermissionDialog(false))
                    .show();
        } catch (Throwable t) {
            // Fallback: no-op on dialog errors
        }
    }

    private static Bitmap ensureBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
            if (bmp != null) return bmp;
        }
        int w = width > 0 ? width : (drawable != null ? Math.max(1, drawable.getIntrinsicWidth()) : 96);
        int h = height > 0 ? height : (drawable != null ? Math.max(1, drawable.getIntrinsicHeight()) : 96);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (drawable != null) {
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } else {
            canvas.drawColor(Color.LTGRAY);
        }
        return bitmap;
    }
}
