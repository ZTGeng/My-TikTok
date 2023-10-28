package com.ztgeng.mytiktok.utils

import android.content.Context
import android.content.SharedPreferences
import com.ztgeng.mytiktok.VideoAppApplication
import com.ztgeng.mytiktok.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object PreferencesHelper {

    private const val PREFERENCES_NAME = "video_app_preferences"
    private const val SERVER_IP_KEY = "server_ip"
    private const val DEFAULT_SERVER_IP = "192.168.0.3:5000"

    private val preferences: SharedPreferences by lazy {
        VideoAppApplication.applicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    var serverIp: String
        // 例如：http://192.168.0.3:5000/
        get() = "http://${preferences.getString(SERVER_IP_KEY, null) ?: DEFAULT_SERVER_IP}/"
        set(value) {
            GlobalScope.launch(Dispatchers.IO) {
                preferences.edit().putString(SERVER_IP_KEY, value).commit()
                NetworkModule.refreshApiService()
            }
        }
}