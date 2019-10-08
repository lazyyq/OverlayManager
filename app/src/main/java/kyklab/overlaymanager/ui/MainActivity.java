package kyklab.overlaymanager.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kyklab.overlaymanager.App;
import kyklab.overlaymanager.R;
import kyklab.overlaymanager.overlay.OverlayAdapter;
import kyklab.overlaymanager.overlay.OverlayInterface;
import kyklab.overlaymanager.overlay.OverlayItem;
import kyklab.overlaymanager.overlay.RvItem;
import kyklab.overlaymanager.overlay.TargetItem;
import kyklab.overlaymanager.utils.AppUtils;
import kyklab.overlaymanager.utils.OverlayUtils;
import kyklab.overlaymanager.utils.ThemeManager;
import kyklab.overlaymanager.utils.Utils;
import projekt.andromeda.client.AndromedaOverlayManager;
import projekt.andromeda.client.util.OverlayInfo;

public class MainActivity extends AppCompatActivity
        implements OverlayInterface, View.OnClickListener {
    private static final int REQ_CODE_UNINSTALL_PACKAGE = 10000;
    private static final String TAG = "OVERLAY_MANAGER";
    private List<RvItem> mOverlaysList;
    private List<OverlayItem> mRemoveList;
    private View mBackgroundBlocker;
    private CoordinatorLayout mCoordinatorLayout;
    private boolean mIsAllChecked;
    private boolean mBatchUninstallMode;
    private OverlayAdapter mAdapter;
    private RefreshListTask mRefreshListTask;
    private ToggleOverlayTask mToggleOverlayTask;
    private ProgressBar mProgressBar;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOverlaysList = new ArrayList<>();
        mRemoveList = new ArrayList<>();
        mRefreshListTask = null;
        mToggleOverlayTask = null;
        mBatchUninstallMode = false;
        mCoordinatorLayout = findViewById(R.id.coordinatorLayout);
        mProgressBar = findViewById(R.id.progressBar);
        mBackgroundBlocker = findViewById(R.id.backgroundBlocker);

        setupFab();
        initRefreshLayout();
        setRecyclerView();
        updateOverlayList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.taskNeedsResume(mRefreshListTask)) {
            updateOverlayList();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Utils.isTaskRunning(mRefreshListTask)) {
            mRefreshListTask.cancel(true);
        }
        if (Utils.isTaskRunning(mToggleOverlayTask)) {
            mToggleOverlayTask.cancel(true);
        }
    }

    private void setupFab() {
        final SpeedDialView fab = findViewById(R.id.fab);

        fab.addActionItem(
                new SpeedDialActionItem
                        .Builder(R.id.fab_uninstall, R.drawable.ic_delete_forever_white_24dp)
                        .setFabBackgroundColor(Color.WHITE)
                        .setFabImageTintColor(Color.BLACK)
                        .setLabel(R.string.fab_uninstall)
                        .create());
        fab.addActionItem(
                new SpeedDialActionItem
                        .Builder(R.id.fab_disable, R.drawable.ic_clear_white_24dp)
                        .setFabBackgroundColor(Color.WHITE)
                        .setFabImageTintColor(Color.BLACK)
                        .setLabel(R.string.fab_disable)
                        .create());
        fab.addActionItem(
                new SpeedDialActionItem
                        .Builder(R.id.fab_enable, R.drawable.ic_done_white_24dp)
                        .setFabBackgroundColor(Color.WHITE)
                        .setFabImageTintColor(Color.BLACK)
                        .setLabel(R.string.fab_enable)
                        .create());
        fab.addActionItem(
                new SpeedDialActionItem
                        .Builder(R.id.fab_toggle, R.drawable.ic_cached_white_24dp)
                        .setFabBackgroundColor(Color.WHITE)
                        .setFabImageTintColor(Color.BLACK)
                        .setLabel(R.string.fab_toggle)
                        .create());

        fab.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                fab.close();
                switch (actionItem.getId()) {
                    case R.id.fab_disable:
                        toggleSelectedOverlays(false);
                        break;
                    case R.id.fab_enable:
                        toggleSelectedOverlays(true);
                        break;
                    case R.id.fab_toggle:
                        toggleSelectedOverlays(null);
                        break;
                    case R.id.fab_uninstall:
                        uninstallSelectedOverlays();
                        break;
                }
                return true;
            }
        });
    }

    private List<OverlayItem> getSelectedOverlays() {
        List<OverlayItem> list = new ArrayList<>();
        for (RvItem item : mOverlaysList) {
            if (item.getItemType() == RvItem.TYPE_OVERLAY && ((OverlayItem) item).isChecked()) {
                list.add((OverlayItem) item);
            }
        }
        return list;
    }

    private void toggleSelectedOverlays(@Nullable Boolean newState) {
        toggleOverlays(getSelectedOverlays(), newState, true);
    }

    @Override
    public void toggleOverlays(List<OverlayItem> selectedOverlays, @Nullable Boolean newState,
                               boolean resetCheckState) {
        if (selectedOverlays.isEmpty()) {
            Snackbar.make(mCoordinatorLayout,
                    R.string.nothing_selected,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, this)
                    .show();
            return;
        }

        if (Utils.isTaskExecutable(mToggleOverlayTask)) {
            mToggleOverlayTask = new ToggleOverlayTask(
                    this, selectedOverlays, newState, resetCheckState);
            mToggleOverlayTask.execute();
        }
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem switchTheme = menu.findItem(R.id.action_switch_theme);
        int theme = ThemeManager.getCurrentTheme();
        switch (theme) {
            case ThemeManager.THEME_AUTO:
                switchTheme.setIcon(R.drawable.ic_brightness_7_24dp);
                break;
            case ThemeManager.THEME_LIGHT:
                switchTheme.setIcon(R.drawable.ic_brightness_3_24dp);
                break;
            case ThemeManager.THEME_DARK:
            default:
                switchTheme.setIcon(R.drawable.ic_brightness_auto_black_24dp);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_check_all:
                toggleCheckAllOverlays();
                break;
            case R.id.action_switch_theme:
                switchTheme();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleCheckAllOverlays() {
        // Set new checked state for all items
        // and add to selected indexes set if we're in 'check all' mode
        Bundle b = new Bundle();
        b.putBoolean(OverlayItem.Payload.CHECKED, !mIsAllChecked);
        for (int i = 0; i < mOverlaysList.size(); ++i) {
            RvItem item = mOverlaysList.get(i);
            if (item.getItemType() == RvItem.TYPE_OVERLAY) {
                // This updates only the items currently visible on screen,
                // so we need to block the checkbox listener temporarily
                // and change its checked state only.
                mAdapter.notifyItemChanged(i, b);
                ((OverlayItem) item).setChecked(!mIsAllChecked);
            }
        }

        mIsAllChecked = !mIsAllChecked;
    }

    private void switchTheme() {
        ThemeManager.getInstance().switchTheme();
        invalidateOptionsMenu();
    }

    private void initRefreshLayout() {
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateOverlayList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void updateOverlayList() {
        if (Utils.isTaskExecutable(mRefreshListTask)) {
            mRefreshListTask = new RefreshListTask(this);
            mRefreshListTask.execute();
        }
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new OverlayAdapter(this, this, mOverlaysList);
        recyclerView.setAdapter(mAdapter);
    }

    private void blockScreen() {
        mBackgroundBlocker.setVisibility(View.VISIBLE);
    }

    private void releaseScreen() {
        mBackgroundBlocker.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setAllChecked(boolean isAllChecked) {
        this.mIsAllChecked = isAllChecked;
    }

    private void uninstallSelectedOverlays() {
        List<OverlayItem> selectedOverlays = getSelectedOverlays();
        if (selectedOverlays.isEmpty()) {
            Snackbar.make(mCoordinatorLayout,
                    R.string.nothing_selected,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, this)
                    .show();
            return;
        }

        mBatchUninstallMode = true;
        mRemoveList.addAll(selectedOverlays);
        // Trigger uninstall for the first item,
        // the rest will keep being processed in onActivityResult().
        uninstallOverlay(mRemoveList.get(0));
    }

    @Override
    public void uninstallOverlay(OverlayItem overlay) {
        if (!mBatchUninstallMode) {
            mRemoveList.add(overlay);
        }
        String packageName = overlay.getPackageName();
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, REQ_CODE_UNINSTALL_PACKAGE);
    }

    private void removeOverlayFromList(OverlayItem overlay) {
        int position = mOverlaysList.indexOf(overlay);
        mOverlaysList.remove(position);
        mAdapter.notifyItemRemoved(position);

        // Check if it was the only overlay within its category
        if (mOverlaysList.get(position - 1).getItemType() == RvItem.TYPE_TARGET
                && (position >= mOverlaysList.size() ||
                mOverlaysList.get(position).getItemType() == RvItem.TYPE_TARGET)) {
            mOverlaysList.remove(position - 1);
            mAdapter.notifyItemRemoved(position - 1);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_UNINSTALL_PACKAGE:
                if (mRemoveList.isEmpty()) {
                    return;
                }

                if (resultCode == -1) {
                    // Success, remove from list
                    removeOverlayFromList(mRemoveList.get(0));
                } else {
                    // Aborted or failed, reset checked state only if we're in batch uninstall mode
                    if (mBatchUninstallMode) {
                        mRemoveList.get(0).setChecked(false);
                        int position = mOverlaysList.indexOf(mRemoveList.get(0));
                        Bundle b = new Bundle();
                        b.putBoolean(OverlayItem.Payload.CHECKED, false);
                        mAdapter.notifyItemChanged(position, b);
                    }
                }
                mRemoveList.remove(0);
                if (mBatchUninstallMode) {
                    if (!mRemoveList.isEmpty()) {
                        // Uninstall next item, and we'll return to this function again
                        uninstallOverlay(mRemoveList.get(0));
                    } else {
                        mBatchUninstallMode = false;
                    }
                }
                break;
        }
    }

    private static class RefreshListTask extends AsyncTask<Void, Integer, Void> {
        private final WeakReference<MainActivity> activityWeakReference;
        private final List<RvItem> newList;

        RefreshListTask(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.newList = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.blockScreen();
            activity.showProgressBar();
            activity.mIsAllChecked = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            Map<String, List<OverlayInfo>> map = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    try {
                        return AppUtils.getApplicationName(activity, o1)
                                .compareTo(AppUtils.getApplicationName(activity, o2));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });
            map.putAll(AndromedaOverlayManager.INSTANCE.getAllOverlay());

            for (Map.Entry<String, List<OverlayInfo>> entry : map.entrySet()) {
                // ===== Start fetching target item =====
                final String targetPackageName = entry.getKey();
                final String targetAppName;
                final Drawable targetAppIcon;
                try {
                    targetAppName = AppUtils.getApplicationName(App.getContext(), targetPackageName);
                    targetAppIcon = AppUtils.getApplicationIcon(App.getContext(), targetPackageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error while loading category " + targetPackageName);
                    continue;
                }
                final boolean targetHasAppName = !TextUtils.equals(targetAppName, targetPackageName);

                newList.add(new TargetItem(targetAppName, targetPackageName, targetAppIcon, targetHasAppName));
                // ===== Done fetching target item =====

                // ===== Start fetching overlay item =====
                for (OverlayInfo overlay : entry.getValue()) {
                    final String overlayPackageName = overlay.getPackageName(); // Package name
                    final String overlayAppName;
                    final Drawable overlayAppIcon;
                    try {
                        overlayAppName = AppUtils.getApplicationName(App.getContext(), overlayPackageName); // App name
                        overlayAppIcon = AppUtils.getApplicationIcon(App.getContext(), overlayPackageName); // App icon
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error while loading overlay " + overlayPackageName);
                        continue;
                    }
                    final boolean overlayEnabled = overlay.isEnabled(); // Enabled
                    final boolean overlayHasAppName = !TextUtils.equals(overlayAppName, overlayPackageName); // Has its own app name

                    newList.add(new OverlayItem(overlayHasAppName ? overlayAppName : null, // Store app name only when it exists
                            overlayPackageName, overlayAppIcon, overlayHasAppName, overlayEnabled));
                }
                // ===== Done fetching category item =====
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.mAdapter.update(newList);
            activity.releaseScreen();
            activity.hideProgressBar();
        }
    }

    private static class ToggleOverlayTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<MainActivity> activityWeakReference;

        /**
         * List of overlays to toggle.
         */
        private final List<OverlayItem> selectedOverlays;

        /**
         * New state of selected overlays, setting this to null will swap their states.
         */
        private final Boolean newState;

        /**
         * Whether to reset checked state of selected overlays,
         * usually set to false for use on changing state through switch.
         */
        private final boolean resetCheckState;

        ToggleOverlayTask(MainActivity activity, List<OverlayItem> selectedOverlays,
                          @Nullable Boolean newState, boolean resetCheckState) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.selectedOverlays = selectedOverlays;
            this.newState = newState;
            this.resetCheckState = resetCheckState;
        }

        @Override
        protected void onPreExecute() {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.blockScreen();
            activity.showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            if (newState == null) {
                // Toggle overlays' states, enabled -> disabled, disabled -> enabled
                OverlayUtils.toggleOverlays(selectedOverlays);
                for (OverlayItem overlay : selectedOverlays) {
                    overlay.setEnabled(!overlay.isEnabled());
                }
            } else {
                // Enable or disable overlays
                OverlayUtils.toggleOverlays(selectedOverlays, newState);
                for (OverlayItem overlay : selectedOverlays) {
                    overlay.setEnabled(newState);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.releaseScreen();
            activity.hideProgressBar();
            for (OverlayItem overlay : selectedOverlays) {
                int position = activity.mOverlaysList.indexOf(overlay);
                // Notify each changed item of its new state
                Bundle b = new Bundle();
                if (resetCheckState) {
                    b.putBoolean(OverlayItem.Payload.CHECKED, false);
                    overlay.setChecked(false);
                }
                b.putBoolean(OverlayItem.Payload.ENABLED, overlay.isEnabled());
                activity.mAdapter.notifyItemChanged(position, b);
            }

            Snackbar.make(activity.mCoordinatorLayout,
                    R.string.selected_toggle_complete,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, activity)
                    .show();
        }
    }
}
