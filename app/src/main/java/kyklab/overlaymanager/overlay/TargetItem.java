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

    public TargetItem(@Nullable String appName, @NonNull String packageName,
                      @NonNull Drawable icon) {
        this.appName = TextUtils.equals(appName, packageName) ? null : appName;
        this.packageName = packageName;
        this.icon = icon;
    }

    @Override
    public int getItemType() {
        return RvItem.TYPE_TARGET;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetItem that = (TargetItem) o;
        return TextUtils.equals(appName, that.appName) &&
                TextUtils.equals(packageName, that.packageName) &&
                icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName, icon);
    }
}
