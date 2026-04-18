package top.niunaijun.blackboxa.view.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.viewpager2.widget.ViewPager2;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.system.buser.BUserInfo;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.app.App;
import top.niunaijun.blackboxa.app.AppManager;
import top.niunaijun.blackboxa.databinding.ActivityMainBinding;
import top.niunaijun.blackboxa.util.Resolution;
import top.niunaijun.blackboxa.view.apps.AppsFragment;
import top.niunaijun.blackboxa.view.base.LoadingActivity;
import top.niunaijun.blackboxa.view.fake.FakeManagerActivity;
import top.niunaijun.blackboxa.view.list.ListActivity;
import top.niunaijun.blackboxa.view.setting.SettingActivity;

public class MainActivity extends LoadingActivity {
    private static final String TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private ActivityMainBinding viewBinding;
    private ViewPagerAdapter mViewPagerAdapter;
    private final java.util.List<AppsFragment> fragmentList = new java.util.ArrayList<>();
    private int currentUser = 0;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            try {
                BlackBoxCore.get().onBeforeMainActivityOnCreate(this);
            } catch (Exception e) {
                Log.e(TAG, "Error in onBeforeMainActivityOnCreate: " + e.getMessage());
            }

            viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
            setContentView(viewBinding.getRoot());
            initToolbar(viewBinding.toolbarLayout.toolbar, R.string.app_name, false, null);
            initViewPager();
            initFab();
            initToolbarSubTitle();

            checkStoragePermission();
            checkVpnPermission();

            try {
                BlackBoxCore.get().onAfterMainActivityOnCreate(this);
            } catch (Exception e) {
                Log.e(TAG, "Error in onAfterMainActivityOnCreate: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate: " + e.getMessage());
            showErrorDialog("Failed to initialize app: " + e.getMessage());
        }
    }

    private void checkStoragePermission() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (!android.os.Environment.isExternalStorageManager()) {
                    Log.w(TAG, "MANAGE_EXTERNAL_STORAGE permission not granted");
                    showStoragePermissionDialog();
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestLegacyStoragePermission();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking storage permission: " + e.getMessage());
        }
    }

    private void requestLegacyStoragePermission() {
        try {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            Log.e(TAG, "Error requesting storage permission: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int r : grantResults) {
                if (r != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Log.d(TAG, "Storage permissions granted");
            } else {
                Log.w(TAG, "Storage permissions denied");
            }
        }
    }

    private void showStoragePermissionDialog() {
        try {
            new MaterialDialog(this,MaterialDialog.getDEFAULT_BEHAVIOR())
                    .title(null, "Storage Permission Required")
                    .message(null, "该应用需获取“所有文件访问”权限以正常运行沙盒化应用。若缺少此权限，部分应用可能无法正常运行。\n\n请在下一页授权。",null)
                    .positiveButton(null, "批准", dialog -> { openAllFilesAccessSettings(); return null; })
                    .negativeButton(null, "稍后", dialog -> null)
                    .cancelable(false)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing storage permission dialog: " + e.getMessage());
        }
    }

    private final ActivityResultLauncher<Intent> storagePermissionResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        if (android.os.Environment.isExternalStorageManager()) {
                            Log.d(TAG, "Storage permission granted!");
                        } else {
                            Log.w(TAG, "Storage permission still not granted");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling storage permission result: " + e.getMessage());
                }
            });

    private void openAllFilesAccessSettings() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                storagePermissionResult.launch(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening storage settings: " + e.getMessage());
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storagePermissionResult.launch(intent);
            } catch (Exception e2) {
                Log.e(TAG, "Error opening fallback storage settings: " + e2.getMessage());
            }
        }
    }

    private void checkVpnPermission() {
        try {
            Intent vpnIntent = VpnService.prepare(this);
            if (vpnIntent != null) {
                vpnPermissionResult.launch(vpnIntent);
            } else {
                Log.d(TAG, "VPN permission already granted");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking VPN permission: " + e.getMessage());
        }
    }

    private final ActivityResultLauncher<Intent> vpnPermissionResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "VPN permission granted!");
                    } else {
                        Log.w(TAG, "VPN permission denied by user");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling VPN permission result: " + e.getMessage());
                }
            });

    private void showErrorDialog(String message) {
        try {
            new MaterialDialog(this,MaterialDialog.getDEFAULT_BEHAVIOR())
                    .title(null, "Error")
                    .message(null, message,null)
                    .positiveButton(null, "OK", dialog -> { finish(); return null; })
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing error dialog: " + e.getMessage());
            finish();
        }
    }

    private void initToolbarSubTitle() {
        try {
            updateUserRemark(0);
            // 设置副标题点击打开输入框
            viewBinding.toolbarLayout.toolbar.getChildAt(1).setOnClickListener(v -> {
                try {
                    new MaterialDialog(this,MaterialDialog.getDEFAULT_BEHAVIOR())
                            .title(null, getString(R.string.userRemark));
                        {
                            android.widget.EditText editText = new android.widget.EditText(this);
                            CharSequence prefill = viewBinding.toolbarLayout.toolbar.getSubtitle();
                            if (prefill != null) {
                                editText.setText(prefill);
                                editText.setSelection(prefill.length());
                            }
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle(getString(R.string.userRemark))
                                    .setView(editText)
                                    .setPositiveButton(getString(R.string.done), (dialogInterface, which) -> {
                                        try {
                                            String input = editText.getText().toString();
                                            AppManager.getMRemarkSharedPreferences().edit()
                                                    .putString("Remark" + currentUser, input)
                                                    .apply();
                                            viewBinding.toolbarLayout.toolbar.setSubtitle(input);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error saving user remark: " + e.getMessage());
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {})
                                    .show();
                        }
                } catch (Exception e) {
                    Log.e(TAG, "Error showing remark dialog: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in initToolbarSubTitle: " + e.getMessage());
        }
    }

    private void initViewPager() {
        try {
            List<BUserInfo> userList = BlackBoxCore.get().getUsers();
            for (BUserInfo u : userList) {
                fragmentList.add(AppsFragment.newInstance(u.getId()));
            }
            currentUser = userList.isEmpty() ? 0 : userList.get(0).getId();
            fragmentList.add(AppsFragment.newInstance(userList.size()));

            mViewPagerAdapter = new ViewPagerAdapter(this);
            mViewPagerAdapter.replaceData(fragmentList);
            viewBinding.viewPager.setAdapter(mViewPagerAdapter);
            viewBinding.dotsIndicator.setViewPager2(viewBinding.viewPager);
            viewBinding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    try {
                        super.onPageSelected(position);
                        currentUser = fragmentList.get(position).getUserID();
                        updateUserRemark(currentUser);
                        showFloatButton(true);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onPageSelected: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in initViewPager: " + e.getMessage());
        }
    }

    private void initFab() {
        try {
            viewBinding.fab.setOnClickListener(v -> {
                try {
                    int userId = viewBinding.viewPager.getCurrentItem();
                    Intent intent = new Intent(this, ListActivity.class);
                    intent.putExtra("userID", userId);
                    apkPathResult.launch(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching ListActivity: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in initFab: " + e.getMessage());
        }
    }

    public void showFloatButton(boolean show) {
        try {
            float tranY = Resolution.convertDpToPixel(120F, App.getContext());
            long time = 200L;
            if (show) {
                viewBinding.fab.animate().translationY(0f).alpha(1f).setDuration(time).start();
            } else {
                viewBinding.fab.animate().translationY(tranY).alpha(0f).setDuration(time).start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in showFloatButton: " + e.getMessage());
        }
    }

    /**
     * 更新所有ViewPager中的界面
     */
    public void scanUser() {
        try {
            Log.e(TAG,"scanUser()", new RuntimeException());
            List<top.niunaijun.blackboxa.bean.InstalledAppBean> userList = new java.util.ArrayList<>();
            mViewPagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error in scanUser: " + e.getMessage());
        }
    }

    private void updateUserRemark(int userId) {
        try {
            String remark = AppManager.getMRemarkSharedPreferences()
                    .getString("Remark" + userId, "User " + userId);
            if (remark == null || remark.isEmpty()) {
                remark = "User " + userId;
            }
            viewBinding.toolbarLayout.toolbar.setSubtitle(remark);
        } catch (Exception e) {
            Log.e(TAG, "Error updating user remark: " + e.getMessage());
            viewBinding.toolbarLayout.toolbar.setSubtitle("User " + userId);
        }
    }

    private final ActivityResultLauncher<Intent> apkPathResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        int userId = data.getIntExtra("userID", 0);
                        String source = data.getStringExtra("source");
                        if (source != null) {
                            fragmentList.get(userId).installApk(source);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error handling APK path result: " + e.getMessage());
                }
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating options menu: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.main_git) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ALEX5402/NewBlackbox")));
            } else if (id == R.id.main_setting) {
                SettingActivity.start(this);
            } else if (id == R.id.main_tg) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/newblackboxa")));
            } else if (id == R.id.fake_location) {
                Intent intent = new Intent(this, FakeManagerActivity.class);
                intent.putExtra("userID", 0);
                startActivity(intent);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error handling menu item selection: " + e.getMessage());
            return false;
        }
    }
}
