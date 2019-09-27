package kyklab.overlaymanager.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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
import kyklab.overlaymanager.utils.ViewUtils;
import projekt.andromeda.client.AndromedaOverlayManager;
import projekt.andromeda.client.util.OverlayInfo;

public class MainActivity extends AppCompatActivity
        implements OverlayInterface, View.OnClickListener, View.OnTouchListener {
    private static final int REQ_CODE_REMOVE_APP = 10000;
    private static final long MINI_FAB_ANIM_LENGTH = 300L;
    private static final long MINI_FAB_ANIM_DELAY = 100L;
    private static final String TAG = "OVERLAY_MANAGER";
    private final List<RvItem> mList = new ArrayList<>();
    private float mMiniFabTransitionDistance;
    private FloatingActionButton[] mMiniFab;
    private CardView[] mFabText;
    private View mFabBackground;
    private View mBackgroundBlocker;
    private CoordinatorLayout mCoordinatorLayout;
    private boolean mIsAllChecked;
    private OverlayAdapter mAdapter;
    private UpdateTask mUpdateTask = null;
    private ToggleTask mToggleTask = null;
    private ProgressBar mProgressBar;
    private FloatingActionButton mFab;
    //private String removedApp;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        if (Utils.taskNeedsResume(mUpdateTask)) {
            updateOverlayList();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Utils.isTaskRunning(mUpdateTask)) {
            mUpdateTask.cancel(true);
        }
        if (Utils.isTaskRunning(mToggleTask)) {
            mToggleTask.cancel(true);
        }
    }

    private void setupFab() {
        float fabSizeNormal = getResources().getDimension(R.dimen.fab_size_normal);
        mMiniFabTransitionDistance = getResources().getDimension(R.dimen.mini_fab_transition_distance);

        ConstraintLayout miniFabContainer = findViewById(R.id.miniFabContainer);
        miniFabContainer.setPadding(
                0, 0, 0, (int) (fabSizeNormal - mMiniFabTransitionDistance));

        mFabText = new CardView[]{
                findViewById(R.id.fabTextCardToggle), findViewById(R.id.fabTextCardEnable), findViewById(R.id.fabTextCardDisable)
        };
        mMiniFab = new FloatingActionButton[]{
                findViewById(R.id.miniFabToggle), findViewById(R.id.miniFabEnable), findViewById(R.id.miniFabDisable)
        };

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(this);
        for (FloatingActionButton miniFab : mMiniFab) {
            miniFab.setOnClickListener(this);
        }

        mFabBackground = findViewById(R.id.fabBackground);
        mFabBackground.setOnTouchListener(this);
    }

    private List<OverlayItem> getSelectedOverlays() {
        List<OverlayItem> newList = new ArrayList<>();
        OverlayItem overlay;
        for (RvItem item : mList) {
            if (item.getItemType() == RvItem.TYPE_OVERLAY) {
                overlay = (OverlayItem) item;
                if (overlay.isItemChecked()) {
                    newList.add(overlay);
                }
            }
        }
        return newList;
    }

    private void toggleSelectedOverlays() {
        List<OverlayItem> list = getSelectedOverlays();
        toggleOverlays(list);
    }

    private void toggleSelectedOverlays(boolean state) {
        List<OverlayItem> list = getSelectedOverlays();
        toggleOverlays(list, state);
    }

    @Override
    public void toggleOverlays(List<OverlayItem> list) {
        if (list.isEmpty()) {
            Snackbar.make(mCoordinatorLayout,
                    R.string.nothing_selected,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, this)
                    .show();
            return;
        }

        if (Utils.isTaskExecutable(mToggleTask)) {
            mToggleTask = new ToggleTask(this, list, null);
            mToggleTask.execute();
        }
    }

    @Override
    public void toggleOverlays(List<OverlayItem> list, boolean state) {
        if (list.isEmpty()) {
            Snackbar.make(mCoordinatorLayout,
                    R.string.nothing_selected,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, this)
                    .show();
            return;
        }

        if (Utils.isTaskExecutable(mToggleTask)) {
            mToggleTask = new ToggleTask(this, list, state);
            mToggleTask.execute();
        }
    }

    @SuppressLint("RestrictedApi")
    private void expandFab() {
        mFabBackground.setVisibility(View.VISIBLE);
        ViewUtils.animateShowInOrder(
                mMiniFab, 0, -mMiniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        ViewUtils.animateShowInOrder(
                mFabText, 0, -mMiniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        mFab.setExpanded(true);
    }

    @SuppressLint("RestrictedApi")
    private void collapseFab() {
        mFabBackground.setVisibility(View.GONE);
        ViewUtils.animateHideInOrder(
                mMiniFab, 0, mMiniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        ViewUtils.animateHideInOrder(
                mFabText, 0, mMiniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        mFab.setExpanded(false);
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
        for (RvItem item : mList) {
            if (item.getItemType() == RvItem.TYPE_OVERLAY) {
                ((OverlayItem) item).setItemChecked(!mIsAllChecked);
            }
        }
        mIsAllChecked = !mIsAllChecked;
        mAdapter.notifyDataSetChanged();
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
        if (Utils.isTaskExecutable(mUpdateTask)) {
            mUpdateTask = new UpdateTask(this);
            mUpdateTask.execute();
        }
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new OverlayAdapter(this, this, mList);
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
    public boolean isAllChecked() {
        return mIsAllChecked;
    }

    @Override
    public void setAllChecked(boolean isAllChecked) {
        this.mIsAllChecked = isAllChecked;
    }

    @Override
    public void removeAppFromList(String packageName) {
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
        //removedApp = packageName;
        startActivityForResult(intent, REQ_CODE_REMOVE_APP);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab:
                if (mFab.isExpanded()) {
                    collapseFab();
                } else {
                    expandFab();
                }
                break;
            case R.id.miniFabToggle:
                collapseFab();
                toggleSelectedOverlays();
                break;
            case R.id.miniFabEnable:
                collapseFab();
                toggleSelectedOverlays(true);
                break;
            case R.id.miniFabDisable:
                collapseFab();
                toggleSelectedOverlays(false);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();
        switch (id) {
            case R.id.fabBackground:
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    collapseFab();
                }
                view.performClick();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_REMOVE_APP) {
            //if (!AppUtils.overlayExists(removedApp)) {
            // The function is unreliable at the moment,
            // so let's just assume it was successfully removed anyways.
            // App was successfully removed
            updateOverlayList();
            //}
        }
    }

    private static class UpdateTask extends AsyncTask<Void, Integer, Void> {
        private final WeakReference<MainActivity> activityWeakReference;

        UpdateTask(MainActivity activity) {
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
            activity.mAdapter.notifyItemRangeRemoved(0, activity.mList.size());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            List<RvItem> list = activity.mList;
            list.clear();

            String appName;
            Drawable icon;
            boolean enabled;
            String packageName = null;
            String targetAppName;
            String targetPackageName;
            boolean hasAppName;

            int adapterPosition = 0;

            Comparator<String> comparator = new Comparator<String>() {
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
            };
            Map<String, List<OverlayInfo>> map = new TreeMap<>(comparator);
            map.putAll(AndromedaOverlayManager.INSTANCE.getAllOverlay());

            for (Map.Entry<String, List<OverlayInfo>> entry : map.entrySet()) {
                targetPackageName = entry.getKey();
                try {
                    targetAppName = AppUtils.getApplicationName(App.getContext(), targetPackageName);
                    icon = AppUtils.getApplicationIcon(App.getContext(), targetPackageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error while loading category " + packageName);
                    continue;
                }
                hasAppName = !TextUtils.equals(targetAppName, packageName);

                list.add(new TargetItem(targetAppName, targetPackageName, icon, hasAppName));
                publishProgress(adapterPosition++);

                for (OverlayInfo overlay : entry.getValue()) {
                    try {
                        packageName = overlay.getPackageName(); // Package name
                        appName = AppUtils.getApplicationName(App.getContext(), packageName); // App name
                        icon = AppUtils.getApplicationIcon(App.getContext(), packageName); // App icon
                        enabled = overlay.isEnabled(); // Enabled
                        hasAppName = !TextUtils.equals(appName, packageName); // Has its own app name

                        list.add(new OverlayItem(hasAppName ? appName : null, // Store app name only when it exists
                                packageName, icon, hasAppName, enabled));
                        publishProgress(adapterPosition++);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error while loading overlay " + packageName);
                    }
                }
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

    private static class ToggleTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<MainActivity> activityWeakReference;
        private final List<OverlayItem> list;
        private final Boolean state;

        ToggleTask(MainActivity activity, List<OverlayItem> list, Boolean state) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.list = list;
            this.state = state;
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

            if (state == null) {
                // Toggle overlays' states, enabled -> disabled, disabled -> enabled
                OverlayUtils.toggleOverlays(list);
            } else {
                // Enable or disable overlays
                OverlayUtils.toggleOverlays(list, state);
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
            activity.updateOverlayList();
            Snackbar.make(activity.mCoordinatorLayout,
                    R.string.selected_toggle_complete,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, activity)
                    .show();
        }
    }
}
