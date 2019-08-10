package kyklab.overlaymanager.overlay;

import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;

public class OverlayItemDiffCallback extends DiffUtil.Callback {
    private final ArrayList<OverlayItem> mOldOverlayList;
    private final ArrayList<OverlayItem> mNewOverlayList;

    public OverlayItemDiffCallback(ArrayList<OverlayItem> mOldOverlayList, ArrayList<OverlayItem> mNewOverlayList) {
        this.mOldOverlayList = mOldOverlayList;
        this.mNewOverlayList = mNewOverlayList;
    }

    @Override
    public int getOldListSize() {
        return mOldOverlayList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewOverlayList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldOverlayList.get(oldItemPosition).getItemType()
                == mNewOverlayList.get(newItemPosition).getItemType();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldOverlayList.get(oldItemPosition).equals(mNewOverlayList.get(newItemPosition));
    }
}
