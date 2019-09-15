package kyklab.overlaymanager.overlay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class OverlayItem implements RvItem {
    @Nullable private String appName;
    @NonNull private String packageName;
    private boolean enabled;
    private boolean hasAppName;
    private boolean itemChecked = false;

    public OverlayItem(@Nullable String appName, @NonNull String packageName,
                       boolean enabled, boolean hasAppName) {
        this.appName = appName;
        this.packageName = packageName;
        this.enabled = enabled;
        this.hasAppName = hasAppName;
    }

    @Override
    public int getItemType() {
        return RvItem.TYPE_OVERLAY;
    }

    @Override
    public String getAppName() {
        return hasAppName ? appName : packageName;
    }

    @Override
    public void setAppName(@Nullable String appName) {
        this.appName = appName;
    }

    @NonNull
    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public void setPackageName(@NonNull String packageName) {
        this.packageName = packageName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean hasAppName() {
        return hasAppName;
    }

    public void setHasAppName(boolean hasAppName) {
        this.hasAppName = hasAppName;
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
        return enabled == that.enabled &&
                hasAppName == that.hasAppName &&
                itemChecked == that.itemChecked &&
                Objects.equals(appName, that.appName) &&
                packageName.equals(that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, enabled, hasAppName, itemChecked);
    }
}
