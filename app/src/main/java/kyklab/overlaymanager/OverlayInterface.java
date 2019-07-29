package kyklab.overlaymanager;

import java.util.List;

interface OverlayInterface {
    boolean isAllChecked();
    void setAllChecked(boolean isAllChecked);

    void updateOverlayList();
    void toggleOverlays(List<String> packages, boolean newState);
    void toggleSelectedOverlays();
    void toggleSelectedOverlays(boolean newState);
}
