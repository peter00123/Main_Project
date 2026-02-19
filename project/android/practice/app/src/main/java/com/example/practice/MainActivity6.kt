/*grid view page*/
package com.example.practice

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity6 : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 1. FIRST set the layout
        setContentView(R.layout.activity_main6)

        val textView = findViewById<TextView>(R.id.textname)
        val receivedName = intent.getStringExtra("username") ?: ""
        textView.text = "USER NAME : $receivedName"

        // ✅ 2. Now views exist – safe to use findViewById
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawerLayout)

        val backbtn = findViewById<Button>(R.id.back)
        backbtn.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent) // ✅ REQUIRED
            finish()
        }

        // ✅ 3. Drawer toggle
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
