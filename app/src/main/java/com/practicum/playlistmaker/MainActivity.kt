package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.App


//++
const val PREFERENCES = "app_settings"
const val DARK_THEME = "setting_dark_theme"
const val TRACKS_SEARCH_HISTORY = "tracks_search_history"



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//
        val sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE)

        if (sharedPreferences.contains(DARK_THEME)) {
            val darkThemeEnabled: Boolean = sharedPreferences.getBoolean(DARK_THEME, false)
            (applicationContext as App).switchTheme(darkThemeEnabled)
        }
        else
        {
            sharedPreferences.edit()
                .putBoolean(DARK_THEME, isNightModeEnabled(this))
                .apply()
        }

        val buttonSearch = findViewById<Button>(R.id.button_find)
        buttonSearch.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            startActivity(displayIntent)
        }


        val buttonMedia = findViewById<Button>(R.id.button_media)
        buttonMedia.setOnClickListener {
            val displayIntent = Intent(this, MediaActivity::class.java)
            startActivity(displayIntent)
        }

        val buttonTools = findViewById<Button>(R.id.button_tools)
        buttonTools.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }

    }

    fun isNightModeEnabled(context: Context): Boolean {
        val resources = context.resources
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}