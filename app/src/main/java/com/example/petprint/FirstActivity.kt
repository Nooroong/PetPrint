package com.example.petprint

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_first.*

class FirstActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        val status = NetworkStatus.getConnectivityStatus(applicationContext)


        gotomap.setOnClickListener {

            if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
                Toast.makeText(this, "인터넷에 연결되어 있지 않습니다.\n연결 후 다시 시도해주세요.", Toast.LENGTH_LONG)
                    .show()
            } else {
                startActivity(Intent(this@FirstActivity, MainActivity::class.java))
                Toast.makeText(this, "지도 로딩 중", Toast.LENGTH_LONG).show()
            }
        }

        gotowalk.setOnClickListener {
            if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
                Toast.makeText(this, "인터넷에 연결되어 있지 않습니다.\n연결 후 다시 시도해주세요.", Toast.LENGTH_LONG)
                    .show()
            } else {
                startActivity(Intent(this@FirstActivity, WalkingPathActivity::class.java))
            }
        }

        record.setOnClickListener {
            if (status == NetworkStatus.TYPE_NOT_CONNECTED) {
                Toast.makeText(this, "인터넷에 연결되어 있지 않습니다.\n연결 후 다시 시도해주세요.", Toast.LENGTH_LONG)
                    .show()
            } else {
                startActivity(Intent(this@FirstActivity, WalkingRecordActivity::class.java))
            }
        }
    }
}