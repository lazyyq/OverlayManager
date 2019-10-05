package kyklab.overlaymanager.overlay;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class TargetItem implements RvItem {
    @Nullable private final String appName;
    @NonNull private final String packageName;
    @NonNull private final Drawable icon;
    private final boolean hasAppName;

    public TargetItem(@Nullable String appName, @NonNull String packageName,
                      @NonNull Drawable icon, boolean hasAppName) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.hasAppName = hasAppName;
    }

    @Override
    public int getItemType() {
        return RvItem.TYPE_TARGET;
    }

    @Override
    @Nullable
    public String getAppName() {
        return hasAppName ? appName : packageName;
    }

    @Override
    @NonNull
    public String getPackageName() {
        return packageName;
    }

    @Override
    @NonNull
    public Drawable getIcon() {
        return icon;
    }

    public boolean hasAppName() {
        return hasAppName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetItem that = (TargetItem) o;
        return TextUtils.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, icon, hasAppName);
    }

    public static class Payload {
        public static final String APP_NAME = "target_app_name";
        public static final String PACKAGE_NAME = "target_package_name";
    }
}
