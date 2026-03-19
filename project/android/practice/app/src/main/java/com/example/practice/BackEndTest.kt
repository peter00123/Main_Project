package com.example.practice

import android.content.Intent
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

        val btnlogout = findViewById<Button>(R.id.btnlogout)

        val btnCall = findViewById<Button>(R.id.btnCall)
        val txtResult = findViewById<TextView>(R.id.txtResult)

        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val txtResult2 = findViewById<TextView>(R.id.txtResult2)

        btnCall.setOnClickListener {
            callBackend(txtResult)
        }

        btnlogout.setOnClickListener {
            val intent = Intent(this, loginActivity::class.java)
            startActivity(intent)
        }




    }
    // OkHttp API call
//    private fun callBackend(txt: TextView) {
//
//        val request = Request.Builder()
//            .url("https://main-project-cdol.onrender.com/api/test")
//            .build()
//
//        client.newCall(request).enqueue(object : okhttp3.Callback {
//
//            override fun onFailure(call: Call, e: IOException) {
//                runOnUiThread {
//                    txt.text = "Error: ${e.message}"
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val result = response.body?.string()
//
//                runOnUiThread {
//                    txt.text = result ?: "Empty response"
//                }
//            }
//        })
//    }



        private fun callBackend(txt: TextView) {


        val request = Request.Builder()
            .url(WebURL.message)
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
