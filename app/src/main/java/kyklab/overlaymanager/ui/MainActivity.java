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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
    private int removeIndex;
    private List<RvItem> mOverlaysList;
    private Set<Integer> mSelectedIndexes;
    private View mBackgroundBlocker;
    private CoordinatorLayout mCoordinatorLayout;
    private boolean mIsAllChecked;
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
        mSelectedIndexes = new TreeSet<>();
        mRefreshListTask = null;
        mToggleOverlayTask = null;
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
                }
                return true;
            }
        });
    }

    private void toggleSelectedOverlays(@Nullable Boolean newState) {
        toggleOverlays(mSelectedIndexes, newState, true);
    }

    @Override
    public void toggleOverlays(Set<Integer> indexes, @Nullable Boolean newState, boolean resetCheckState) {
        if (indexes.isEmpty()) {
            Snackbar.make(mCoordinatorLayout,
                    R.string.nothing_selected,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, this)
                    .show();
            return;
        }

        if (Utils.isTaskExecutable(mToggleOverlayTask)) {
            mToggleOverlayTask = new ToggleOverlayTask(this, indexes, newState, resetCheckState);
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
        if (mIsAllChecked) {
            mSelectedIndexes.clear();
        }

        // Set new checked state for all items
        // and add to selected indexes set if we're in 'check all' mode
        Bundle b = new Bundle();
        b.putBoolean(OverlayItem.Payload.CHECKED_STATE, !mIsAllChecked);
        for (int i = 0; i < mOverlaysList.size(); ++i) {
            RvItem item = mOverlaysList.get(i);
            if (item.getItemType() == RvItem.TYPE_OVERLAY) {
                // This updates only the items currently visible on screen,
                // so we need to block the checkbox listener temporarily
                // and change its checked state only.
                mAdapter.notifyItemChanged(i, b);
                if (!mIsAllChecked) {
                    mSelectedIndexes.add(i);
                }
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
    public boolean isChecked(int index) {
        return mSelectedIndexes.contains(index);
    }

    @Override
    public void setChecked(int index, boolean checked) {
        if (checked) {
            mSelectedIndexes.add(index);
        } else {
            mSelectedIndexes.remove(index);
            mIsAllChecked = false;
        }
    }

    @Override
    public void uninstallPackageIndex(int index) {
        removeIndex = index;
        String packageName = mOverlaysList.get(index).getPackageName();
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, REQ_CODE_UNINSTALL_PACKAGE);
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
                if (resultCode == -1) { // Success
                    mOverlaysList.remove(removeIndex);
                    mAdapter.notifyItemRemoved(removeIndex);

                    // Check if it was the only overlay within its category
                    if (mOverlaysList.get(removeIndex - 1).getItemType() == RvItem.TYPE_TARGET
                            && (removeIndex >= mOverlaysList.size() ||
                            mOverlaysList.get(removeIndex).getItemType() == RvItem.TYPE_TARGET)) {
                        mOverlaysList.remove(removeIndex - 1);
                        mAdapter.notifyItemRemoved(removeIndex - 1);
                    }
                }
                break;
        }
    }

    private static class RefreshListTask extends AsyncTask<Void, Integer, Void> {
        private final WeakReference<MainActivity> activityWeakReference;

        RefreshListTask(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.blockScreen();
            activity.showProgressBar();
            activity.mAdapter.notifyItemRangeRemoved(0, activity.mOverlaysList.size());
            activity.mSelectedIndexes.clear();
            activity.mIsAllChecked = false;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            List<RvItem> list = activity.mOverlaysList;
            list.clear();

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

            int adapterPosition = 0;
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

                list.add(new TargetItem(targetAppName, targetPackageName, targetAppIcon, targetHasAppName));
                publishProgress(adapterPosition++);
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

                    list.add(new OverlayItem(overlayHasAppName ? overlayAppName : null, // Store app name only when it exists
                            overlayPackageName, overlayAppIcon, overlayHasAppName, overlayEnabled));
                    publishProgress(adapterPosition++);
                }
                // ===== Done fetching category item =====
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.mAdapter.notifyItemChanged(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.releaseScreen();
            activity.hideProgressBar();
        }
    }

    private static class ToggleOverlayTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<MainActivity> activityWeakReference;
        private final Set<Integer> indexes;
        private final Boolean newState;
        private final boolean resetCheckState;

        ToggleOverlayTask(MainActivity activity, Set<Integer> indexes, @Nullable Boolean newState,
                          boolean resetCheckState) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.indexes = indexes;
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

            // Get selected overlays
            List<OverlayItem> selectedOverlays = new ArrayList<>();
            for (int i : indexes) {
                selectedOverlays.add((OverlayItem) activity.mOverlaysList.get(i));
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
            for (int i : indexes) {
                // Notify each changed item of its new state
                Bundle b = new Bundle();
                if (resetCheckState) {
                    b.putBoolean(OverlayItem.Payload.CHECKED_STATE, false);
                }
                b.putBoolean(OverlayItem.Payload.ENABLED_STATE,
                        ((OverlayItem) activity.mOverlaysList.get(i)).isEnabled());
                activity.mAdapter.notifyItemChanged(i, b);
            }

            Snackbar.make(activity.mCoordinatorLayout,
                    R.string.selected_toggle_complete,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, activity)
                    .show();
        }
    }
}
