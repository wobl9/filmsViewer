package ru.wobcorp.filmsviewer.domain

data class FilmsSourceModel(
    val page: Int,
    val totalPages: Int,
    val films: List<FilmModel>
)