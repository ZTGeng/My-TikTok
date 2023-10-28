package com.ztgeng.mytiktok.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class VideoPlayer(private val context: Context) {

    private var player: ExoPlayer? = null

    fun initializePlayer(): ExoPlayer {
        return player ?: ExoPlayer.Builder(context).build().also { player = it }
    }

    fun load(videoUrl: String, autoPlay: Boolean) {
        val uri = Uri.parse(videoUrl)
        load(uri, autoPlay)
    }

    fun load(uri: Uri, autoPlay: Boolean) {
        val mediaItem = MediaItem.fromUri(uri)
        player?.setMediaItem(mediaItem)
        player?.prepare()

        if (autoPlay) {
            player?.play()
        }
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }
}