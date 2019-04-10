package com.shxhzhxx.sdk.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shxhzhxx.sdk.R

private val LOADING_LAYOUT_ID = R.layout.list_loading

private class LoadingHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
class LoadMoreAdapter<VH : RecyclerView.ViewHolder>(private val mWrapped: RecyclerView.Adapter<VH>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var isLoadingVisible = false
        set(visible) {
            if (isLoadingVisible != visible) {
                field = visible
                if (visible) {
                    notifyItemInserted(itemCount - 1)
                } else {
                    notifyItemRemoved(itemCount)
                }
            }
        }

    init {
        mWrapped.registerAdapterDataObserver(DataSetChangeDelegate())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == LOADING_LAYOUT_ID) LoadingHolder(LayoutInflater.from(parent.context).inflate(LOADING_LAYOUT_ID, parent, false)) else mWrapped.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < mWrapped.itemCount) {
            @Suppress("UNCHECKED_CAST")
            mWrapped.onBindViewHolder(holder as VH, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoadingVisible && position == itemCount - 1) LOADING_LAYOUT_ID else mWrapped.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return mWrapped.itemCount + if (isLoadingVisible) 1 else 0
    }

    private inner class DataSetChangeDelegate : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            notifyItemRangeRemoved(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            notifyItemRangeChanged(positionStart, itemCount)
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            notifyItemRangeChanged(positionStart, itemCount, payload)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }
    }
}
