package com.shxhzhxx.sdk.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.scwang.smartrefresh.header.MaterialHeader
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.shxhzhxx.sdk.CoroutineFragment
import com.shxhzhxx.sdk.R
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.coroutines.launch
import java.util.*

abstract class ListFragment<M, VH : RecyclerView.ViewHolder, A : RecyclerView.Adapter<VH>> : CoroutineFragment(), SwipeRefreshLayout.OnRefreshListener {
    private val _list = ArrayList<M>()
    private var loading = false
    private val adapter by lazy { onAdapter() }

    protected val listSize: Int get() = _list.size

    protected val list: List<M> get() = _list.toList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipe.setOnRefreshListener(this)
        onHeader(swipe)
        onFooter(footer)

        listRecyclerView.itemAnimator = onItemAnimator()
        listRecyclerView.layoutManager = onLayoutManager()
        listRecyclerView.adapter = adapter

        smartRefreshLayout.setEnableRefresh(false)
        smartRefreshLayout.setOnLoadMoreListener { nextPage() }

        fun check() {
            smartRefreshLayout.setEnableLoadMore(_list.size % pageSize() == 0 && !swipe.isRefreshing)//根据swipe的isRefreshing状态来判断事件是否要禁止smartRefreshLayout可用
            swipe.isEnabled = smartRefreshLayout.state != RefreshState.ReleaseToLoad &&
                    smartRefreshLayout.state != RefreshState.LoadReleased &&
                    smartRefreshLayout.state != RefreshState.Loading  //根据smartRefreshLayout的Load状态来判断事件是否要禁止mSwipe可用
        }
        rootLayout.interceptor = {
            check()
            launch { check() }
            false
        }

        swipe.setOnRefreshListener(this)
        customizeView(context, view.findViewById(R.id.rooContentFl))
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

    protected open fun customizeView(context: Context?, parent: ViewGroup) {}

    protected open fun onHeader(header: SwipeRefreshLayout) {}

    protected open fun onFooter(footer: MaterialHeader) {}

    protected open fun onLayoutManager(): RecyclerView.LayoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

    protected fun addItemDecoration(decor: RecyclerView.ItemDecoration) {
        listRecyclerView?.addItemDecoration(decor)
    }

    open fun onItemAnimator(): RecyclerView.ItemAnimator {
        return DefaultItemAnimator()
    }

    protected abstract fun onAdapter(): A


    /**
     * @param onResult 必须调用这个方法来结束加载过程。
     * @param onLoad    调用这个方法代表成功获取指定页面的数据。
     *                  失败时不需要调用。
     *                  这个方法的调用必须在[onResult]后面，且中间不能插入对[ListFragment.nextPage]的调用
     *
     * */
    protected abstract fun onNextPage(page: Int, onResult: () -> Unit, onLoad: (list: List<M>) -> Unit)

    protected operator fun get(position: Int) = _list[position]

    override fun onRefresh() {
        nextPage()
    }

    private fun nextPage() {
        if (loading)
            return
        loading = true

        val refresh = swipe.isRefreshing
        val page = pageStartAt() + if (refresh) 0 else _list.size / pageSize()
        if (refresh) {
            smartRefreshLayout.setEnableLoadMore(false)
        } else {
            swipe.isEnabled = false
        }
        onNextPage(page,
                onResult = {
                    if (refresh) {
                        val size = _list.size
                        _list.clear()
                        adapter.notifyItemRangeRemoved(0, size)
                    }
                    loading = false
                    swipe.isEnabled = true
                    swipe.isRefreshing = false
                    smartRefreshLayout.setEnableLoadMore(true)
                    smartRefreshLayout.finishLoadMore()
                },
                onLoad = { list ->
                    smartRefreshLayout.setEnableLoadMore(list.size == pageSize())
                    if (list.isNotEmpty()) {
                        val start = _list.size
                        _list.addAll(list)
                        adapter.notifyItemRangeInserted(start, _list.size)
                    }
                })
    }
}
