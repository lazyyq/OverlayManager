package kyklab.overlaymanager.overlay;

import java.util.List;

public interface OverlayInterface {
    boolean isAllChecked();

    void setAllChecked(boolean isAllChecked);

    void updateOverlayList();

    void toggleOverlays(List<Integer> indexes);

    void toggleOverlays(List<Integer> indexes, boolean newState);

    void toggleSelectedOverlays();

    void toggleSelectedOverlays(boolean newState);

    void removeAppFromList(String packageName);
}
