package com.example.practice

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONObject
import java.util.UUID

class ReceiveActivity : AppCompatActivity() {

    private lateinit var imgQr: ImageView

    // session values saved for later
    companion object {
        var sessionId: String = ""
        var expiresAt: Long = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)

        imgQr = findViewById(R.id.imgQr)

        createSessionAndShowQr()
    }

    private fun createSessionAndShowQr() {
        try {
            sessionId = UUID.randomUUID().toString()
            expiresAt = System.currentTimeMillis() + (5 * 60 * 1000) // 5 min

            val sessionJson = JSONObject().apply {
                put("sessionId", sessionId)
                put("expiresAt", expiresAt)
            }

            generateQrCode(sessionJson.toString())

        } catch (e: Exception) {
            Log.e("RECEIVE", "Session creation failed", e)
        }
    }

    private fun generateQrCode(data: String) {
        val encoder = BarcodeEncoder()
        val bitmap = encoder.encodeBitmap(
            data,
            BarcodeFormat.QR_CODE,
            800,
            800
        )
        imgQr.setImageBitmap(bitmap)
    }
}