package top.niunaijun.blackboxa.view.apps;

import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import cbfg.rvadapter.RVAdapter;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackboxa.R;
import top.niunaijun.blackboxa.bean.AppInfo;
import top.niunaijun.blackboxa.databinding.FragmentAppsBinding;
import top.niunaijun.blackboxa.util.InjectionUtil;
import top.niunaijun.blackboxa.util.MemoryManager;
import top.niunaijun.blackboxa.util.ToastEx;
import top.niunaijun.blackboxa.view.base.LoadingActivity;
import top.niunaijun.blackboxa.view.main.MainActivity;

/**
 * 应用列表 Fragment：显示并管理当前用户的应用条目（支持拖拽排序、启动、卸载、清数据、创建快捷方式等）。
 */
public class AppsFragment extends Fragment {
    private static final String TAG = "AppsFragment";

    private int userID = 0;
    private AppsViewModel viewModel;
    private RVAdapter<AppInfo> mAdapterAppList;
    private FragmentAppsBinding viewBinding;
    private PopupMenu popupMenu;

    /**
     * 创建指定用户的 AppsFragment。
     * @param userID 用户 ID
     * @return AppsFragment 实例
     */
    public static AppsFragment newInstance(int userID) {
        AppsFragment fragment = new AppsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("userID", userID);
        fragment.setArguments(bundle);
        return fragment;
    }

    /** 初始化 ViewModel 与读取 userID 参数。*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            viewModel = new ViewModelProvider(this, InjectionUtil.getAppsFactory()).get(AppsViewModel.class);
            Bundle args = getArguments();
            if (args != null) {
                userID = args.getInt("userID", 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
        }
    }

    /**
     * 初始化视图与 RecyclerView（包含缓存、预取、拖拽排序、点击/长按行为）。
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            viewBinding = FragmentAppsBinding.inflate(inflater, container, false);
            viewBinding.stateView.showEmpty();

            mAdapterAppList = new RVAdapter<AppInfo>(requireContext(), new AppsAdapter()).bind(viewBinding.appListRecyclerView);
            viewBinding.appListRecyclerView.setAdapter(mAdapterAppList);

            GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 4);
            layoutManager.setItemPrefetchEnabled(true);
            layoutManager.setInitialPrefetchItemCount(8);
            viewBinding.appListRecyclerView.setLayoutManager(layoutManager);

            viewBinding.appListRecyclerView.setItemViewCacheSize(20);
            viewBinding.appListRecyclerView.setHasFixedSize(true);

            viewBinding.appListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    try {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            MemoryManager.optimizeMemoryForRecyclerView();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in scroll state change: " + e.getMessage());
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    try {
                        super.onScrolled(recyclerView, dx, dy);
                        if (Math.abs(dy) > 100) {
                            if (MemoryManager.isMemoryCritical()) {
                                Log.w(TAG, "Memory critical during fast scrolling, forcing GC");
                                MemoryManager.forceGarbageCollectionIfNeeded();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in scroll: " + e.getMessage());
                    }
                }
            });

            AppsTouchCallBack touchCallBack = new AppsTouchCallBack((from, to) -> {
                try {
                    onItemMove(from, to);
                    viewModel.getUpdateSortLiveData().postValue(true);
                } catch (Exception e) {
                    Log.e(TAG, "Error in touch callback: " + e.getMessage());
                }
            });
            new ItemTouchHelper(touchCallBack).attachToRecyclerView(viewBinding.appListRecyclerView);

            mAdapterAppList.setItemClickListener((view, data, integer) -> {
                try {
                    showLoading();
                    viewModel.launchApk(data.getPackageName(), userID);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching app: " + e.getMessage());
                    hideLoading();
                }
                return null;
            });

            interceptTouch();
            setOnLongClick();
            return viewBinding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage());
            return new View(requireContext());
        }
    }

    /** 绑定数据与观察者。*/
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            initData();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage());
        }
    }

    /** 首次或服务可用后刷新安装应用列表。*/
    @Override
    public void onStart() {
        super.onStart();
        try {
            try {
                BlackBoxCore.get().addServiceAvailableCallback(new Runnable() {
                    @Override public void run() {
                        Log.d(TAG, "Services became available, refreshing app list");
                        viewModel.getInstalledAppsWithRetry(userID);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error registering service available callback: " + e.getMessage());
            }
            viewModel.getInstalledAppsWithRetry(userID);
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart: " + e.getMessage());
        }
    }

    /** 截获列表触摸事件：用于点击弹出菜单与滑动隐藏/显示 FAB。*/
    private void interceptTouch() {
        try {
            final Point point = new Point();
            final boolean[] isScrolling = {false};
            final long[] scrollStartTime = {0L};
            viewBinding.appListRecyclerView.setOnTouchListener((v, e) -> {
                try {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isScrolling[0] = false;
                            scrollStartTime[0] = System.currentTimeMillis();
                            point.set(0, 0);
                            break;
                        case MotionEvent.ACTION_UP:
                            long duration = System.currentTimeMillis() - scrollStartTime[0];
                            if (!isScrolling[0] && !isMove(point, e) && duration < 500) {
                                try { if (popupMenu != null) popupMenu.show(); } catch (Exception ex) { Log.e(TAG, "Error showing popup: " + ex.getMessage()); }
                            }
                            popupMenu = null;
                            point.set(0, 0);
                            isScrolling[0] = false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (point.x == 0 && point.y == 0) {
                                point.x = (int) e.getRawX();
                                point.y = (int) e.getRawY();
                            }
                            if (isMove(point, e)) {
                                isScrolling[0] = true;
                                if (popupMenu != null) popupMenu.dismiss();
                            }
                            isDownAndUp(point, e);
                            break;
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error in touch listener: " + ex.getMessage());
                }
                return false;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in interceptTouch: " + e.getMessage());
        }
    }

    private boolean isMove(Point point, MotionEvent e) {
        try {
            int max = 40;
            int x = point.x;
            int y = point.y;
            float xU = Math.abs(x - e.getRawX());
            float yU = Math.abs(y - e.getRawY());
            return xU > max || yU > max;
        } catch (Exception ex) {
            Log.e(TAG, "Error in isMove: " + ex.getMessage());
            return false;
        }
    }

    private void isDownAndUp(Point point, MotionEvent e) {
        try {
            int min = 10;
            int y = point.y;
            float yU = y - e.getRawY();
            if (Math.abs(yU) > min) {
                try {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showFloatButton(yU < 0);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error showing/hiding float button: " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error in isDownAndUp: " + ex.getMessage());
        }
    }

    private void onItemMove(int fromPosition, int toPosition) {
        try {
            List<AppInfo> items = mAdapterAppList.getItems();
            if (fromPosition < 0 || toPosition < 0 || fromPosition >= items.size() || toPosition >= items.size()) {
                Log.w(TAG, "Invalid positions for move: from=" + fromPosition + ", to=" + toPosition + ", size=" + items.size());
                return;
            }
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    try { Collections.swap(items, i, i + 1); } catch (Exception e) { Log.e(TAG, "Error swapping items at " + i + ": " + e.getMessage()); return; }
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    try { Collections.swap(items, i, i - 1); } catch (Exception e) { Log.e(TAG, "Error swapping items at " + i + ": " + e.getMessage()); return; }
                }
            }
            try {
                mAdapterAppList.notifyItemMoved(fromPosition, toPosition);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying item moved: " + e.getMessage());
                mAdapterAppList.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onItemMove: " + e.getMessage());
        }
    }

    private void setOnLongClick() {
        try {
            mAdapterAppList.setItemLongClickListener((view, data, pos) -> {
                try {
                    popupMenu = new PopupMenu(requireContext(), view);
                    popupMenu.inflate(R.menu.app_menu);
                    popupMenu.setOnMenuItemClickListener(item -> {
                        try {
                            int id = item.getItemId();
                            if (id == R.id.app_remove) {
                                if (data.isXpModule()) {
                                    ToastEx.toast(R.string.uninstall_module_toast);
                                } else {
                                    unInstallApk(data);
                                }
                            } else if (id == R.id.app_clear) {
                                clearApk(data);
                            } else if (id == R.id.app_stop) {
                                stopApk(data);
                            } else if (id == R.id.app_shortcut) {
                                top.niunaijun.blackboxa.util.ShortcutUtil.createShortcut(requireContext(), userID, data);
                            }
                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, "Error in menu item click: " + e.getMessage());
                            return false;
                        }
                    });
                    popupMenu.show();
                } catch (Exception e) {
                    Log.e(TAG, "Error in long click: " + e.getMessage());
                }
                return null;
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setOnLongClick: " + e.getMessage());
        }
    }

    private void initData() {
        try {
            viewBinding.stateView.showLoading();
            viewModel.getInstalledApps(userID);
            // 更新后的监听
            viewModel.getAppsLiveData().observe(getViewLifecycleOwner(), it -> {
                Log.e(TAG,"更新列表的监听: "+it.size());
                try {
                    if (it != null) {
                        // 重新设置列表
                        mAdapterAppList.setItems(it, true, true);
                        if (it.isEmpty()) {
                            viewBinding.stateView.showEmpty();
                        } else {
                            viewBinding.stateView.showContent();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error observing apps data: " + e.getMessage());
                }
            });
            // 发生变化时的更新
            viewModel.getResultLiveData().observe(getViewLifecycleOwner(), it -> {
                try {
                    if (!TextUtils.isEmpty(it)) {
                        hideLoading();
                        ToastEx.toast(it);
                        viewModel.getInstalledApps(userID);
                        scanUser();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error observing result data: " + e.getMessage());
                }
            });
            // 启动监听
            viewModel.getLaunchLiveData().observe(getViewLifecycleOwner(), ok -> {
                try {
                    if (ok != null) {
                        hideLoading();
                        if (!ok) {
                            ToastEx.toast(R.string.start_fail);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error observing launch data: " + e.getMessage());
                }
            });
            // 刷新
            viewModel.getUpdateSortLiveData().observe(getViewLifecycleOwner(), flag -> {
                try {
                    if (mAdapterAppList != null) {
                        viewModel.updateApkOrder(userID, mAdapterAppList.getItems());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error observing sort data: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in initData: " + e.getMessage());
        }
    }

    /**
     * 安装一个apk
     * @param source
     */
    public void installApk(@NonNull String source) {
        try {
            showLoading();
            viewModel.install(source, userID);
        } catch (Exception e) {
            Log.e(TAG, "Error installing APK: " + e.getMessage());
            hideLoading();
        }
    }

    /**
     * 删除一个APK
     * @param info
     */
    private void unInstallApk(@NonNull AppInfo info) {
        try {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.uninstall_app))
                    .setMessage(getString(R.string.uninstall_app_hint, info.getName()))
                    .setPositiveButton(R.string.done, (d,w) -> {
                        try {
                            showLoading();
                            viewModel.unInstall(info.getPackageName(), userID);
                        } catch (Exception e) {
                            Log.e(TAG, "Error uninstalling app: " + e.getMessage());
                            hideLoading();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing uninstall dialog: " + e.getMessage());
        }
    }

    /**
     * 停止运行
     * @param info
     */
    private void stopApk(@NonNull AppInfo info) {
        try {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.app_stop)
                    .setMessage(getString(R.string.app_stop_hint, info.getName()))
                    .setPositiveButton(R.string.done, (d, w) -> {
                        try {
                            BlackBoxCore.get().stopPackage(info.getPackageName(), userID);
                            ToastEx.toast(getString(R.string.is_stop, info.getName()));
                        } catch (Exception e) {
                            Log.e(TAG, "Error stopping app: " + e.getMessage());
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing stop dialog: " + e.getMessage());
        }
    }

    /**
     * 清除数据
     * @param info
     */
    private void clearApk(@NonNull AppInfo info) {
        try {
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.app_clear)
                    .setMessage(getString(R.string.app_clear_hint, info.getName()))
                    .setPositiveButton(R.string.done, (d, w) -> {
                        try {
                            showLoading();
                            viewModel.clearApkData(info.getPackageName(), userID);
                        } catch (Exception e) {
                            Log.e(TAG, "Error clearing app data: " + e.getMessage());
                            hideLoading();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing clear dialog: " + e.getMessage());
        }
    }

    /**
     * 更新主界面Activity
     */
    private void scanUser() {
        try {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).scanUser();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning user: " + e.getMessage());
        }
    }

    private void showLoading() {
//        Log.e(TAG,"hideLoading()", new RuntimeException());
        try {
            if (getActivity() instanceof LoadingActivity) {
                ((LoadingActivity) getActivity()).showLoading();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading: " + e.getMessage());
        }
    }

    private void hideLoading() {
//        Log.e(TAG,"hideLoading()", new RuntimeException());
        try {
            if (getActivity() instanceof LoadingActivity) {
                ((LoadingActivity) getActivity()).hideLoading();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding loading: " + e.getMessage());
        }
    }

    public int getUserID() {
        return userID;
    }
}
