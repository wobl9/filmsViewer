package ru.wobcorp.filmsviewer.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.wobcorp.filmsviewer.data.dto.FilmsSourceDto

interface FilmsApiService {

    private companion object {
        const val KEY = "c355a0993386aa6279e2f6f483182b48"
    }

    @GET("3/movie/popular")
    suspend fun getPopularFilms(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") key: String = KEY
    ): FilmsSourceDto

    @GET("3/movie/top_rated")
    suspend fun getTopFilms(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") key: String = KEY
    ): FilmsSourceDto

    @GET("3/movie/upcoming")
    suspend fun getUpcomingFilms(
        @Query("page") page: Int,
        @Query("language") language: String,
        @Query("api_key") key: String = KEY
    ): FilmsSourceDto
}