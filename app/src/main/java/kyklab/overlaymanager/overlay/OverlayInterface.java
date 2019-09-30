package kyklab.overlaymanager.overlay;

import java.util.List;

public interface OverlayInterface {
    void setAllChecked(boolean isAllChecked);

    void updateOverlayList();

    void toggleOverlays(List<OverlayItem> selectedOverlays, Boolean state, boolean resetCheckState);

    void uninstallOverlay(OverlayItem overlay);
}
