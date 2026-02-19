/*reset password*/

package com.example.practice


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity4 : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        fun showAlert(title: String, message: String) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }


        val email = findViewById<EditText>(R.id.email)
        val newPassword = findViewById<EditText>(R.id.newPassword)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)

        val resetButton = findViewById<Button>(R.id.resetButton)
        val backToLogin = findViewById<TextView>(R.id.backToLogin)

        backToLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        resetButton.setOnClickListener {
            if (email.text.isEmpty()) {
                email.visibility = View.VISIBLE
                email.error = "Email is required"
                email.requestFocus()
                return@setOnClickListener
            }

            if (newPassword.text.isEmpty()) {
                newPassword.visibility = View.VISIBLE
                newPassword.error = "New password is required"
                newPassword.requestFocus()
                return@setOnClickListener
            }
            if (confirmPassword.text.isEmpty()) {
                confirmPassword.visibility = View.VISIBLE
                confirmPassword.error = "Confirm password is required"
                confirmPassword.requestFocus()
                return@setOnClickListener
            }
            if (newPassword.text.toString() != confirmPassword.text.toString()) {
                confirmPassword.visibility = View.VISIBLE
                confirmPassword.error = "Passwords do not match"
                confirmPassword.requestFocus()
                return@setOnClickListener
            } else {

                showAlert(
                    "Invalid Username or Password",
                    "Please enter valid username and password"
                )

                /* Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()*/

            }

            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }
}