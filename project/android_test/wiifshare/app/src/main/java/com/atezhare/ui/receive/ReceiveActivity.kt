package com.atezhare.ui.receive


import android.graphics.Bitmap
import android.os.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import okhttp3.OkHttpClient
import okhttp3.Request

class ReceiveActivity : AppCompatActivity() {

    private lateinit var qrImage: ImageView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)

        qrImage = findViewById(R.id.qrImage)

        createSession()
    }

    private fun createSession() {
        Thread {
            val request = Request.Builder()
                .url("https://YOUR_SERVER/api/session")
                .build()

            val response = client.newCall(request).execute()
            val sessionId = response.body?.string() ?: return@Thread

            runOnUiThread {
                val qrData = "WIFISHARE:$sessionId"
                qrImage.setImageBitmap(generateQR(qrData))

                startPolling(sessionId)
            }
        }.start()
    }

    private fun generateQR(text: String): Bitmap {
        val size = 512
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }

    private fun startPolling(sessionId: String) {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                checkFile(sessionId)
                handler.postDelayed(this, 2000)
            }
        }

        handler.post(runnable)
    }

    private fun checkFile(sessionId: String) {
        Thread {
            val request = Request.Builder()
                .url("https://YOUR_SERVER/api/download/$sessionId")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                downloadFile(sessionId)
            }
        }.start()
    }

    private fun downloadFile(sessionId: String) {
        Thread {
            val request = Request.Builder()
                .url("https://YOUR_SERVER/api/download/$sessionId")
                .build()

            val response = client.newCall(request).execute()
            val input = response.body?.byteStream()

            val file = java.io.File(getExternalFilesDir(null), "received.jpg")
            val output = java.io.FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytes: Int

            while (input!!.read(buffer).also { bytes = it } != -1) {
                output.write(buffer, 0, bytes)
            }

            output.close()
        }.start()
    }
}