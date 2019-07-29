package kyklab.overlaymanager;

import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;

class OverlayItemDiffCallback extends DiffUtil.Callback {
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
        return mOldOverlayList.get(oldItemPosition).getAppName().equals(
                mNewOverlayList.get(newItemPosition).getAppName()
        );
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldOverlayList.get(oldItemPosition).equals(
                mNewOverlayList.get(newItemPosition)
        );
    }
}
