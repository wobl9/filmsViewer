package ru.wobcorp.filmsviewer.data.mappers

import ru.wobcorp.filmsviewer.data.dto.FilmDto
import ru.wobcorp.filmsviewer.data.dto.FilmsSourceDto
import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.domain.FilmsSourceModel
import javax.inject.Inject

interface FilmsMapper {
    fun sourceToDomain(source: FilmsSourceDto): FilmsSourceModel
    fun filmToDomain(film: FilmDto): FilmModel
}

class FilmsMapperImpl @Inject constructor() : FilmsMapper {

    override fun sourceToDomain(source: FilmsSourceDto): FilmsSourceModel = FilmsSourceModel(
        page = source.page,
        totalPages = source.totalPages,
        films = source.films.map { filmDto -> filmToDomain(filmDto) }
    )

    override fun filmToDomain(film: FilmDto): FilmModel = FilmModel(
        id = film.id,
        title = film.title,
        overview = film.overview,
        imageLink = film.imageLink
    )
}