package com.example.practice

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.practice.network.ApiService
import com.example.practice.network.RetrofitClient
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import retrofit2.Callback
import retrofit2.Call as RetrofitCall
import retrofit2.Response as RetrofitResponse

class BackEndTest : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_back_end_test)

        val btnCall = findViewById<Button>(R.id.btnCall)
        val txtResult = findViewById<TextView>(R.id.txtResult)

        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val txtResult2 = findViewById<TextView>(R.id.txtResult2)

        btnCall.setOnClickListener {
            callBackend(txtResult)
        }


    }
    // OkHttp API call
    private fun callBackend(txt: TextView) {

        val request = Request.Builder()
            .url("http://10.92.211.95:8080/api/test")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {

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
