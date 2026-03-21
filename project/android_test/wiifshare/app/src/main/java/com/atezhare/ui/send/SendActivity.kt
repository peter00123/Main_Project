package com.atezhare.ui.send

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.atezhare.R
import com.google.zxing.integration.android.IntentIntegrator
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class SendActivity : AppCompatActivity() {

    private lateinit var fileUri: Uri
    private val client = OkHttpClient()
    private var sessionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)

        findViewById<Button>(R.id.scanBtn).setOnClickListener {
            startScanner()
        }
    }

    private fun startScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan QR")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            sessionId = result.contents.replace("WIFISHARE:", "")
            pickFile()
        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                fileUri = it
                uploadFile()
            }
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 200)
    }

    private fun uploadFile() {
        // Note: This is a network operation and should be done on a background thread.
        // Using enqueue for asynchronous execution.
        val inputStream = contentResolver.openInputStream(fileUri)
        val bytes = inputStream?.readBytes() ?: return

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg",
                RequestBody.create("image/*".toMediaTypeOrNull(), bytes))
            .build()

        val request = Request.Builder()
            .url("https://YOUR_SERVER/api/upload/$sessionId")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
                response.close()
            }
        })
    }
}
