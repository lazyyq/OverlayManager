package kyklab.overlaymanager.overlay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface RvItem {
    int TYPE_OVERLAY = 0;
    int TYPE_TARGET = 1;

    int getItemType();

    @Nullable
    String getAppName();

    void setAppName(String appName);

    @NonNull
    String getPackageName();

    void setPackageName(String packageName);
}
