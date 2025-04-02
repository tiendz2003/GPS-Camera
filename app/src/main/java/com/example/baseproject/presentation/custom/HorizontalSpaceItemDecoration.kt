package com.example.baseproject.presentation.custom

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val spacing: Int, private val spaceInside: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = spacing
            outRect.right =spaceInside
        }else if (parent.getChildAdapterPosition(view) == (parent.adapter!!.itemCount-1)) {
            outRect.right = spacing
        }else{
            outRect.right = spaceInside
        }
    }
}