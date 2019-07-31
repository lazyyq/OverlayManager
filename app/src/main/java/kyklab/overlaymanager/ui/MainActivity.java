package kyklab.overlaymanager.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import java.util.List;
import java.util.Map;

import kyklab.overlaymanager.R;
import kyklab.overlaymanager.overlay.OverlayAdapter;
import kyklab.overlaymanager.overlay.OverlayInterface;
import kyklab.overlaymanager.overlay.OverlayItem;
import kyklab.overlaymanager.utils.AppUtils;
import kyklab.overlaymanager.utils.ViewUtils;
import projekt.andromeda.client.AndromedaOverlayManager;
import projekt.andromeda.client.util.OverlayInfo;

public class MainActivity extends AppCompatActivity implements OverlayInterface {
    private static final long MINI_FAB_ANIM_LENGTH = 300L;
    private static final long MINI_FAB_ANIM_DELAY = 100L;
    private static final String TAG = "OVERLAY_MANAGER";
    private final ArrayList<OverlayItem> overlayList = new ArrayList<>();
    private float miniFabTransitionDistance;
    private FloatingActionButton[] miniFab;
    private CardView[] fabText;
    private View fabBackground;
    private CoordinatorLayout coordinatorLayout;
    private boolean isAllChecked;
    private OverlayAdapter adapter;
    private UpdateTask updateTask;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private int nightMode;
    private int newNightMode;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        progressBar = findViewById(R.id.progressBar);

        setupFab();
        initRefreshLayout();
        updateOverlayList();
        setRecyclerView();
    }

    private void setupFab() {
        float fabSizeNormal = getResources().getDimension(R.dimen.fab_size_normal);
        miniFabTransitionDistance = getResources().getDimension(R.dimen.mini_fab_transition_distance);

        ConstraintLayout miniFabContainer = findViewById(R.id.miniFabContainer);
        miniFabContainer.setPadding(
                0, 0, 0, (int) (fabSizeNormal - miniFabTransitionDistance));

        fabText = new CardView[]{
                findViewById(R.id.fabTextCardToggle), findViewById(R.id.fabTextCardEnable), findViewById(R.id.fabTextCardDisable)
        };
        miniFab = new FloatingActionButton[]{
                findViewById(R.id.miniFabToggle), findViewById(R.id.miniFabEnable), findViewById(R.id.miniFabDisable)
        };

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fab.isExpanded()) {
                    collapseFab();
                } else {
                    expandFab();
                }
            }
        });
        miniFab[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseFab();
                toggleSelectedOverlays();
            }
        });
        miniFab[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseFab();
                toggleSelectedOverlays(true);
            }
        });
        miniFab[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                collapseFab();
                toggleSelectedOverlays(false);
            }
        });

        fabBackground = findViewById(R.id.fabBackground);
        fabBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    collapseFab();
                }
                view.performClick();
                return true;
            }
        });
    }

    @Override
    public void toggleSelectedOverlays() {
        List<Integer> indexes = getSelectedOverlayIndex();
        toggleOverlays(indexes);
    }

    @Override
    public void toggleSelectedOverlays(boolean newState) {
        List<Integer> indexes = getSelectedOverlayIndex();
        toggleOverlays(indexes, newState);
    }

    @Override
    public void toggleOverlays(List<Integer> indexes) {
        if (indexes.isEmpty()) {
            Snackbar.make(coordinatorLayout,
                    R.string.nothing_selected,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    })
                    .show();
            return;
        }
        ToggleOverlayThread thread = new ToggleOverlayThread(this, indexes);
        thread.start();
    }

    @Override
    public void toggleOverlays(List<Integer> indexes, boolean newState) {
        if (indexes.isEmpty()) {
            Snackbar.make(coordinatorLayout,
                    R.string.nothing_selected,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    })
                    .show();
            return;
        }
        ToggleOverlayThread thread = new ToggleOverlayThread(this, indexes, newState);
        thread.start();
    }

    private List<Integer> getSelectedOverlayIndex() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0, len = overlayList.size(); i < len; ++i) {
            OverlayItem overlay = overlayList.get(i);
            if (overlay.getItemType() == OverlayItem.OVERLAY_ITEM_TYPE_ITEM
                    && overlay.isItemChecked()) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    @SuppressLint("RestrictedApi")
    private void expandFab() {
        fabBackground.setVisibility(View.VISIBLE);
        ViewUtils.animateShowInOrder(
                miniFab, 0, -miniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        ViewUtils.animateShowInOrder(
                fabText, 0, -miniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        fab.setExpanded(true);
    }

    @SuppressLint("RestrictedApi")
    private void collapseFab() {
        fabBackground.setVisibility(View.GONE);
        ViewUtils.animateHideInOrder(
                miniFab, 0, miniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        ViewUtils.animateHideInOrder(
                fabText, 0, miniFabTransitionDistance, MINI_FAB_ANIM_LENGTH, MINI_FAB_ANIM_DELAY
        );
        fab.setExpanded(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        updateTask.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_check_all:
                toggleCheckAllOverlays();
                break;
            case R.id.action_dark_mode:
                nightMode = AppCompatDelegate.getDefaultNightMode();
                Log.e("MODE", nightMode + "");
                if (nightMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
                    newNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    Toast.makeText(this, "MODE_NIGHT_FOLLOW_SYSTEM", Toast.LENGTH_SHORT).show();
                } else if (nightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                    newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    Toast.makeText(this, "MODE_NIGHT_NO", Toast.LENGTH_SHORT).show();
                } else if (nightMode == AppCompatDelegate.MODE_NIGHT_NO) {
                    newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                    Toast.makeText(this, "MODE_NIGHT_YES", Toast.LENGTH_SHORT).show();
                } else if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    newNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    Toast.makeText(this, "MODE_NIGHT_FOLLOW_SYSTEM", Toast.LENGTH_SHORT).show();
                } else
                    break;
                AppCompatDelegate.setDefaultNightMode(newNightMode);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleCheckAllOverlays() {
        for (OverlayItem overlay : overlayList) {
            if (overlay.getItemType() == OverlayItem.OVERLAY_ITEM_TYPE_ITEM) {
                overlay.setItemChecked(!isAllChecked);
            }
        }
        isAllChecked = !isAllChecked;
        adapter.notifyDataSetChanged();
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
        updateTask = new UpdateTask(this);
        updateTask.execute();
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new OverlayAdapter(this, this, overlayList);
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
//                linearLayoutManager.getOrientation()));
    }

    @Override
    public boolean isAllChecked() {
        return isAllChecked;
    }

    @Override
    public void setAllChecked(boolean isAllChecked) {
        this.isAllChecked = isAllChecked;
    }

    private static class UpdateTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<MainActivity> activityWeakReference;

        UpdateTask(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            activityWeakReference.get().progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }

            ArrayList<OverlayItem> tempList = new ArrayList<>();

            String appName;
            Drawable icon;
            boolean enabled;
            String packageName = null;
            String targetAppName;
            String targetPackageName;
            boolean hasAppName;

            Map<String, List<OverlayInfo>> overlayMap = AndromedaOverlayManager.INSTANCE.getAllOverlay();

            for (Map.Entry<String, List<OverlayInfo>> entry : overlayMap.entrySet()) {
                targetPackageName = entry.getKey();
                try {
                    targetAppName = AppUtils.getApplicationName(activity, targetPackageName);
                    icon = AppUtils.getApplicationIcon(activity, targetPackageName);
                    tempList.add(new OverlayItem(targetAppName, icon));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(activity,
                            "Error while loading target " + packageName,
                            Toast.LENGTH_SHORT)
                            .show();
                    continue;
                }

                for (OverlayInfo overlay : entry.getValue()) {
                    try {
                        packageName = overlay.getPackageName(); // Package name
                        appName = AppUtils.getApplicationName(activity, packageName); // App name
                        icon = AppUtils.getApplicationIcon(activity, packageName); // App icon
                        enabled = overlay.isEnabled(); // Enabled
                        hasAppName = !TextUtils.equals(appName, packageName); // Has its own app name

                        tempList.add(
                                new OverlayItem(
                                        hasAppName ? appName : null, // Store app name only when it exists
                                        icon, enabled, packageName, hasAppName)
                        );
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(activity,
                                "Error while loading app " + packageName,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }

            activity.overlayList.clear();
            activity.overlayList.addAll(tempList);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            activity.adapter.notifyDataSetChanged();
            activity.progressBar.setVisibility(View.GONE);
        }
    }

    class ToggleOverlayThread extends Thread {
        private final Activity pActivity;
        private final List<Integer> packageIndex;
        private final boolean toggleMode;
        private final boolean targetState;

        ToggleOverlayThread(Activity pActivity, List<Integer> packageIndex) {
            this.pActivity = pActivity;
            this.packageIndex = packageIndex;
            this.toggleMode = true;
            this.targetState = false;
        }

        ToggleOverlayThread(Activity pActivity, List<Integer> packageIndex, boolean targetState) {
            this.pActivity = pActivity;
            this.packageIndex = packageIndex;
            this.toggleMode = false;
            this.targetState = targetState;
        }

        @Override
        public void run() {
            Log.e(TAG, "Thread run");
            if (!toggleMode) {
                List<String> packages = new ArrayList<>();
                for (int i : packageIndex) {
                    packages.add(overlayList.get(i).getPackageName());
                }
                AndromedaOverlayManager.INSTANCE.switchOverlay(packages, targetState);
            } else {
                List<String> packagesToEnable = new ArrayList<>();
                List<String> packagesToDisable = new ArrayList<>();
                OverlayItem overlayItem;
                for (int i : packageIndex) {
                    overlayItem = overlayList.get(i);
                    if (overlayItem.isEnabled()) {
                        packagesToDisable.add(overlayItem.getPackageName());
                    } else {
                        packagesToEnable.add(overlayItem.getPackageName());
                    }
                }
                AndromedaOverlayManager.INSTANCE.switchOverlay(packagesToEnable, true);
                AndromedaOverlayManager.INSTANCE.switchOverlay(packagesToDisable, false);
            }
            pActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateOverlayList();
                    Snackbar.make(coordinatorLayout,
                            R.string.selected_toggle_complete,
                            Snackbar.LENGTH_SHORT)
                            .setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            })
                            .show();
                }
            });
        }
    }
}
