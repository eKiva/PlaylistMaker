package com.practicum.playlistmaker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Track(
    val trackId: String,
    val trackName: String,      // Название композиции
    val artistName: String,     // Имя исполнителя
    val trackTimeMillis: Int,
    //val trackTime: String,      // Продолжительность трека
    val artworkUrl100: String,   // Ссылка на изображение обложки


    val collectionName: String,     //Название альбома
    val releaseDate: Date,          //Год релиза трека
    val primaryGenreName: String,   //Жанр трека
    val country: String             // Страна исполнителя
)


{
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/',"512x512bb.jpg")


}