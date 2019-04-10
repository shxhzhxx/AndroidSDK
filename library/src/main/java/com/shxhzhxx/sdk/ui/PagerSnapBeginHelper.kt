package com.shxhzhxx.sdk.ui

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class PagerSnapBeginHelper : PagerSnapHelper() {
    private val horizontalMethod = PagerSnapHelper::class.java.getDeclaredMethod("getHorizontalHelper", RecyclerView.LayoutManager::class.java).apply { isAccessible = true }
    private val verticalMethod = PagerSnapHelper::class.java.getDeclaredMethod("getVerticalHelper", RecyclerView.LayoutManager::class.java).apply { isAccessible = true }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray? {
        val out = intArrayOf(0, 0)
        if (layoutManager.canScrollHorizontally()) {
            val helper = horizontalMethod.invoke(this, layoutManager) as OrientationHelper
            out[0] = helper.getDecoratedStart(targetView) - helper.startAfterPadding
        }

        if (layoutManager.canScrollVertically()) {
            val helper = verticalMethod.invoke(this, layoutManager) as OrientationHelper
            out[1] = helper.getDecoratedStart(targetView) - helper.startAfterPadding
        }
        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if (layoutManager is LinearLayoutManager)
            return findFirstView(layoutManager)
        return super.findSnapView(layoutManager)
    }

    private fun findFirstView(layoutManager: LinearLayoutManager) = layoutManager.findViewByPosition(layoutManager.findFirstVisibleItemPosition())
}
