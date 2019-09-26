package kyklab.overlaymanager.overlay;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class OverlayItem implements RvItem {
    @Nullable private final String appName;
    @NonNull private final String packageName;
    @NonNull private final Drawable icon;
    private final boolean hasAppName;
    private boolean enabled;
    private boolean itemChecked;

    public OverlayItem(@Nullable String appName, @NonNull String packageName,
                       @NonNull Drawable icon, boolean hasAppName, boolean enabled) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.hasAppName = hasAppName;
        this.enabled = enabled;
        this.itemChecked = false;
    }

    @Override
    public int getItemType() {
        return RvItem.TYPE_OVERLAY;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isItemChecked() {
        return itemChecked;
    }

    public void setItemChecked(boolean itemChecked) {
        this.itemChecked = itemChecked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverlayItem that = (OverlayItem) o;
        return hasAppName == that.hasAppName &&
                enabled == that.enabled &&
                itemChecked == that.itemChecked &&
                Objects.equals(appName, that.appName) &&
                packageName.equals(that.packageName) &&
                icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, icon, hasAppName, enabled, itemChecked);
    }
}
