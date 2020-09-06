package ru.wobcorp.filmsviewer.data.repositories

import ru.wobcorp.filmsviewer.data.api.FilmsApiService
import ru.wobcorp.filmsviewer.data.mappers.FilmsMapper
import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.domain.FilmsLanguage
import javax.inject.Inject

interface FilmsRepository {
    suspend fun getPopularFilms(page: Int, language: FilmsLanguage): List<FilmModel>
    suspend fun getTopRatedFilms(page: Int, language: FilmsLanguage): List<FilmModel>
    suspend fun getUpcomingFilms(page: Int, language: FilmsLanguage): List<FilmModel>
}

class FilmsRepositoryImpl @Inject constructor(
    private val api: FilmsApiService,
    private val mapper: FilmsMapper
) : FilmsRepository {

    override suspend fun getPopularFilms(page: Int, language: FilmsLanguage) = api
        .getPopularFilms(page, language.query)
        .let(mapper::sourceToDomain)
        .films

    override suspend fun getTopRatedFilms(page: Int, language: FilmsLanguage): List<FilmModel> =
        api.getTopFilms(page, language.query)
            .let(mapper::sourceToDomain)
            .films

    override suspend fun getUpcomingFilms(page: Int, language: FilmsLanguage): List<FilmModel> =
        api.getUpcomingFilms(page, language.query)
            .let(mapper::sourceToDomain)
            .films
}