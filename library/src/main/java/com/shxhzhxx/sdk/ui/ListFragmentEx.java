package com.shxhzhxx.sdk.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.shxhzhxx.sdk.BaseFragment;
import com.shxhzhxx.sdk.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class ListFragmentEx<M, VH extends RecyclerView.ViewHolder, A extends RecyclerView.Adapter<VH>> extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public abstract class LoadCallback {
        /**
         * 必须调用这个方法来结束加载过程。
         */
        public abstract void onResult();

        /**
         * 调用这个方法代表成功获取指定页面的数据。
         * 失败时不需要调用。
         * 这个方法的调用必须在{@link #onResult()}后面，且中间不能插入对{@link ListFragmentEx#nextPage(boolean)}的调用
         */
        public abstract void onLoad(List<M> list);
    }

    private SwipeRefreshLayout mSwipe;
    private List<M> mList = new ArrayList<>();
    private LoadMoreAdapter mLoadMoreAdapter;
    private RecyclerView mListView;
    private boolean mEnableLoadMore = true, mDetectScrollGesture = true, mLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_ex, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipe = view.findViewById(R.id.swipe);
        mSwipe.setOnRefreshListener(this);
        mListView = view.findViewById(R.id.list);
        mListView.setLayoutManager(onLayoutManager());

        final GestureDetector detector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mDetectScrollGesture && mEnableLoadMore && !mListView.canScrollVertically(1) && distanceY > 0) {
                    mDetectScrollGesture = false;
                    mLoadMoreAdapter.setLoadingVisible(true);
                    nextPage(false);
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //一次滑动手势触发一次刷新，防止多次触发
                mDetectScrollGesture = true;
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detector.onTouchEvent(event);
            }
        });
        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (mLoading) {
                    return;
                }
                boolean enable = !mListView.canScrollVertically(-1);
                if (enable != mSwipe.isEnabled() && !mSwipe.isRefreshing()) {
                    mSwipe.setEnabled(enable);
                }
            }
        });
        mLoadMoreAdapter = new LoadMoreAdapter(onAdapter());
        mListView.setAdapter(mLoadMoreAdapter);
        mLoadMoreAdapter.setLoadingVisible(false);
        customizeView(view.getContext(), view.<ViewGroup>findViewById(R.id.root));
        refresh();
    }

    public final void refresh() {
        if (mSwipe != null) {
            if (!mSwipe.isRefreshing())
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

    protected RecyclerView.LayoutManager onLayoutManager() {
        return new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
    }

    protected void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        mListView.addItemDecoration(decor);
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
        nextPage(true);
    }

    /**
     * 回调{@link LoadCallback#onResult()}之前不允许再次调用本方法。
     */
    private void nextPage(final boolean refresh) {
        mLoading = true;
        mEnableLoadMore = false;
        final int page = pageStartAt() + (refresh ? 0 : mList.size() / pageSize());
        if (!refresh && !mSwipe.isRefreshing()) {
            mSwipe.setEnabled(false);
        }
        onNextPage(page, new LoadCallback() {
            @Override
            public void onResult() {
                mLoading = false;
                if (refresh) {
                    mSwipe.setRefreshing(false);
                    mList.clear();
                    mLoadMoreAdapter.notifyDataSetChanged();
                }
                mLoadMoreAdapter.setLoadingVisible(false);
                mEnableLoadMore = true;
                if (!mSwipe.isRefreshing())
                    mSwipe.setEnabled(!mListView.canScrollVertically(-1));
            }

            @Override
            public void onLoad(List<M> list) {
                if (!list.isEmpty()) {
                    int start = mList.size();
                    mList.addAll(list);
                    mLoadMoreAdapter.notifyItemRangeInserted(start, list.size());
                }
                mEnableLoadMore = list.size() == pageSize();
            }
        });
    }
}
