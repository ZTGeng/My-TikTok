package com.ztgeng.mytiktok.network

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    @GET("/videos")
    fun getVideoList(
        @Query("start") start: Int = 0,
        @Query("limit") limit: Int = 20
    ): Call<VideoListResponse>

    @Multipart
    @POST("upload")
    fun uploadVideo(
        @Part("filename") filename: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    data class VideoListResponse(
        val status: String,
        val data: List<Video>
    )

    data class Video(
        val id: String,
        val name: String
    )

    data class ErrorResponse(
        @SerializedName("status") val status: String,
        @SerializedName("message") val message: String
    )
}