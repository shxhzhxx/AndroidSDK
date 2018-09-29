package com.shxhzhxx.sdk.ui;

import android.graphics.Rect;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class LinearSpacingItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL = LinearLayoutManager.VERTICAL;

    private int space;
    private int orientation;
    private boolean includeEdge;

    public LinearSpacingItemDecoration(int space, int orientation, boolean includeEdge) {
        this.space = space;
        this.orientation = orientation;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int start = includeEdge || position > 0 ? space : 0;
        int end = includeEdge && position == parent.getAdapter().getItemCount() - 1 ? space : 0;

        if (orientation == HORIZONTAL) {
            outRect.top = 0;
            outRect.bottom = 0;
            outRect.left = start;
            outRect.right = end;
        } else {
            outRect.left = 0;
            outRect.right = 0;
            outRect.top = start;
            outRect.bottom = end;
        }
    }
}
