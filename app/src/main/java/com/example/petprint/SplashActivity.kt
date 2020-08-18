package com.example.petprint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main1)

        //delayMillis 만큼 대기 후 MainActivity로 넘어감
        Handler().postDelayed({ //delay를 위한 handler
            startActivity(Intent(this@SplashActivity, FirstActivity::class.java))
            finish()
        }, 2000)
    }
}