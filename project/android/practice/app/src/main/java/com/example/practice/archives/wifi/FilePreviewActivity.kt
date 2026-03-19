package com.example.practice.archives.wifi

import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import com.example.practice.R

class FilePreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_preview)

        val imageView = findViewById<ImageView>(R.id.imagePreview)
        val videoView = findViewById<VideoView>(R.id.videoPreview)

        val uriString = intent.getStringExtra("file_uri") ?: run {
            finish()
            return
        }

        val uri = Uri.parse(uriString)

        val mimeType = contentResolver.getType(uri)
            ?: MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                )

        when {
            mimeType?.startsWith("image") == true -> {
                imageView.visibility = ImageView.VISIBLE
                imageView.setImageURI(uri)
            }

            mimeType?.startsWith("video") == true -> {
                videoView.visibility = VideoView.VISIBLE
                videoView.setVideoURI(uri)
                videoView.setOnPreparedListener {
                    it.isLooping = true
                    videoView.start()
                }
            }

            else -> {
                // Unsupported file
                finish()
            }
        }
    }
}
