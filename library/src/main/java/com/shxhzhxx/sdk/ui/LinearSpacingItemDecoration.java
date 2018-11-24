package com.shxhzhxx.sdk.ui;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LinearSpacingItemDecoration extends RecyclerView.ItemDecoration {
    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int VERTICAL = RecyclerView.VERTICAL;

    private int space;
    private int orientation;
    private boolean includeEdge;

    public LinearSpacingItemDecoration(int space, int orientation, boolean includeEdge) {
        this.space = space;
        this.orientation = orientation;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null)
            return;
        int position = parent.getChildAdapterPosition(view);
        int start = includeEdge || position > 0 ? space : 0;
        int end = includeEdge && position == adapter.getItemCount() - 1 ? space : 0;

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
