package ru.wobcorp.filmsviewer.data.dto

import com.google.gson.annotations.SerializedName

class FilmDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path") val imageLink: String
)