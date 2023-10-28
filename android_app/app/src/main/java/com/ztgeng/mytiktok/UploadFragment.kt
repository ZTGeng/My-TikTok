package com.ztgeng.mytiktok

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.media3.ui.PlayerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ztgeng.mytiktok.network.NetworkModule
import com.ztgeng.mytiktok.player.VideoPlayer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Callback
import java.io.File

class UploadFragment(private val callback: Callback<ResponseBody>) : Fragment() {

    private var videoUri: Uri? = null
    private lateinit var videoPlayer: VideoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var titleInputEditText: TextInputEditText
    private lateinit var uploadButton: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        view.setOnClickListener {
            // 点击空白区域隐藏软键盘
            closeKeyboard()
        }

        // 初始化视频播放器
        playerView = view.findViewById(R.id.uploadExoPlayerView)
        videoPlayer = VideoPlayer(this.requireContext())
        val player = videoPlayer.initializePlayer()
        playerView.player = player

        // 获取视频URI，加载视频，但不自动播放
        arguments?.let {
            videoUri = it.getParcelable("videoUri")
            videoUri?.let { uri ->
                videoPlayer.load(uri, autoPlay = false)
            }
        }

        // 视频标题输入
        titleInputLayout = view.findViewById(R.id.titleInputLayout)
        titleInputEditText = view.findViewById(R.id.titleInputEditText)
        uploadButton = view.findViewById(R.id.uploadButton)
        uploadButton.alpha = 0.5f

        // 监听标题输入，确保有效才能点击上传
        titleInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                uploadButton.isEnabled = s.toString().trim().isNotEmpty()
                uploadButton.alpha = if (uploadButton.isEnabled) 1f else 0.5f
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        uploadButton.setOnClickListener {
            videoUri?.let { uri ->
                val title = titleInputEditText.text.toString().trim()
                if (title.isNotEmpty()) {
                    uploadVideo(uri, title)
                    parentFragmentManager.popBackStack()
                }
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoPlayer.releasePlayer()
        closeKeyboard()
    }

    private fun uploadVideo(videoUri: Uri, title: String) {
        val file = File(getRealPathFromURI(videoUri))
        val requestFile = file.asRequestBody("video/mp4".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val filename = title.toRequestBody("text/plain".toMediaTypeOrNull())

        NetworkModule.apiService.uploadVideo(filename, body).enqueue(callback)
    }

    private fun getRealPathFromURI(contentURI: Uri): String {
        val result: String
        val cursor = activity?.contentResolver?.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI.path!!
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    private fun closeKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(videoUri: Uri, callback: Callback<ResponseBody>) =
            UploadFragment(callback).apply {
                arguments = Bundle().apply {
                    putParcelable("videoUri", videoUri)
                }
            }
    }
}