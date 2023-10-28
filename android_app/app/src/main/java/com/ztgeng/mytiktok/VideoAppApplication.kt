package com.ztgeng.mytiktok

import android.app.Application
import android.content.Context

class VideoAppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: VideoAppApplication

        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }
}