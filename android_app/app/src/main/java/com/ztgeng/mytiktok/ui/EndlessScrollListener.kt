package com.ztgeng.mytiktok.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.concurrent.TimeUnit

abstract class EndlessScrollListener(
    private val layoutManager: StaggeredGridLayoutManager
) : RecyclerView.OnScrollListener() {

    private var lastLoadTimestamp: Long = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        // 只有向下滚动时才触发
        if (dy <= 0) return

        // 检查是否在1秒内已经触发过
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLoadTimestamp < TimeUnit.SECONDS.toMillis(1)) return

        val totalItemCount = layoutManager.itemCount

        // 获取当前可见的最后一个item的位置
        val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
        val endOfVisibleItemCount = lastVisibleItemPositions.maxOrNull() ?: 0

        // 如果可见的item已经是最后一个，那么触发onLoadMore
        if (endOfVisibleItemCount + 1 >= totalItemCount) {
            onLoadMore()
            lastLoadTimestamp = currentTime
        }
    }

    abstract fun onLoadMore()
}