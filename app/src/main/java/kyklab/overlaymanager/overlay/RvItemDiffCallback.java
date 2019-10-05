package kyklab.overlaymanager.overlay;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class RvItemDiffCallback extends DiffUtil.Callback {
    private final List<RvItem> mOldList;
    private final List<RvItem> mNewList;

    public RvItemDiffCallback(List<RvItem> mOldList, List<RvItem> mNewList) {
        this.mOldList = mOldList;
        this.mNewList = mNewList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        RvItem oldItem = mOldList.get(oldItemPosition);
        RvItem newItem = mNewList.get(newItemPosition);
//        Log.e("Callback", "areItemsTheSame() called "+oldItem.getItemType()+", "+newItem.getItemType()+", "+(oldItem.getItemType() == newItem.getItemType() &&
//                TextUtils.equals(oldItem.getPackageName(), newItem.getPackageName()))); //TODO: remove

        return oldItem.getItemType() == newItem.getItemType() &&
                TextUtils.equals(oldItem.getPackageName(), newItem.getPackageName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        RvItem oldItem = mOldList.get(oldItemPosition);
        RvItem newItem = mNewList.get(newItemPosition);

        // We're not diffing icons here, we'll consider them changed if package name did.
        if (newItem.getItemType() == RvItem.TYPE_TARGET) {
            TargetItem oldTarget = (TargetItem) oldItem;
            TargetItem newTarget = (TargetItem) newItem;

//            Log.e("Callback", "areContentsTheSame() called "+oldItem.getItemType()+", "+newItem.getItemType()
//            +", "+(TextUtils.equals(oldTarget.getPackageName(), newTarget.getPackageName()))); //TODO: remove

            return TextUtils.equals(oldTarget.getPackageName(), newTarget.getPackageName());
        } else if (newItem.getItemType() == RvItem.TYPE_OVERLAY) {
            OverlayItem oldOverlay = (OverlayItem) oldItem;
            OverlayItem newOverlay = (OverlayItem) newItem;

//            Log.e("Callback", "areContentsTheSame() called "+oldItem.getItemType()+", "+newItem.getItemType()
//            +", "+(TextUtils.equals(oldOverlay.getAppName(), newOverlay.getPackageName()) &&
//                    TextUtils.equals(oldOverlay.getPackageName(), newOverlay.getPackageName()) &&
//                    oldOverlay.isChecked() == newOverlay.isChecked() &&
//                    oldOverlay.isEnabled() == newOverlay.isEnabled())); //TODO: remove

            return TextUtils.equals(oldOverlay.getAppName(), newOverlay.getPackageName()) &&
                    TextUtils.equals(oldOverlay.getPackageName(), newOverlay.getPackageName()) &&
                    oldOverlay.isChecked() == newOverlay.isChecked() &&
                    oldOverlay.isEnabled() == newOverlay.isEnabled();
        }

        return false;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        RvItem oldItem = mOldList.get(oldItemPosition);
        RvItem newItem = mNewList.get(newItemPosition);
        Bundle b = new Bundle();
//        Log.e("Callback", oldItem.getItemType() +", " + newItem.getItemType()); //TODO: remove

        if (newItem.getItemType() == RvItem.TYPE_TARGET) {
            // Get payloads for target items
            TargetItem oldTarget = (TargetItem) oldItem;
            TargetItem newTarget = (TargetItem) newItem;

            if (!TextUtils.equals(oldTarget.getAppName(), newTarget.getAppName())) {
                b.putString(TargetItem.Payload.APP_NAME, newTarget.getAppName());
            }
        } else if (newItem.getItemType() == RvItem.TYPE_OVERLAY) {
            // Get payloads for overlay items
            OverlayItem oldOverlay = (OverlayItem) oldItem;
            OverlayItem newOverlay = (OverlayItem) newItem;

            if (!TextUtils.equals(oldOverlay.getAppName(), newOverlay.getAppName())) {
                b.putString(OverlayItem.Payload.APP_NAME, newOverlay.getAppName());
            }
            if (!TextUtils.equals(oldOverlay.getPackageName(), newOverlay.getPackageName())) {
                b.putString(OverlayItem.Payload.PACKAGE_NAME, newOverlay.getPackageName());
            }
            b.putBoolean(OverlayItem.Payload.HAS_APP_NAME, newOverlay.hasAppName());
            if (oldOverlay.isChecked() != newOverlay.isChecked()) {
                b.putBoolean(OverlayItem.Payload.CHECKED, newOverlay.isChecked());
            }
            if (oldOverlay.isEnabled() != newOverlay.isEnabled()) {
                b.putBoolean(OverlayItem.Payload.ENABLED, newOverlay.isEnabled());
            }
        } else {
            return null;
        }

        return b;
    }
}
