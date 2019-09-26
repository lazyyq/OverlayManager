package kyklab.overlaymanager.overlay;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface RvItem {
    int TYPE_OVERLAY = 0;
    int TYPE_TARGET = 1;

    int getItemType();

    @Nullable
    String getAppName();

    @NonNull
    String getPackageName();

    @NonNull
    Drawable getIcon();
}
