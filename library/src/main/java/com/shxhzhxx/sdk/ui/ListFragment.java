package com.shxhzhxx.sdk.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.shxhzhxx.sdk.BaseFragment;
import com.shxhzhxx.sdk.R;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFragment<M, VH extends RecyclerView.ViewHolder, A extends RecyclerView.Adapter<VH>> extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    public abstract class LoadCallback {
        /**
         * 必须调用这个方法来结束加载过程。
         */
        public abstract void onResult();

        /**
         * 调用这个方法代表成功获取指定页面的数据。
         * 失败时不需要调用。
         * 这个方法的调用必须在{@link #onResult()}后面，且中间不能插入对{@link ListFragment#nextPage(int)}的调用
         */
        public abstract void onLoad(List<M> list);
    }

    private final int LOAD_MORE = 1;
    private final int REFRESH = 1 << 1;
    private final int SWIPE = 1 << 2;
    private SwipeRefreshLayout mSwipe;
    private List<M> mList = new ArrayList<>();
    private LoadMoreAdapter mLoadMoreAdapter;
    private RecyclerView mListView;
    private boolean mEnableLoadMore = true, mDetectScrollGesture = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipe = view.findViewById(R.id.swipe);
        mSwipe.setOnRefreshListener(this);
        mListView = view.findViewById(R.id.list);
        mListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        final GestureDetector detector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mDetectScrollGesture && mEnableLoadMore && !mListView.canScrollVertically(1) && distanceY > 0 && !mLoadMoreAdapter.isLoadingVisible()) {
                    mDetectScrollGesture = false;
                    mLoadMoreAdapter.setLoadingVisible(true);
                    nextPage(LOAD_MORE);
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
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean enable = !mListView.canScrollVertically(-1);
                if (enable != mSwipe.isEnabled()) {
                    mSwipe.setEnabled(enable);
                    mSwipe.setRefreshing(false);
                }
            }
        });
        mLoadMoreAdapter = new LoadMoreAdapter(onAdapter());
        mListView.setAdapter(mLoadMoreAdapter);
        mEnableLoadMore = true;
        mLoadMoreAdapter.setLoadingVisible(false);

        refresh();
    }

    public final void refresh() {
        if (mSwipe.isEnabled())
            nextPage(REFRESH);
    }

    protected int pageSize() {
        return 10;
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

    @Override
    public final void onRefresh() {
        nextPage(SWIPE);
    }

    /**
     * 回调{@link LoadCallback#onResult()}之前不允许再次调用本方法。
     */
    private void nextPage(final int flag) {
        mEnableLoadMore = false;
        mSwipe.setEnabled(false);
        final int page = (flag & (REFRESH | SWIPE)) != 0 ? 1 : mList.size() / pageSize() + 1;

        if ((flag & (SWIPE | LOAD_MORE)) == 0) {
            mSwipe.setRefreshing(true);
        }
        onNextPage(page, new LoadCallback() {
            @Override
            public void onResult() {
                if ((flag & (REFRESH | SWIPE)) != 0) {
                    mSwipe.setRefreshing(false);
                    mList.clear();
                    mLoadMoreAdapter.notifyDataSetChanged();
                }
                mLoadMoreAdapter.setLoadingVisible(false);
                mEnableLoadMore = true;
                mSwipe.setEnabled(true);
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