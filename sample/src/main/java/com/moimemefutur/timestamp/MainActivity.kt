package com.moimemefutur.timestamp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.moimemefutur.timestamp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setUpClickListener()
    }

    private fun setUpClickListener() = with(binding) {
        btnGoToCameraView.setOnClickListener {
            Intent(this@MainActivity, CameraViewActivity::class.java).let(::startActivity)
        }
    }
}