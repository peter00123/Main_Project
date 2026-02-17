/*register page*/

package com.example.practice

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import android.content.Intent
import android.widget.Button
class MainActivity3 : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main3)

            val btnSignUp = findViewById<Button>(R.id.btnSignUp)

            btnSignUp.setOnClickListener {
                val intent = Intent(this, MainActivity2::class.java)
                startActivity(intent)
                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
            }
        }
    }