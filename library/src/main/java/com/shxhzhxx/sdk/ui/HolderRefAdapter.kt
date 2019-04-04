package com.shxhzhxx.sdk.ui

import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

/**
 * Convenient class holds all ViewHolder's reference.
 * especially useful for [com.shxhzhxx.sdk.activity.ImageViewerActivity]
 */
abstract class HolderRefAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    private val refs = ArrayList<WeakReference<VH>>()

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        if (refs.none { it.get() == holder })
            refs.add(WeakReference(holder))
        super.onBindViewHolder(holder, position, payloads)
    }

    val holders get(): Iterable<VH> = refs.mapNotNull { it.get() }.filter { it.adapterPosition != RecyclerView.NO_POSITION }
}
