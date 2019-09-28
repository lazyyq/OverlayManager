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

    public OverlayItem(@Nullable String appName, @NonNull String packageName,
                       @NonNull Drawable icon, boolean hasAppName, boolean enabled) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.hasAppName = hasAppName;
        this.enabled = enabled;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverlayItem that = (OverlayItem) o;
        return hasAppName == that.hasAppName &&
                enabled == that.enabled &&
                Objects.equals(appName, that.appName) &&
                packageName.equals(that.packageName) &&
                icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, icon, hasAppName, enabled);
    }

    public static class Payload {
        public static final String CHECKED_STATE = "checked_state";
        public static final String ENABLED_STATE = "enabled_state";
    }
}
