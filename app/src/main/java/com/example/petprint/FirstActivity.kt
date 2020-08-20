package com.example.petprint

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        val status = NetworkStatus.getConnectivityStatus(applicationContext)

        gotomap.setOnClickListener {

            if(status == NetworkStatus.TYPE_NOT_CONNECTED) {
                Toast.makeText(this, "인터넷에 연결되어 있지 않습니다.\n연결 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this, "지도 로딩 중", Toast.LENGTH_LONG).show()
                startActivity(Intent(this@FirstActivity, MainActivity::class.java))
            }
        }

        gotowalk.setOnClickListener {
            if(status == NetworkStatus.TYPE_NOT_CONNECTED) {
                Toast.makeText(this, "인터넷에 연결되어 있지 않습니다.\n연결 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            }
            else {
                startActivity(Intent(this@FirstActivity, WalkingPathActivity::class.java))
            }
        }
    }
}