package top.niunaijun.blackboxa.view.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import cbfg.rvadapter.RVAdapter;
import com.ferfalk.simplesearchview.SimpleSearchView;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.InstalledAppBean;
import top.niunaijun.blackboxa.databinding.ActivityListBinding;
import top.niunaijun.blackboxa.util.InjectionUtil;
import top.niunaijun.blackboxa.view.base.BaseActivity;

public class ListActivity extends BaseActivity {

    private ActivityListBinding viewBinding;
    private RVAdapter<InstalledAppBean> mAdapter;
    private ListViewModel viewModel;
    private List<InstalledAppBean> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.installed_app, true,null);
        // APP 列表
        mAdapter = new RVAdapter<InstalledAppBean>(this, new ListAdapter())
                .bind(viewBinding.appListRecyclerView)
                .setItemClickListener((view, installedAppBean, integer) -> {
                    finishWithResult((installedAppBean).getPackageName());
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
        viewModel = new ViewModelProvider(this, InjectionUtil.getListFactory())
                .get(ListViewModel.class);
        int userID = getIntent().getIntExtra("userID", 0);
        viewModel.getInstallAppList(userID);
        viewBinding.toolbarLayout.toolbar.setTitle(R.string.installed_app);

        viewModel.getLoadingLiveData().observe(this, loading -> {
            if (Boolean.TRUE.equals(loading)) {
                viewBinding.stateView.showLoading();
            } else {
                viewBinding.stateView.showContent();
            }
        });

        viewModel.getAppsLiveData().observe(this, list -> {
            if (list != null) {
                appList = list;
                viewBinding.searchView.setQuery("", false);
                filterApp("");
                if (!list.isEmpty()) {
                    viewBinding.stateView.showContent();
                    viewModel.previewInstalledList();
                } else {
                    viewBinding.stateView.showEmpty();
                }
            }
        });
    }

    private void filterApp(String newText) {
        List<InstalledAppBean> newList = new ArrayList<>();
        for (InstalledAppBean bean : appList) {
            if (bean.getName().toLowerCase().contains(newText.toLowerCase())
                    || bean.getPackageName().toLowerCase().contains(newText.toLowerCase())) {
                newList.add(bean);
            }
        }
        mAdapter.setItems(newList,false,false);
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
        viewModel.getLoadingLiveData().postValue(true);
        viewModel.getLoadingLiveData().removeObservers(this);
        viewModel.getAppsLiveData().postValue(null);
        viewModel.getAppsLiveData().removeObservers(this);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ListActivity.class);
        context.startActivity(intent);
    }
}
