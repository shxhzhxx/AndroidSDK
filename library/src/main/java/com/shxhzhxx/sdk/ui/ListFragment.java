package com.shxhzhxx.sdk.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.shxhzhxx.sdk.BaseFragment;
import com.shxhzhxx.sdk.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class ListFragment<M, VH extends RecyclerView.ViewHolder, A extends RecyclerView.Adapter<VH>> extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private List<M> mList = new ArrayList<>();
    private boolean mLoading = false;
    private SwipeRefreshLayout mSwipe;
    private SmartRefreshLayout mSmartRefreshLayout;
    private A mListAdapter = onAdapter();
    private RecyclerView mListView;
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mEventRunnable = new Runnable() {
        @Override
        public void run() {
            mSmartRefreshLayout.setEnableLoadMore(mList.size()%pageSize()==0 && !mSwipe.isRefreshing());//根据mSwipe的isRefreshing状态来判断事件是否要禁止mSmartRefreshLayout可用
            mSwipe.setEnabled(mSmartRefreshLayout.getState() != RefreshState.ReleaseToLoad &&
                    mSmartRefreshLayout.getState() != RefreshState.LoadReleased &&
                    mSmartRefreshLayout.getState() != RefreshState.Loading);  //根据mSmartRefreshLayout的Load状态来判断事件是否要禁止mSwipe可用
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InterceptFrameLayout rootLayout = view.findViewById(R.id.rootLayout);
        mSwipe = view.findViewById(R.id.swipe);
        mSwipe.setOnRefreshListener(this);
        onHeader(mSwipe);
        MaterialHeader footer = view.findViewById(R.id.footer);
        onFooter(footer);
        mSmartRefreshLayout = view.findViewById(R.id.smartRefreshLayout);

        mListView = view.findViewById(R.id.listRecyclerView);
        mListView.setItemAnimator(onItemAnimator());
        mListView.setLayoutManager(onLayoutManager());
        mListView.setAdapter(mListAdapter);

        mSmartRefreshLayout.setEnableRefresh(false);
        mSmartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                nextPage();
            }
        });

        rootLayout.setInterceptListener(new InterceptFrameLayout.InterceptListener() {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                mEventRunnable.run();
                mHandler.post(mEventRunnable);
                return false;
            }
        });

        mSwipe.setOnRefreshListener(this);
        customizeView(getContext(), view.<ViewGroup>findViewById(R.id.rooContentFl));
        refresh();
    }

    public final void refresh() {
        if (mSwipe != null) {
            mSwipe.setRefreshing(true);
            onRefresh();
        }
    }

    /**
     * 返回值不要太小，尽量避免一屏高度可以显示一页数据的情况。
     */
    protected int pageSize() {
        return 10;
    }

    protected int pageStartAt() {
        return 0;
    }

    protected void customizeView(Context context, ViewGroup parent) {
    }

    protected void onHeader(SwipeRefreshLayout header) {
    }

    protected void onFooter(MaterialHeader footer) {
    }

    protected RecyclerView.LayoutManager onLayoutManager() {
        return new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
    }

    protected void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        if (mListView != null) {
            mListView.addItemDecoration(decor);
        }
    }

    protected RecyclerView.ItemAnimator onItemAnimator() {
        return new DefaultItemAnimator();
    }

    @NonNull
    protected abstract A onAdapter();

    protected abstract void onNextPage(int page, LoadCallback callback);

    protected int getListSize() {
        return mList.size();
    }

    protected M getListItem(int position) {
        return mList.get(position);
    }

    protected List<M> getList() {
        return new ArrayList<>(mList);
    }

    @Override
    public final void onRefresh() {
        nextPage();
    }

    private void nextPage() {
        if (mLoading)
            return;
        mLoading = true;

        final boolean refresh = mSwipe.isRefreshing();
        final int page = pageStartAt() + (refresh ? 0 : mList.size() / pageSize());
        if (refresh) {
            mSmartRefreshLayout.setEnableLoadMore(false);
        } else {
            mSwipe.setEnabled(false);
        }
        onNextPage(page, new LoadCallback() {
            @Override
            public void onResult() {
                if (refresh) {
                    int size = mList.size();
                    mList.clear();
                    mListAdapter.notifyItemRangeRemoved(0, size);
                }
                mLoading = false;
                mSwipe.setEnabled(true);
                mSwipe.setRefreshing(false);
                mSmartRefreshLayout.setEnableLoadMore(true);
                mSmartRefreshLayout.finishLoadMore();
            }

            @Override
            public void onLoad(List<M> list) {
                mSmartRefreshLayout.setEnableLoadMore(list.size() == pageSize());
                if (!list.isEmpty()) {
                    int start = mList.size();
                    mList.addAll(list);
                    mListAdapter.notifyItemRangeInserted(start, mList.size());
                }
            }
        });
    }

    public abstract class LoadCallback {
        /**
         * 必须调用这个方法来结束加载过程。
         */
        public abstract void onResult();

        /**
         * 调用这个方法代表成功获取指定页面的数据。
         * 失败时不需要调用。
         * 这个方法的调用必须在{@link #onResult()}后面，且中间不能插入对{@link ListFragment#nextPage()}的调用
         */
        public abstract void onLoad(List<M> list);
    }

}
