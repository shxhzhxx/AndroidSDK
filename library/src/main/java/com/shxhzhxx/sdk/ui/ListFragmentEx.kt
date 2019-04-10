package com.shxhzhxx.sdk.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.shxhzhxx.sdk.CoroutineFragment
import com.shxhzhxx.sdk.R
import kotlinx.android.synthetic.main.fragment_list_ex.*
import java.util.*

abstract class ListFragmentEx<M, VH : RecyclerView.ViewHolder, A : RecyclerView.Adapter<VH>> : CoroutineFragment(), SwipeRefreshLayout.OnRefreshListener {
    private val _list = ArrayList<M>()
    private val adapter: LoadMoreAdapter<VH> by lazy { LoadMoreAdapter(onAdapter()) }
    private var enableLoadMore = true
    private var loading = false

    protected val listSize: Int get() = _list.size

    protected val list: List<M> get() = ArrayList(_list)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_ex, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipe.setOnRefreshListener(this)
        listView.layoutManager = onLayoutManager()

        var detectScrollGesture = true
        val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                if (detectScrollGesture && enableLoadMore && !listView.canScrollVertically(1) && distanceY > 0) {
                    detectScrollGesture = false
                    adapter.isLoadingVisible = true
                    nextPage(false)
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                //一次滑动手势触发一次刷新，防止多次触发
                detectScrollGesture = true
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })
        listView.setOnTouchListener { v, event -> detector.onTouchEvent(event) }
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (loading) {
                    return
                }
                val enable = !listView.canScrollVertically(-1)
                if (enable != swipe.isEnabled && !swipe.isRefreshing) {
                    swipe.isEnabled = enable
                }
            }
        })
        listView.adapter = adapter
        adapter.isLoadingVisible = false
        customizeView(view.context, root)
        refresh()
    }

    fun refresh() {
        swipe?.isRefreshing = true
        onRefresh()
    }

    /**
     * 返回值不要太小，尽量避免一屏高度可以显示一页数据的情况。
     */
    protected open fun pageSize() = 10

    protected open fun pageStartAt() = 0

    protected open fun customizeView(context: Context, parent: ViewGroup) {}

    protected open fun onLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    protected fun addItemDecoration(decor: RecyclerView.ItemDecoration) {
        listView.addItemDecoration(decor)
    }

    protected abstract fun onAdapter(): A


    /**
     * @param onResult 必须调用这个方法来结束加载过程。
     * @param onLoad    调用这个方法代表成功获取指定页面的数据。
     *                  失败时不需要调用。
     *                  这个方法的调用必须在[onResult]后面，且中间不能插入对[ListFragment.nextPage]的调用
     * */
    protected abstract fun onNextPage(page: Int, onResult: () -> Unit, onLoad: (list: List<M>) -> Unit)

    protected fun getListItem(position: Int) = _list[position]

    override fun onRefresh() {
        nextPage(true)
    }

    private fun nextPage(refresh: Boolean) {
        loading = true
        enableLoadMore = false
        val page = pageStartAt() + if (refresh) 0 else _list.size / pageSize()
        if (!refresh && !swipe.isRefreshing) {
            swipe.isEnabled = false
        }
        onNextPage(page,
                onResult = {
                    loading = false
                    if (refresh) {
                        swipe.isRefreshing = false
                        _list.clear()
                        adapter.notifyDataSetChanged()
                    }
                    adapter.isLoadingVisible = false
                    enableLoadMore = true
                    if (!swipe.isRefreshing)
                        swipe.isEnabled = !listView.canScrollVertically(-1)
                },
                onLoad = {list->
                    if (!list.isEmpty()) {
                        val start = _list.size
                        _list.addAll(list)
                        adapter.notifyItemRangeInserted(start, list.size)
                    }
                    enableLoadMore = list.size == pageSize()
                })
    }
}
