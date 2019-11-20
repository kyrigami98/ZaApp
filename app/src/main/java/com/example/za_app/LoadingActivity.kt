package com.example.za_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent = Intent(this@LoadingActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)


    }

}