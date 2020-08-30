package ru.wobcorp.filmsviewer.domain

data class FilmModel(
    val id: Int,
    val title: String,
    val overview: String,
    val imageLink: String
)