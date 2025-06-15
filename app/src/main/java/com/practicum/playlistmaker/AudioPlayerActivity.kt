package com.practicum.playlistmaker

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toMainButton = findViewById<ImageView>(R.id.buttonToMain)
        toMainButton.setOnClickListener {
            finish()
        }

        val selectedTrackString = intent.getStringExtra("selectedTrack")
        val selectedTrack = Gson().fromJson(selectedTrackString, Track::class.java)


        val albumImage = findViewById<ImageView>(R.id.albumImage)
        val artistName = findViewById<TextView>(R.id.artistName)
        val album = findViewById<TextView>(R.id.collectionName)

        val trackName = findViewById<TextView>(R.id.trackName)

        val trackTimeMills = findViewById<TextView>(R.id.trackTimeMills)
        val primaryGenreName = findViewById<TextView>(R.id.primaryGenreName)
        val releaseDate = findViewById<TextView>(R.id.releaseDate)
        val country = findViewById<TextView>(R.id.country)


        if (selectedTrack != null) {

            album.text = selectedTrack.collectionName
            artistName.text = selectedTrack.artistName
            trackName.text = selectedTrack.trackName

            primaryGenreName.text = selectedTrack.primaryGenreName

            trackTimeMills.text = SimpleDateFormat("m:ss", Locale.getDefault()).format(selectedTrack.trackTimeMillis).toString()
            releaseDate.text = SimpleDateFormat("yyyy", Locale.getDefault()).format(selectedTrack.releaseDate).toString()

            country.text = selectedTrack.country


            Glide.with(this@AudioPlayerActivity)
                .load(selectedTrack.getCoverArtwork())
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(albumImage)

        }

    }
}