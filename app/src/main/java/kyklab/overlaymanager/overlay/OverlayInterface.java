package kyklab.overlaymanager.overlay;

import java.util.List;

public interface OverlayInterface {
    boolean isAllChecked();

    void setAllChecked(boolean isAllChecked);

    void updateOverlayList();

    void toggleOverlays(List<OverlayItem> list);

    void toggleOverlays(List<OverlayItem> list, boolean state);

    void removeAppFromList(String packageName);
}
