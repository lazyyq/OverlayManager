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
    private final boolean hasAppName;
    private boolean enabled;
    private boolean checked;

    public OverlayItem(@Nullable String appName, @NonNull String packageName,
                       @NonNull Drawable icon, boolean hasAppName, boolean enabled) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.hasAppName = hasAppName;
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
        return TextUtils.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, icon, hasAppName, enabled, checked);
    }

    public static class Payload {
        public static final String APP_NAME = "overlay_app_name";
        public static final String PACKAGE_NAME = "overlay_package_name";
        public static final String HAS_APP_NAME = "overlay_has_app_name";
        public static final String CHECKED = "checked";
        public static final String ENABLED = "enabled";
    }
}
