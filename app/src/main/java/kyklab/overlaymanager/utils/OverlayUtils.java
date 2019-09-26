package kyklab.overlaymanager.utils;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import kyklab.overlaymanager.App;
import kyklab.overlaymanager.overlay.OverlayItem;
import kyklab.overlaymanager.overlay.RvItem;
import kyklab.overlaymanager.overlay.TargetItem;
import projekt.andromeda.client.AndromedaOverlayManager;
import projekt.andromeda.client.util.OverlayInfo;

public class OverlayUtils {
    private static final String TAG = "OverlayUtils";

    public static List<RvItem> getOverlayRvItems() {
        List<RvItem> newList = new ArrayList<>();

        String appName = null;
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
                targetAppName = AppUtils.getApplicationName(App.getContext(), targetPackageName);
                icon = AppUtils.getApplicationIcon(App.getContext(), targetPackageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "Error while loading category " + packageName);
                continue;
            }
            hasAppName = !TextUtils.equals(appName, packageName);

            newList.add(new TargetItem(targetAppName, targetPackageName, icon, hasAppName));

            for (OverlayInfo overlay : entry.getValue()) {
                try {
                    packageName = overlay.getPackageName(); // Package name
                    appName = AppUtils.getApplicationName(App.getContext(), packageName); // App name
                    icon = AppUtils.getApplicationIcon(App.getContext(), packageName); // App icon
                    enabled = overlay.isEnabled(); // Enabled
                    hasAppName = !TextUtils.equals(appName, packageName); // Has its own app name

                    newList.add(
                            new OverlayItem(
                                    hasAppName ? appName : null, // Store app name only when it exists
                                    packageName, icon, hasAppName, enabled)
                    );
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error while loading overlay " + packageName);
                }
            }
        }

        return newList;
    }

    public static void toggleOverlays(List<OverlayItem> list) {
        String name;
        boolean state;

        for (OverlayItem overlay : list) {
            name = overlay.getPackageName();
            state = overlay.isEnabled();
            AndromedaOverlayManager.INSTANCE.switchOverlay(
                    Collections.singletonList(name), !state);
        }
    }

    public static void toggleOverlays(List<OverlayItem> list, boolean state) {
        List<String> packages = new ArrayList<>();
        for (RvItem item : list) {
            packages.add(item.getPackageName());
        }

        AndromedaOverlayManager.INSTANCE.switchOverlay(packages, state);
    }
}
