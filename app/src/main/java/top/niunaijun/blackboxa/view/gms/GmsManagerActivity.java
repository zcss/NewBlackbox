package top.niunaijun.blackboxa.view.gms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import cbfg.rvadapter.RVAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.GmsBean;
import top.niunaijun.blackboxa.databinding.ActivityGmsBinding;
import top.niunaijun.blackboxa.util.InjectionUtil;
import top.niunaijun.blackboxa.view.base.LoadingActivity;

public class GmsManagerActivity extends LoadingActivity {

    private GmsViewModel viewModel;
    private RVAdapter<GmsBean> mAdapter;
    private ActivityGmsBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityGmsBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.gms_manager, true, null);
        initViewModel();
        initRecyclerView();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this, InjectionUtil.getGmsFactory())
                .get(GmsViewModel.class);
        showLoading();

        viewModel.getMInstalledLiveData().observe(this, list -> {
            hideLoading();
            mAdapter.setItems(list, false, false);
        });

        viewModel.getMUpdateInstalledLiveData().observe(this, result -> {
            if (result == null) return;
            java.util.List<GmsBean> items = mAdapter.getItems();
            for (int index = 0; index < items.size(); index++) {
                GmsBean bean = items.get(index);
                if (bean.getUserID() == result.getUserID()) {
                    if (result.getSuccess()) {
                        bean.setInstalledGms(!bean.isInstalledGms());
                    }
                    mAdapter.replaceAt(index, bean, false);
                    break;
                }
            }
            hideLoading();

            if (result.getSuccess()) {
                Toast.makeText(this, result.getMsg(), Toast.LENGTH_SHORT).show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.gms_manager)
                        .setMessage(result.getMsg())
                        .setPositiveButton(R.string.done, (d, w) -> {})
                        .show();
            }
        });

        viewModel.getInstalledUser();
    }

    private void initRecyclerView() {
        mAdapter = new RVAdapter<GmsBean>(this, new GmsAdapter()).bind(viewBinding.appListRecyclerView)
                .setItemClickListener((view, item, position) -> {
                    Switch checkbox = view.findViewById(R.id.checkbox);
                    if (item.isInstalledGms()) {
                        uninstallGms(item.getUserID(), checkbox);
                    } else {
                        installGms(item.getUserID(), checkbox);
                    }
                    return kotlin.Unit.INSTANCE;
                });
        viewBinding.appListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void installGms(int userID, Switch checkbox) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.enable_gms)
                .setMessage(R.string.enable_gms_hint)
                .setPositiveButton(R.string.done, (d, w) -> {
                    showLoading();
                    viewModel.installGms(userID);
                })
                .setNegativeButton(R.string.cancel, (d, w) -> checkbox.setChecked(!checkbox.isChecked()))
                .show();
    }

    private void uninstallGms(int userID, Switch checkbox) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.disable_gms)
                .setMessage(R.string.disable_gms_hint)
                .setPositiveButton(R.string.done, (d, w) -> {
                    showLoading();
                    viewModel.uninstallGms(userID);
                })
                .setNegativeButton(R.string.cancel, (d, w) -> checkbox.setChecked(!checkbox.isChecked()))
                .show();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GmsManagerActivity.class);
        context.startActivity(intent);
    }
}
