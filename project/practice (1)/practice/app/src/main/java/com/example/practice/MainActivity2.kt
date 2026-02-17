package com.example.practice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity2 : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var runnable: Runnable
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

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

        // Connect dots with ViewPager
        TabLayoutMediator(dots, viewPager) { _, _ -> }.attach()

        val sendbtn = findViewById<Button>(R.id.send)

        sendbtn.setOnClickListener {
            val intent = Intent(this, MainActivity5::class.java)
            startActivity(intent)
        }



        // Auto scroll logic (REPEATING)
        runnable = object : Runnable {
            override fun run() {
                viewPager.currentItem =
                    (viewPager.currentItem + 1) % images.size
                handler.postDelayed(this, 3000)
            }
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




























