package com.ztgeng.mytiktok.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.ztgeng.mytiktok.R

class VideoThumbnailView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val thumbnailImageView: ImageView
    private val textView: AppCompatTextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_video, this, true)
        thumbnailImageView = findViewById(R.id.videoThumbnail)
        textView = findViewById(R.id.videoTitle)
    }

    fun setThumbnailUrl(url: String) {
        Glide.with(context)
            .load(url)
            .into(thumbnailImageView)
    }

    fun setTitle(title: String) {
        textView.text = title
    }
}