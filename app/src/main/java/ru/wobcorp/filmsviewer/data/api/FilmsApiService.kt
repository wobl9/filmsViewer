package ru.wobcorp.filmsviewer.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.wobcorp.filmsviewer.data.dto.FilmsSourceDto

interface FilmsApiService {
    @GET("3/movie/popular?api_key=c355a0993386aa6279e2f6f483182b48")
    suspend fun getFilms(
        @Query("page") page: Int,
        @Query("language") language: String = "en-US"
    ): FilmsSourceDto
}