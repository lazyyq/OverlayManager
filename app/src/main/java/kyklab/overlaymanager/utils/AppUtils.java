package kyklab.overlaymanager.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppUtils {
    public static String getApplicationName(Context context, String packageName)
            throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
        return pm.getApplicationLabel(app).toString();
    }

    public static Drawable getApplicationIcon(Context context, String packageName)
            throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
        return pm.getApplicationIcon(app);
    }

    // TODO: Find a workaround for unreliable behavior of AndromedaOverlayManager's loading of
    //  currently installed overlays.
    /*
    public static boolean overlayExists(String packageName) {
        Map<String, List<OverlayInfo>> overlayInfoMap =
                AndromedaOverlayManager.INSTANCE.getAllOverlay();
        for (Map.Entry<String, List<OverlayInfo>> mapEntry : overlayInfoMap.entrySet()) {
            for (OverlayInfo overlayInfo : mapEntry.getValue()) {
                if (overlayInfo.getPackageName().equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
    */
}
