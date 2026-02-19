/*home page*/
package com.example.practice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.practice.com.example.practice.ImageSliderAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var runnable: Runnable
    private val handler = Handler(Looper.getMainLooper())

    // ✅ These MUST be initialized
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // ✅ Initialize drawer & toolbar FIRST
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Drawer toggle
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Welcome text
        val textView = findViewById<TextView>(R.id.textView2)
        val receivedName = intent.getStringExtra("username") ?: ""
        textView.text = "Welcome $receivedName"

        // ViewPager
        viewPager = findViewById(R.id.imageSlider)
        val dots = findViewById<TabLayout>(R.id.dotsIndicator)

        val images = listOf(
            R.drawable.imageone,
            R.drawable.image2,
            R.drawable.image3
        )

        viewPager.adapter = ImageSliderAdapter(images)

        TabLayoutMediator(dots, viewPager) { _, _ -> }.attach()

        // Buttons
        val sendbtn = findViewById<Button>(R.id.send)
        val receivebtn = findViewById<Button>(R.id.receive)

        receivebtn.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("username", receivedName)
            startActivity(intent)
        }

        sendbtn.setOnClickListener {
            startActivity(Intent(this, SendActivity::class.java))
        }

        // Auto-scroll
        runnable = object : Runnable {
            override fun run() {
                viewPager.currentItem =
                    (viewPager.currentItem + 1) % images.size
                handler.postDelayed(this, 3000)
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}
