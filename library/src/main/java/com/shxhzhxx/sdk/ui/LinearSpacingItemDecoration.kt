package com.shxhzhxx.sdk.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LinearSpacingItemDecoration(private val space: Int, @RecyclerView.Orientation private val orientation: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter ?: return
        val position = parent.getChildAdapterPosition(view)
        val start = if (includeEdge || position > 0) space else 0
        val end = if (includeEdge && position == adapter.itemCount - 1) space else 0

        if (orientation == RecyclerView.HORIZONTAL) {
            outRect.top = 0
            outRect.bottom = 0
            outRect.left = start
            outRect.right = end
        } else {
            outRect.left = 0
            outRect.right = 0
            outRect.top = start
            outRect.bottom = end
        }
    }
}
