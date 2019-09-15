package kyklab.overlaymanager.overlay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class TargetItem implements RvItem {
    @Nullable private String appName;
    @NonNull private String packageName;

    public TargetItem(@Nullable String appName, @NonNull String packageName) {
        this.appName = appName;
        this.packageName = packageName;
    }

    @Override
    public int getItemType() {
        return RvItem.TYPE_TARGET;
    }

    @Override
    public String getAppName() {
        return appName != null ? appName : packageName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetItem that = (TargetItem) o;
        return Objects.equals(appName, that.appName) &&
                packageName.equals(that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appName, packageName);
    }
}
