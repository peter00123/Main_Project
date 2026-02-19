/*login page */
package com.example.practice

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.widget.TextView

class loginActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.button)
        val signup = findViewById<TextView>(R.id.signup)
        val resetPassword = findViewById<TextView>(R.id.forgotPassword)



        val correctUsername = "name"
        val correctPassword = "1234"




        fun showAlert(title: String, message: String) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        signup.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
        resetPassword.setOnClickListener {
            val intent = Intent(this, BackEndTest::class.java)
            startActivity(intent)
        }


        loginButton.setOnClickListener {

            val userInput = username.text.toString()
            val passInput = password.text.toString()
            /*val usernameStar = findViewById<TextView>(R.id.star)
            val passwordStar = findViewById<TextView>(R.id.star)*/


            if (userInput.isEmpty()) {
               username.visibility = View.VISIBLE
                username.error = "Username is required"
                username.requestFocus()
                return@setOnClickListener
            }
            if (passInput.isEmpty()) {
                password.visibility = View.VISIBLE
                password.error = "Password is required"
                password.requestFocus()
                return@setOnClickListener
            }



            if (/*userInput == correctUsername &&*/ passInput == correctPassword) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("username", userInput)
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                startActivity(intent)




            } else {

                showAlert("Invalid Username or Password", "Please enter valid username and password")

                /* Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()*/

            }







        }
    }
}
