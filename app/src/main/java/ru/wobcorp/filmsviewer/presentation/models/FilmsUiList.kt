package ru.wobcorp.filmsviewer.presentation.models

import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.presentation.models.FilmsUiList.FilmUi

sealed class FilmsUiList {
    data class FilmUi(val film: FilmModel) : FilmsUiList()
    object LoadingPage : FilmsUiList()
}

internal fun FilmModel.toUi() = FilmUi(film = this)