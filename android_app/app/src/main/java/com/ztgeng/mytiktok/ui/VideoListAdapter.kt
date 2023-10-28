package com.ztgeng.mytiktok.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ztgeng.mytiktok.network.ApiService
import com.ztgeng.mytiktok.utils.PreferencesHelper

class VideoListAdapter(
    private val videos: List<ApiService.Video>,
    private val onVideoClick: (ApiService.Video) -> Unit
) : RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = VideoThumbnailView(parent.context)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.bind(video)
    }

    override fun getItemCount(): Int = videos.size

    inner class VideoViewHolder(
        private val videoThumbnailView: VideoThumbnailView
    ) : RecyclerView.ViewHolder(videoThumbnailView) {
        init {
            videoThumbnailView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onVideoClick(videos[position])
                }
            }
        }

        fun bind(video: ApiService.Video) {
            // 例如：http://192.168.0.3:5000/thumbnail/<video_id>
            val url = "${PreferencesHelper.serverIp}thumbnail/${video.id}"
            videoThumbnailView.setThumbnailUrl(url)
            videoThumbnailView.setTitle(video.name)
        }
    }
}