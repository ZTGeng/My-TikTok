package com.ztgeng.mytiktok.ui

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

abstract class EndlessOnItemTouchListener : RecyclerView.OnItemTouchListener {

    private var initialY = 0f
    private var moved = false

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                initialY = e.y
                moved = false
            }
            MotionEvent.ACTION_MOVE -> {
                // 只有触屏后第一次划动才能触发onLoadMore或onRefresh
                if (moved) return false

                val deltaY = e.y - initialY
                if (deltaY != 0f) moved = true
                if (deltaY < 0 && !rv.canScrollVertically(1)) {
                    // 用户试图向上滚动，但RecyclerView已经滚动到底部
                    onLoadMore()
                    return true
                }
                if (deltaY > 0 && !rv.canScrollVertically(-1)) {
                    // 用户试图向下滚动，但RecyclerView已经滚动到顶部
                    onRefresh()
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    abstract fun onLoadMore()

    abstract fun onRefresh()
}