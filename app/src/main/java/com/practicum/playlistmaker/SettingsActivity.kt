package com.practicum.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toMainButton = findViewById<ImageView>(R.id.button_to_main)
        toMainButton.setOnClickListener {
            val displayIntent = Intent(this, MainActivity::class.java)
            startActivity(displayIntent)
        }

        val toShare = findViewById<TextView>(R.id.share)
        toShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.tools_share_link))
            startActivity(shareIntent)
        }


        val toSupport = findViewById<TextView>(R.id.toSupport)
        toSupport.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.tools_to_support_email)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.tools_to_support_topic))
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.tools_to_support_text))
            startActivity(shareIntent)
        }


        val toAgreement = findViewById<TextView>(R.id.userAgreement)
        toAgreement.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW)
            shareIntent.data = Uri.parse(getString(R.string.tools_to_agreement))
            startActivity(shareIntent)
        }


    }
}