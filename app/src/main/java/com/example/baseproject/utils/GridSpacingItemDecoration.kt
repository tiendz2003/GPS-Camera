package com.example.baseproject.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.presentation.hometab.adapter.PhotoAdapter

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter as PhotoAdapter

        // Nếu là header, không áp dụng padding
        if (adapter.getItemViewType(position) == PhotoAdapter.VIEW_TYPE_DATE_HEADER) {
            outRect.left = 0
            outRect.right = 0
            outRect.top = if (position > 0) spacing else 0
            outRect.bottom = spacing / 2
            return
        }

        // Tính toán cho items thông thường
        val column = (position - getHeaderCountBeforePosition(adapter, position)) % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            // Thêm top spacing cho hàng đầu tiên sau header
            if (isFirstItemAfterHeader(adapter, position)) {
                outRect.top = spacing
            } else {
                outRect.top = spacing / 2
            }
            outRect.bottom = spacing / 2
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            outRect.top = spacing / 2
            outRect.bottom = spacing / 2
        }
    }

    private fun isFirstItemAfterHeader(adapter: PhotoAdapter, position: Int): Boolean {
        return position > 0 && adapter.getItemViewType(position - 1) == PhotoAdapter.VIEW_TYPE_DATE_HEADER
    }

    private fun getHeaderCountBeforePosition(adapter: PhotoAdapter, position: Int): Int {
        var headerCount = 0
        for (i in 0 until position) {
            if (adapter.getItemViewType(i) == PhotoAdapter.VIEW_TYPE_DATE_HEADER) {
                headerCount++
            }
        }
        return headerCount
    }
}
class GridSpaceItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing / 2
        outRect.right = spacing / 3
        outRect.bottom = spacing
        outRect.top = spacing/2
    }
}