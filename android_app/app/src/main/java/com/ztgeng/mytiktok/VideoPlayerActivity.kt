package com.ztgeng.mytiktok

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.media3.ui.PlayerView
import com.ztgeng.mytiktok.player.VideoPlayer
import com.ztgeng.mytiktok.utils.PreferencesHelper

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var videoPlayer: VideoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var titleView: AppCompatTextView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        playerView = findViewById(R.id.exoPlayerView)
        titleView = findViewById(R.id.titleView)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        val videoName = intent.getStringExtra("VIDEO_NAME")
        if (videoName != null) {
            titleView.text = videoName
        }

        // 初始化视频播放器
        videoPlayer = VideoPlayer(this)
        val player = videoPlayer.initializePlayer()
        playerView.player = player

        // 获取视频ID，加载视频，自动播放
        val videoId = intent.getStringExtra("VIDEO_ID")
        if (videoId != null) {
            // 例如：http://192.168.0.3:5000/video/<videoId>
            val url = "${PreferencesHelper.serverIp}video/$videoId"
            videoPlayer.load(url, autoPlay = true)
        } else {
            Toast.makeText(
                this,
                resources.getString(R.string.video_play_invalid_url),
                Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPlayer.releasePlayer()
    }
}