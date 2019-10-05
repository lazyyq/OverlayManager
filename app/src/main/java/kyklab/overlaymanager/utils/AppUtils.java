package kyklab.overlaymanager.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

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

    public static void openApplicationSettings(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        context.startActivity(intent);
    }
}
