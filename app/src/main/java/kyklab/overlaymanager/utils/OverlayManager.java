package kyklab.overlaymanager.utils;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import kyklab.overlaymanager.App;
import kyklab.overlaymanager.R;
import kyklab.overlaymanager.overlay.OverlayItem;
import projekt.andromeda.client.AndromedaOverlayManager;
import projekt.andromeda.client.util.OverlayInfo;

public class OverlayManager {
    public static List<OverlayItem> getOverlays() {
        List<OverlayItem> list = new ArrayList<>();

        String appName;
        Drawable icon;
        boolean enabled;
        String packageName = null;
        String targetAppName;
        String targetPackageName;
        boolean hasAppName;

        Drawable unknownIcon =
                App.getContext().getResources().getDrawable(R.drawable.ic_help_24dp, App.getContext().getTheme());

        Map<String, List<OverlayInfo>> overlayMap = AndromedaOverlayManager.INSTANCE.getAllOverlay();

        for (Map.Entry<String, List<OverlayInfo>> entry : overlayMap.entrySet()) {
            targetPackageName = entry.getKey();
            try {
                targetAppName = AppUtils.getApplicationName(App.getContext(), targetPackageName);
                icon = AppUtils.getApplicationIcon(App.getContext(), targetPackageName);
            } catch (PackageManager.NameNotFoundException e) {
                targetAppName = targetPackageName;
                icon = unknownIcon;
            }
            list.add(new OverlayItem(targetAppName, targetPackageName, icon));

            for (OverlayInfo overlay : entry.getValue()) {
                try {
                    packageName = overlay.getPackageName(); // Package name
                    appName = AppUtils.getApplicationName(App.getContext(), packageName); // App name
                    icon = AppUtils.getApplicationIcon(App.getContext(), packageName); // App icon
                    enabled = overlay.isEnabled(); // Enabled
                    hasAppName = !TextUtils.equals(appName, packageName); // Has its own app name

                    list.add(
                            new OverlayItem(
                                    hasAppName ? appName : null, // Store app name only when it exists
                                    icon, enabled, packageName, hasAppName)
                    );
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(App.getContext(),
                            "Error while loading app " + packageName,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }

        return list;
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
        for (OverlayItem overlay : list) {
            packages.add(overlay.getPackageName());
        }

        AndromedaOverlayManager.INSTANCE.switchOverlay(packages, state);
    }
}
