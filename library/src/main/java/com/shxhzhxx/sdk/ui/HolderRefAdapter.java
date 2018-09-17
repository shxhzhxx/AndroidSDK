package com.shxhzhxx.sdk.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Convenient class holds all ViewHolder's reference.
 * especially useful for {@link com.shxhzhxx.sdk.activity.ImageViewerActivity}
 */
public abstract class HolderRefAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private Set<WeakReference<VH>> holders = new HashSet<>();

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        holders.add(new WeakReference<>(holder));
        super.onBindViewHolder(holder, position, payloads);
    }

    public Iterable<VH> viewHolders() {
        Set<VH> result = new HashSet<>();
        for (WeakReference<VH> ref : holders) {
            VH h = ref.get();
            if (h != null && h.getAdapterPosition() >= 0) {
                result.add(h);
            }
        }
        return result;
    }
}
