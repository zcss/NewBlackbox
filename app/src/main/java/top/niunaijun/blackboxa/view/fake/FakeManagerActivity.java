package top.niunaijun.blackboxa.view.fake;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import cbfg.rvadapter.RVAdapter;

import com.ferfalk.simplesearchview.SimpleSearchView;
import top.niunaijun.blackbox.entity.location.BLocation;
import top.niunaijun.blackbox.fake.frameworks.BLocationManager;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.FakeLocationBean;
import top.niunaijun.blackboxa.databinding.ActivityListBinding;
import top.niunaijun.blackboxa.util.InjectionUtil;
import top.niunaijun.blackboxa.util.ToastEx;
import top.niunaijun.blackboxa.view.base.BaseActivity;

/**
 * 虚拟定位管理页：按应用配置虚拟坐标、支持搜索/关闭虚拟定位。
 */
public class FakeManagerActivity extends BaseActivity {
    private static final String TAG = "FakeManagerActivity";

    private ActivityListBinding viewBinding;
    private RVAdapter<FakeLocationBean> mAdapter;
    private FakeLocationViewModel viewModel;
    private List<FakeLocationBean> appList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> locationResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double latitude = data.getDoubleExtra("latitude", 0.0);
                    double longitude = data.getDoubleExtra("longitude", 0.0);
                    String pkg = data.getStringExtra("pkg");

                    viewModel.setPattern(currentUserID(), String.valueOf(pkg), BLocationManager.OWN_MODE);
                    viewModel.setLocation(currentUserID(), String.valueOf(pkg), new BLocation(latitude, longitude));

                    ToastEx.toast(getString(R.string.set_location, String.valueOf(latitude), String.valueOf(longitude)));

                    loadAppList();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.fake_location, true, null);

        mAdapter = new RVAdapter<FakeLocationBean>(this, new FakeLocationAdapter())
                .bind(viewBinding.appListRecyclerView)
                .setItemClickListener((view, data, position) -> {
                    Intent intent = new Intent(FakeManagerActivity.this, FollowMyLocationOverlay.class);
                    intent.putExtra("location", data.getFakeLocation());
                    intent.putExtra("pkg", data.getPackageName());
                    locationResult.launch(intent);
                    return null;
                })
                .setItemLongClickListener((view, item, position) -> {
                    disableFakeLocation(item, position);
                    return null;
                });

        viewBinding.appListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        initSearchView();
        initViewModel();
    }

    /** 关闭某应用的虚拟定位并更新列表项。*/
    private void disableFakeLocation(@NonNull FakeLocationBean item, int position) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.close_fake_location)
                .setMessage(getString(R.string.close_app_fake_location, item.getName()))
                .setNegativeButton(R.string.cancel, null)
//                .setPositiveButton(R.string.done, (d,w) -> { return;
//                    BLocationManager.disableFakeLocation(currentUserID(), item.getPackageName());
//                    ToastEx.toast(getString(R.string.close_fake_location_success, item.getName()));
//                    item.setFakeLocationPattern(BLocationManager.CLOSE_MODE);
//                    mAdapter.replaceAt(position, item, false);
//                    return null;
//                })
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BLocationManager.disableFakeLocation(currentUserID(), item.getPackageName());
                        ToastEx.toast(getString(R.string.close_fake_location_success, item.getName()));
                        item.setFakeLocationPattern(BLocationManager.CLOSE_MODE);
                        mAdapter.replaceAt(position, item, false);
                    }
                })
                .show();
    }

    /** 初始化搜索框监听。*/
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
    /** 加载应用列表并显示加载态。*/
    public void loadAppList() {
        viewBinding.stateView.showLoading();
        viewModel.getInstallAppList(currentUserID());
    }

    /** 初始化 ViewModel 并绑定数据观察者。*/
    private void initViewModel() {
        viewModel = new ViewModelProvider(this, InjectionUtil.getFakeLocationFactory()).get(FakeLocationViewModel.class);
        loadAppList();
        viewBinding.toolbarLayout.toolbar.setTitle(R.string.fake_location);

        viewModel.getAppsLiveData().observe(this, it -> {
            if (it != null) {
                this.appList = it;
                viewBinding.searchView.setQuery("", false);
                filterApp("");
                if (!it.isEmpty()) {
                    viewBinding.stateView.showContent();
                } else {
                    viewBinding.stateView.showEmpty();
                }
            }
        });
    }



    /** 根据关键字过滤应用列表并刷新显示。*/
    private void filterApp(@NonNull String newText) {
        List<FakeLocationBean> newList = new ArrayList<>();
        for (FakeLocationBean bean : this.appList) {
            if (bean.getName().toLowerCase().contains(newText.toLowerCase()) ||
                bean.getPackageName().toLowerCase().contains(newText.toLowerCase())) {
                newList.add(bean);
            }
        }
        mAdapter.setItems(newList, false, false);
    }

    private void finishWithResult(@NonNull String source) {
        Intent intent = getIntent();
        intent.putExtra("source", source);
        setResult(Activity.RESULT_OK, intent);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        viewBinding.searchView.setMenuItem(menu.findItem(R.id.list_search));
        return true;
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, FakeManagerActivity.class);
        context.startActivity(intent);
    }
}
