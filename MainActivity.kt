package com.example.appswitcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var toggleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleButton = findViewById(R.id.toggle_button)
        toggleButton.setOnClickListener { toggleApp("com.example.targetapp") }
    }

    private fun toggleApp(packageName: String) {
        val packageManager: PackageManager = packageManager
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                startActivity(intent)
            } else {
                // App not installed or cannot be launched
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
