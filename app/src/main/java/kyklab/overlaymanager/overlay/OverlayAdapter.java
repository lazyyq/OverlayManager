package kyklab.overlaymanager.overlay;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

import kyklab.overlaymanager.R;
import projekt.andromeda.client.AndromedaOverlayManager;

public class OverlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {
    private final Activity pActivity;
    private final OverlayInterface mListener;
    private final ArrayList<OverlayItem> overlayList;

    private boolean listenForSwitchChange = false;

    private Runnable msgRunnable = new Runnable() {
        @Override
        public void run() {
            Snackbar.make(
                    pActivity.findViewById(R.id.coordinatorLayout),
                    R.string.selected_toggle_complete,
                    Snackbar.LENGTH_SHORT)
                    .setAction(android.R.string.ok, OverlayAdapter.this)
                    .show();
        }
    };

    public OverlayAdapter(Activity pActivity, OverlayInterface mListener, ArrayList<OverlayItem> overlayList) {
        this.pActivity = pActivity;
        this.mListener = mListener;
        this.overlayList = overlayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == OverlayItem.OVERLAY_ITEM_TYPE_CATEGORY) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.overlay_category, parent, false);
            return new OverlayCategoryHolder(view);

        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.overlay_item, parent, false);
            return new OverlayItemHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OverlayItem overlay = overlayList.get(position);
        if (holder instanceof OverlayCategoryHolder) {
            OverlayCategoryHolder overlayCategoryHolder = (OverlayCategoryHolder) holder;
            // Load category icon
            Glide.with(pActivity).load(overlay.getIcon()).into(overlayCategoryHolder.categoryIconView);
            // Set category name
            overlayCategoryHolder.categoryNameView.setText(overlay.getTargetAppName());
        } else {
            OverlayItemHolder overlayItemHolder = (OverlayItemHolder) holder;
            // Load app icon
            Glide.with(pActivity).load(overlay.getIcon()).into(overlayItemHolder.iconView);
            // If app name == package name, give smaller space for appNameView
            if (!overlay.isHasAppName()) {
                overlayItemHolder.appNameView.setSingleLine(true);
                overlayItemHolder.appNameView.setEllipsize(TextUtils.TruncateAt.END);
            } else {
                overlayItemHolder.appNameView.setSingleLine(false);
                overlayItemHolder.appNameView.setEllipsize(null);
            }
            // Set app name, package name, enable state, checkbox
            overlayItemHolder.appNameView.setText(overlay.getAppName());
            overlayItemHolder.packageNameView.setText(overlay.getPackageName());
            listenForSwitchChange = false;
            overlayItemHolder.overlaySwitch.setChecked(overlay.isEnabled());
            listenForSwitchChange = true;
            overlayItemHolder.itemCheckBox.setChecked(overlay.isItemChecked());
        }
    }

    @Override
    public int getItemCount() {
        return overlayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return overlayList.get(position).getItemType();
    }

    @Override
    public void onClick(View view) {

    }


    class OverlayCategoryHolder extends RecyclerView.ViewHolder {
        private final TextView categoryNameView;
        private final ImageView categoryIconView;

        OverlayCategoryHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameView = itemView.findViewById(R.id.categoryNameView);
            categoryIconView = itemView.findViewById(R.id.categoryIconView);
        }
    }

    class OverlayItemHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
        private final CardView itemCardView;
        private final ImageView iconView;
        private final TextView appNameView;
        private final TextView packageNameView;
        private final Switch overlaySwitch;
        private final CheckBox itemCheckBox;

        OverlayItemHolder(@NonNull View itemView) {
            super(itemView);
            itemCardView = itemView.findViewById(R.id.itemCardView);
            itemCardView.setOnClickListener(this);
            itemCardView.setOnLongClickListener(this);
            iconView = itemView.findViewById(R.id.appIconView);
            appNameView = itemView.findViewById(R.id.appNameView);
            packageNameView = itemView.findViewById(R.id.packageNameView);
            overlaySwitch = itemView.findViewById(R.id.overlaySwitch);
            overlaySwitch.setOnCheckedChangeListener(this);
            itemCheckBox = itemView.findViewById(R.id.itemCheckBox);
            itemCheckBox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.itemCardView:
                    itemCheckBox.setChecked(!itemCheckBox.isChecked());
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.itemCardView:
                    mListener.removeAppFromList(
                            overlayList.get(getAdapterPosition()).getPackageName()
                    );
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
            int id = compoundButton.getId();
            if (id == R.id.overlaySwitch) {
                if (listenForSwitchChange) {
                    new Thread() {
                        @Override
                        public void run() {
                            OverlayItem overlay = overlayList.get(getAdapterPosition());
                            AndromedaOverlayManager.INSTANCE.switchOverlay(
                                    Collections.singletonList(overlay.getPackageName()), b
                            );
                            pActivity.runOnUiThread(msgRunnable);
                        }
                    }.start();
                }
            } else if (id == R.id.itemCheckBox) {
                OverlayItem overlay = overlayList.get(getAdapterPosition());
                overlay.setItemChecked(b);
                if (!b) {
                    mListener.setAllChecked(false);
                }
            }
        }
    }
}
