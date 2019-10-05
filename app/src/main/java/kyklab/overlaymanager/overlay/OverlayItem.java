package kyklab.overlaymanager.overlay;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class OverlayItem implements RvItem {
    @Nullable private final String appName;
    @NonNull private final String packageName;
    @NonNull private final Drawable icon;
    private boolean enabled;
    private boolean checked;

    public OverlayItem(@Nullable String appName, @NonNull String packageName,
                       @NonNull Drawable icon, boolean enabled) {
        this.appName = TextUtils.equals(appName, packageName) ? null : appName;
        this.packageName = packageName;
        this.icon = icon;
        this.enabled = enabled;
        this.checked = false;
    }

    @Override
    public int getItemType() {
        return RvItem.TYPE_OVERLAY;
    }

    @Override
    @Nullable
    public String getAppName() {
        return appName;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverlayItem that = (OverlayItem) o;
        return enabled == that.enabled &&
                checked == that.checked &&
                TextUtils.equals(appName, that.appName) &&
                TextUtils.equals(packageName, that.packageName) &&
                icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, icon, enabled, checked);
    }

    public static class Payload {
        public static final String CHECKED_STATE = "checked_state";
        public static final String ENABLED_STATE = "enabled_state";
    }
}
