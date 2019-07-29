package kyklab.overlaymanager.overlay;

import android.graphics.drawable.Drawable;

public class OverlayItem {
    public static final int OVERLAY_ITEM_TYPE_CATEGORY = 0;
    public static final int OVERLAY_ITEM_TYPE_ITEM = 1;

    private String appName;
    private Drawable icon;
    private boolean enabled;
    private String packageName;
    private String targetAppName;
    private String targetPackageName;
    private int state;
    private int itemType;
    private boolean hasAppName;
    private boolean itemChecked = false;

    public OverlayItem(String targetAppName, Drawable icon) {
        this.appName = null;
        this.icon = icon;
        this.enabled = false;
        this.packageName = null;
        this.targetAppName = targetAppName;
        this.targetPackageName = null;
        this.state = 0;
        this.itemType = OVERLAY_ITEM_TYPE_CATEGORY;
        this.hasAppName = true;
    }

    public OverlayItem(String appName, Drawable icon, boolean enabled, String packageName,
                       String targetAppName, String targetPackageName, int state, boolean hasAppName) {
        this.appName = appName;
        this.icon = icon;
        this.enabled = enabled;
        this.packageName = packageName;
        this.targetAppName = targetAppName;
        this.targetPackageName = targetPackageName;
        this.state = state;
        this.itemType = OVERLAY_ITEM_TYPE_ITEM;
        this.hasAppName = hasAppName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OverlayItem) {
            OverlayItem target = (OverlayItem) obj;

            return this.appName.equals(target.appName)
                    //&& this.icon == target.icon // Skip icon comparison as of now
                    && this.enabled == target.enabled
                    && this.packageName.equals(target.packageName)
                    && this.targetAppName.equals(target.targetAppName)
                    && this.targetPackageName.equals(target.targetPackageName)
                    && this.state == target.state
                    && this.itemType == target.itemType
                    && this.hasAppName == target.hasAppName
                    && this.itemChecked == target.itemChecked;
        } else {
            return false;
        }
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTargetAppName() {
        return targetAppName;
    }

    public void setTargetAppName(String targetAppName) {
        this.targetAppName = targetAppName;
    }

    public String getTargetPackageName() {
        return targetPackageName;
    }

    public void setTargetPackageName(String targetPackageName) {
        this.targetPackageName = targetPackageName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public boolean isHasAppName() {
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
}
