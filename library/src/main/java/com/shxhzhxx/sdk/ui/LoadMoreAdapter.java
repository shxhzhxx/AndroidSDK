package com.shxhzhxx.sdk.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shxhzhxx.sdk.R;

public class LoadMoreAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int LOADING_LAYOUT_ID = R.layout.list_loading;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mWrapped;
    private boolean mVisible = false;

    public LoadMoreAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> wrapped) {
        mWrapped = wrapped;
        mWrapped.registerAdapterDataObserver(new DataSetChangeDelegate());
    }

    public void setLoadingVisible(boolean visible) {
        if (mVisible != visible) {
            mVisible = visible;
            if (visible) {
                notifyItemInserted(getItemCount() - 1);
            } else {
                notifyItemRemoved(getItemCount());
            }
        }
    }

    public boolean isLoadingVisible() {
        return mVisible;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == LOADING_LAYOUT_ID)
            return new LoadingHolder(LayoutInflater.from(parent.getContext()).inflate(LOADING_LAYOUT_ID, parent, false));
        return mWrapped.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < mWrapped.getItemCount()) {
            mWrapped.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mVisible && position == getItemCount() - 1)
            return LOADING_LAYOUT_ID;
        return mWrapped.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mWrapped.getItemCount() + (mVisible ? 1 : 0);
    }

    class LoadingHolder extends RecyclerView.ViewHolder {
        LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    private class DataSetChangeDelegate extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemMoved(fromPosition, toPosition);
        }
    }
}
