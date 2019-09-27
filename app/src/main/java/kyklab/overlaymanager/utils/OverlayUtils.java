package kyklab.overlaymanager.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kyklab.overlaymanager.overlay.OverlayItem;
import kyklab.overlaymanager.overlay.RvItem;
import projekt.andromeda.client.AndromedaOverlayManager;

public class OverlayUtils {
    private static final String TAG = "OverlayUtils";

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
