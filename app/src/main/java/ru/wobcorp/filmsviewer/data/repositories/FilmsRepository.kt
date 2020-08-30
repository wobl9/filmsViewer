package ru.wobcorp.filmsviewer.data.repositories

import ru.wobcorp.filmsviewer.data.api.FilmsApiService
import ru.wobcorp.filmsviewer.data.mappers.FilmsMapper
import ru.wobcorp.filmsviewer.domain.FilmModel
import javax.inject.Inject

interface FilmsRepository {

    private companion object {
        const val FIRST_PAGE = 1
    }

    suspend fun getFilms(page: Int = FIRST_PAGE): List<FilmModel>
}

class FilmsRepositoryImpl @Inject constructor(
    private val api: FilmsApiService,
    private val mapper: FilmsMapper
) : FilmsRepository {

    override suspend fun getFilms(page: Int) = api.getFilms(page)
        .let(mapper::sourceToDomain)
        .films
}