package top.niunaijun.blackboxa.view.fake;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ferfalk.simplesearchview.SimpleSearchView;

import java.util.ArrayList;
import java.util.List;

import cbfg.rvadapter.RVAdapter;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.AppInfo;
import top.niunaijun.blackboxa.bean.InstalledAppBean;
import top.niunaijun.blackboxa.databinding.ActivityListBinding;
import top.niunaijun.blackboxa.util.InjectionUtil;
import top.niunaijun.blackbox.core.settings.ProxySettingsCore;
import top.niunaijun.blackboxa.view.base.BaseActivity;
import top.niunaijun.blackboxa.view.apps.AppsViewModel;
import top.niunaijun.blackboxa.view.list.ListActivity;
import top.niunaijun.blackboxa.view.list.ListAdapter;

public class FakePathProxyActivity extends BaseActivity {

    private static final String TAG = "FakePathProxyActivity";
    private ActivityListBinding viewBinding;
    private RVAdapter<InstalledAppBean> mAdapter;
    private AppsViewModel viewModel;
    private int userId; // 记录当前 userId，供保存设置使用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.installed_app, true,null);
        // APP 列表
        mAdapter = new RVAdapter<InstalledAppBean>(this, new ListAdapter(ListAdapter.HOLDER_PATH_PROXY))
                .bind(viewBinding.appListRecyclerView)
                .setItemClickListener((view, installedAppBean, integer) -> {
                    // 弹出代理选择对话框
                    showProxyDialog(installedAppBean);
                    return kotlin.Unit.INSTANCE;
                });

        viewBinding.appListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        initSearchView();
        initViewModel();
    }

    private void initSearchView() {
        viewBinding.searchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                filterApp(newText);
                return true;
            }

            @Override
            public boolean onQueryTextCleared() {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        });
    }

    private void initViewModel() {
        Log.e(TAG,"initViewModel() ");
        viewModel = new ViewModelProvider(this, InjectionUtil.getAppsFactory()).get(AppsViewModel.class);
        userId = getIntent().getIntExtra("userID", 0);
        viewModel.getInstalledAppsWithRetry(userId);
        viewModel.getAppsLiveData().observe(this, list -> {
            if (list != null) {
                // 仅显示该 userID 下安装的应用（非系统应用已在仓库层处理）
                List<InstalledAppBean> beans = new java.util.ArrayList<>();
                for (AppInfo app : list) {
                    boolean enabled = ProxySettingsCore.hasAnyConfig(userId, app.getPackageName());
                    beans.add(new InstalledAppBean(app.getName(), app.getIcon(), app.getPackageName(), app.getSourceDir(), enabled));
                }
                mAdapter.setItems(beans, true, true);
                if (beans.isEmpty()) {
                    viewBinding.stateView.showEmpty();
                } else {
                    viewBinding.stateView.showContent();
                }
            }
        });
        viewBinding.toolbarLayout.toolbar.setTitle(getString(R.string.installed_app) + " (User " + userId + ")");

    }

    private void filterApp(String newText) {
        if (mAdapter == null) return;
        List<InstalledAppBean> current = mAdapter.getItems();
        List<InstalledAppBean> filtered = new ArrayList<>();
        for (InstalledAppBean bean : current) {
            String name = bean.getName() != null ? bean.getName() : "";
            String pkg = bean.getPackageName() != null ? bean.getPackageName() : "";
            String q = newText != null ? newText : "";
            if (name.toLowerCase().contains(q.toLowerCase()) || pkg.toLowerCase().contains(q.toLowerCase())) {
                filtered.add(bean);
            }
        }
        mAdapter.setItems(filtered, false, false);
    }

    private final ActivityResultLauncher<String> openDocumentedResult =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    finishWithResult(uri.toString());
                }
            });

    private void finishWithResult(String source) {
        Intent i = getIntent();
        i.putExtra("source", source);
        setResult(Activity.RESULT_OK, i);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getWindow().peekDecorView() != null) {
            imm.hideSoftInputFromWindow(getWindow().peekDecorView().getWindowToken(), 0);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (viewBinding.searchView.isSearchOpen()) {
            viewBinding.searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.list_choose) {
            openDocumentedResult.launch("application/vnd.android.package-archive");
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        MenuItem item = menu.findItem(R.id.list_search);
        viewBinding.searchView.setMenuItem(item);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.getLaunchLiveData().postValue(true);
        viewModel.getResultLiveData().removeObservers(this);
        viewModel.getAppsLiveData().postValue(null);
        viewModel.getAppsLiveData().removeObservers(this);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ListActivity.class);
        context.startActivity(intent);
    }

    // 弹出包含“路径代理”、“联系人代理”的多选对话框，并将选择保存到 SP（按 userId+packageName 绑定）
    private void showProxyDialog(InstalledAppBean app) {
        if (app == null || app.getPackageName() == null) return;
        final String pkg = app.getPackageName();

        // 读取当前已保存状态
        boolean savedPath = ProxySettingsCore.isPathEnabled(userId, pkg);
        boolean savedContacts = ProxySettingsCore.isContactsEnabled( userId, pkg);

        // 动态构建复选框视图
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);
        container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox cbPath = new CheckBox(this);
        cbPath.setText("路径代理");
        cbPath.setChecked(savedPath);
        container.addView(cbPath);

        CheckBox cbContacts = new CheckBox(this);
        cbContacts.setText("联系人代理");
        cbContacts.setChecked(savedContacts);
        container.addView(cbContacts);

        new AlertDialog.Builder(this)
                .setTitle("选择代理功能\n(" + pkg + ")")
                .setView(container)
                .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    boolean pathChecked = cbPath.isChecked();
                    boolean contactsChecked = cbContacts.isChecked();
                    ProxySettingsCore.setPathEnabled(userId, pkg, pathChecked);
                    ProxySettingsCore.setContactsEnabled( userId, pkg, contactsChecked);
                    Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

}
