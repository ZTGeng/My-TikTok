package com.ztgeng.mytiktok.network

import com.ztgeng.mytiktok.utils.PreferencesHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private var _apiService: ApiService? = null

    val apiService: ApiService
        get() = _apiService ?: createApiService().also { _apiService = it }

    private fun createApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(PreferencesHelper.serverIp)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun refreshApiService() {
        _apiService = createApiService()
    }
}