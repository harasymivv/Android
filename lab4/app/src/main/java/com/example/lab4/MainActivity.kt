package com.example.lab4


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var audioPlayer: MediaPlayer
    private lateinit var audioPlaceholder: android.widget.LinearLayout
    private lateinit var videoView: VideoView
    private lateinit var tabLayout: TabLayout
    private lateinit var buttonPlay: Button
    private lateinit var buttonPause: Button
    private lateinit var buttonStop: Button
    private lateinit var buttonLoadAudio: Button
    private lateinit var buttonLoadVideo: Button

    private var isAudioMode = true
    private var currentMediaUri: Uri? = null

    // Використовуємо DocumentPicker замість GetContent
    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            result.data?.data?.let { uri ->
                currentMediaUri = uri
                // Надаємо постійний доступ до файлу
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                loadSelectedMedia(uri)
            }
        }
    }

    // Запит дозволів
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "Дозволи надано", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Необхідні дозволи для роботи з файлами", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupAudioPlayer()
        requestPermissions()
        setupTabListener()
        setupControlButtons()
        updateUIForCurrentMode()
    }

    private fun initializeViews() {
        audioPlaceholder = findViewById(R.id.audio_placeholder)
        videoView = findViewById(R.id.video_view)
        tabLayout = findViewById(R.id.tab_layout)
        buttonPlay = findViewById(R.id.button_play)
        buttonPause = findViewById(R.id.button_pause)
        buttonStop = findViewById(R.id.button_stop)
        buttonLoadAudio = findViewById(R.id.button_load_audio)
        buttonLoadVideo = findViewById(R.id.button_load_video)
    }

    private fun setupAudioPlayer() {
        audioPlayer = MediaPlayer()
        audioPlayer.setOnCompletionListener {
            Toast.makeText(this, "Відтворення аудіо завершено", Toast.LENGTH_SHORT).show()
        }
        audioPlayer.setOnErrorListener { _, what, extra ->
            Toast.makeText(this, "Помилка аудіо: $what, $extra", Toast.LENGTH_LONG).show()
            true
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            ))
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun setupTabListener() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                isAudioMode = tab.position == 0
                stopCurrentPlayback()
                updateUIForCurrentMode()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupControlButtons() {
        buttonPlay.setOnClickListener { playMedia() }
        buttonPause.setOnClickListener { pauseMedia() }
        buttonStop.setOnClickListener { stopMedia() }

        buttonLoadAudio.setOnClickListener {
            isAudioMode = true
            tabLayout.selectTab(tabLayout.getTabAt(0))
            openAudioFilePicker()
        }

        buttonLoadVideo.setOnClickListener {
            isAudioMode = false
            tabLayout.selectTab(tabLayout.getTabAt(1))
            openVideoFilePicker()
        }
    }

    private fun openAudioFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        }
        documentPickerLauncher.launch(intent)
    }

    private fun openVideoFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        }
        documentPickerLauncher.launch(intent)
    }

    private fun loadSelectedMedia(uri: Uri) {
        currentMediaUri = uri

        if (isAudioMode) {
            loadAudioFile(uri)
        } else {
            loadVideoFile(uri)
        }
    }

    private fun loadAudioFile(uri: Uri) {
        try {
            audioPlayer.reset()
            audioPlayer.setDataSource(this, uri)
            audioPlayer.prepareAsync()
            audioPlayer.setOnPreparedListener {
                Toast.makeText(this, "Аудіо файл готовий до відтворення", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Помилка завантаження аудіо: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadVideoFile(uri: Uri) {
        try {
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = false
                Toast.makeText(this, "Відео файл готовий до відтворення", Toast.LENGTH_SHORT).show()
            }
            videoView.setOnErrorListener { _, what, extra ->
                Toast.makeText(this, "Помилка відтворення відео: $what, $extra", Toast.LENGTH_LONG).show()
                true
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка завантаження відео: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun playMedia() {
        if (currentMediaUri == null) {
            Toast.makeText(this, "Спочатку виберіть файл для відтворення", Toast.LENGTH_SHORT).show()
            return
        }

        if (isAudioMode) {
            if (!audioPlayer.isPlaying) {
                audioPlayer.start()
                Toast.makeText(this, "Відтворення аудіо", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (!videoView.isPlaying) {
                videoView.start()
                Toast.makeText(this, "Відтворення відео", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pauseMedia() {
        if (isAudioMode) {
            if (audioPlayer.isPlaying) {
                audioPlayer.pause()
                Toast.makeText(this, "Аудіо призупинено", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (videoView.isPlaying) {
                videoView.pause()
                Toast.makeText(this, "Відео призупинено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopMedia() {
        if (isAudioMode) {
            if (audioPlayer.isPlaying) {
                audioPlayer.stop()
                audioPlayer.prepareAsync()
            }
            Toast.makeText(this, "Аудіо зупинено", Toast.LENGTH_SHORT).show()
        } else {
            videoView.stopPlayback()
            if (currentMediaUri != null) {
                videoView.setVideoURI(currentMediaUri)
            }
            Toast.makeText(this, "Відео зупинено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopCurrentPlayback() {
        if (audioPlayer.isPlaying) {
            audioPlayer.stop()
        }
        videoView.stopPlayback()
        currentMediaUri = null
    }

    private fun updateUIForCurrentMode() {
        if (isAudioMode) {
            audioPlaceholder.visibility = android.view.View.VISIBLE
            videoView.visibility = android.view.View.GONE
            buttonLoadAudio.isEnabled = true
            buttonLoadVideo.isEnabled = false
        } else {
            audioPlaceholder.visibility = android.view.View.GONE
            videoView.visibility = android.view.View.VISIBLE
            buttonLoadAudio.isEnabled = false
            buttonLoadVideo.isEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (audioPlayer.isPlaying) {
            audioPlayer.pause()
        }
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
        videoView.stopPlayback()
    }
}