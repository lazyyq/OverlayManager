package kyklab.overlaymanager.overlay;

import android.graphics.drawable.Drawable;

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
        return hasAppName == that.hasAppName &&
                Objects.equals(appName, that.appName) &&
                packageName.equals(that.packageName) &&
                icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, icon, hasAppName);
    }
}
