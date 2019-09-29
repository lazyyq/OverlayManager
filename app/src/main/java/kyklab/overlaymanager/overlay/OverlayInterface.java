package kyklab.overlaymanager.overlay;

import java.util.Set;

public interface OverlayInterface {
    boolean isChecked(int index);

    void setChecked(int index, boolean checked);

    void updateOverlayList();

    void toggleOverlays(Set<Integer> list, Boolean state, boolean resetCheckState);

    void uninstallPackageIndex(int index);
}
