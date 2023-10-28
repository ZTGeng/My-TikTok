package com.ztgeng.mytiktok

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.ztgeng.mytiktok.network.ApiService
import com.ztgeng.mytiktok.network.NetworkModule
import com.ztgeng.mytiktok.ui.EndlessOnItemTouchListener
import com.ztgeng.mytiktok.ui.EndlessScrollListener
import com.ztgeng.mytiktok.ui.VideoListAdapter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val SETTINGS_FRAGMENT_TAG = "SettingsFragmentTag"
    private val UPLOAD_FRAGMENT_TAG = "UploadFragmentTag"

    private lateinit var videoRecyclerView: RecyclerView
    private lateinit var videoListAdapter: VideoListAdapter
    private lateinit var videoUploadButton: FloatingActionButton
    private val videos: MutableList<ApiService.Video> = mutableListOf()
    private var isLoading = false

    private var isVideoClickable = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已经被授予。打开视频选择器。
            openVideoPicker()
        } else {
            Toast.makeText(
                this,
                resources.getString(R.string.toast_upload_permission_denied),
                Toast.LENGTH_LONG).show()
        }
    }

    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri = result.data?.data
            if (videoUri != null) {
                promptForVideoTitle(videoUri)
            }
        }
    }

    private val videoListCallback = object : Callback<ApiService.VideoListResponse> {
        override fun onResponse(
            call: Call<ApiService.VideoListResponse>,
            response: Response<ApiService.VideoListResponse>
        ) {
            if (response.isSuccessful) {
                val videoList = response.body()?.data
                if (videoList != null) {
                    videos.addAll(videoList)
                    videoListAdapter.notifyItemRangeInserted(videos.size - videoList.size, videoList.size)
                }
            } else {
                val errorResponse = Gson().fromJson(response.errorBody()?.string(), ApiService.ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: resources.getString(R.string.unknown_error)
                Toast.makeText(
                    this@MainActivity,
                    resources.getString(R.string.toast_loading_failed, errorMessage),
                    Toast.LENGTH_LONG).show()
            }
            isLoading = false
        }

        override fun onFailure(call: Call<ApiService.VideoListResponse>, t: Throwable) {
            Toast.makeText(
                this@MainActivity,
                resources.getString(R.string.toast_loading_failed_client, t.message),
                Toast.LENGTH_LONG).show()
            isLoading = false
        }
    }

    private val uploadCallback = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                Toast.makeText(
                    this@MainActivity,
                    resources.getString(R.string.toast_upload_success),
                    Toast.LENGTH_LONG).show()
            } else {
                val errorResponse = Gson().fromJson(response.errorBody()?.string(), ApiService.ErrorResponse::class.java)
                val errorMessage = errorResponse?.message ?: resources.getString(R.string.unknown_error)
                Toast.makeText(
                    this@MainActivity,
                    resources.getString(R.string.toast_upload_failed, errorMessage),
                    Toast.LENGTH_LONG).show()
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Toast.makeText(
                this@MainActivity,
                resources.getString(R.string.toast_upload_failed_client, t.message),
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        videoRecyclerView = findViewById(R.id.videoRecyclerView)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        videoRecyclerView.layoutManager = layoutManager

        videoListAdapter = VideoListAdapter(videos) { video ->
            if (isVideoClickable) {
                isVideoClickable = false

                val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
                intent.putExtra("VIDEO_ID", video.id)
                intent.putExtra("VIDEO_NAME", video.name)
                startActivity(intent)

                Handler(Looper.getMainLooper()).postDelayed({
                    isVideoClickable = true
                }, 1000)
            }
        }
        videoRecyclerView.adapter = videoListAdapter

        // 当滚动到最后一个视频时，加载更多视频
        videoRecyclerView.addOnScrollListener(object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore() {
                if (!isLoading) {
                    isLoading = true
                    fetchVideoList()
                }
            }
        })
        videoRecyclerView.addOnItemTouchListener(object : EndlessOnItemTouchListener() {
            // 触及列表底部时，加载更多视频
            override fun onLoadMore() {
                if (!isLoading) {
                    isLoading = true
                    fetchVideoList()
                }
            }

            // 触及列表顶部时，刷新视频列表
            override fun onRefresh() {
                if (!isLoading) {
                    isLoading = true
                    val size = videos.size
                    videos.clear()
                    videoListAdapter.notifyItemRangeRemoved(0, size)
                    fetchVideoList()
                }
            }
        })

        videoUploadButton = findViewById(R.id.fab_upload)
        videoUploadButton.setOnClickListener {
            checkAndRequestPermissions()
        }

        // 在SettingsFragment或UploadFragment中隐藏fab
        supportFragmentManager.addOnBackStackChangedListener {
            adjustFabVisibility()
        }

        if (!isLoading) {
            isLoading = true
            fetchVideoList()
        }
    }

    private fun fetchVideoList() {
        NetworkModule.apiService.getVideoList(videos.size).enqueue(videoListCallback)
    }

    /*------------------TopBar和设置菜单相关------------------*/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val existingFragment = supportFragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG)
                if (existingFragment != null) {
                    supportFragmentManager.popBackStack()
                    return true
                }
                // 启动SettingsFragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment(), SETTINGS_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*------------------视频上传相关------------------*/
    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                // 权限已经被授予。打开视频选择器。
                openVideoPicker()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // 给用户解释为什么应用需要此权限。为了简单起见，这里使用Toast。
                Toast.makeText(
                    this,
                    resources.getString(R.string.toast_upload_permission_rationale),
                    Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                // 直接请求权限。结果将返回给requestPermissionLauncher。
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        videoPickerLauncher.launch(intent)
    }

    private fun promptForVideoTitle(videoUri: Uri) {
        val existingFragment = supportFragmentManager.findFragmentByTag(UPLOAD_FRAGMENT_TAG)
        if (existingFragment != null) {
            return
        }
        val fragment = UploadFragment.newInstance(videoUri, uploadCallback)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, UPLOAD_FRAGMENT_TAG)
            .addToBackStack(null)
            .commit()
    }

    private fun adjustFabVisibility() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is UploadFragment || fragment is SettingsFragment) {
            videoUploadButton.hide()
        } else {
            videoUploadButton.show()
        }
    }
}