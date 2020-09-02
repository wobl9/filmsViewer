package ru.wobcorp.filmsviewer.data.repositories

import ru.wobcorp.filmsviewer.data.api.FilmsApiService
import ru.wobcorp.filmsviewer.data.mappers.FilmsMapper
import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.domain.FilmsLanguage
import ru.wobcorp.filmsviewer.domain.FilmsLanguage.EN
import javax.inject.Inject

interface FilmsRepository {

    private companion object {
        const val FIRST_PAGE = 1
    }

    suspend fun getFilms(page: Int = FIRST_PAGE): List<FilmModel>
    fun setLanguage(language: FilmsLanguage)
}

class FilmsRepositoryImpl @Inject constructor(
    private val api: FilmsApiService,
    private val mapper: FilmsMapper
) : FilmsRepository {

    private var language = EN

    override suspend fun getFilms(page: Int) = api.getFilms(page, language.query)
        .let(mapper::sourceToDomain)
        .films

    override fun setLanguage(language: FilmsLanguage) {
        this.language = language
    }
}