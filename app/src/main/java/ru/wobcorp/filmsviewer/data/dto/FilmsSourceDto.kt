package ru.wobcorp.filmsviewer.data.dto

import com.google.gson.annotations.SerializedName

class FilmsSourceDto(
    @SerializedName("results") val films: List<FilmDto>,
    @SerializedName("page") val page: Int,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("total_pages") val totalPages: Int
)