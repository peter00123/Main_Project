package com.example.practice

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class BackEndTest : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_back_end_test)

        val btn = findViewById<Button>(R.id.btnCall)
        val txt = findViewById<TextView>(R.id.txtResult)

        btn.setOnClickListener {
            callBackend(txt)
        }
    }

    private fun callBackend(txt: TextView) {

        val request = Request.Builder()
            .url("http://192.168.31.188:8080/api/test")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    txt.text = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()

                runOnUiThread {
                    txt.text = result ?: "Empty response"
                }
            }
        })
    }
}
