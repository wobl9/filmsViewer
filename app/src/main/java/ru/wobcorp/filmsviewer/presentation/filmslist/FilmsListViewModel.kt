package ru.wobcorp.filmsviewer.presentation.filmslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.wobcorp.filmsviewer.data.repositories.FilmsRepository
import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.domain.FilmsLanguage.EN
import ru.wobcorp.filmsviewer.domain.FilmsLanguage.RUS
import ru.wobcorp.filmsviewer.utils.pagination.*
import timber.log.Timber
import javax.inject.Inject

interface FilmsListViewModel {
    val paginationState: LiveData<PaginationState<FilmModel>>
    fun onReachedItemPosition(position: Int)
    fun filmClicked(film: FilmModel)
    fun refresh()
    fun retryLoadPage()
}

class FilmsListViewModelImpl @Inject constructor(
    filmsRequestFactory: FilmsRequestFactory
) : ViewModel(), FilmsListViewModel {

    override val paginationState = MutableLiveData<PaginationState<FilmModel>>()

    private val pagination = PaginationImpl(
        scope = viewModelScope,
        sources = arrayOf(
            Source(1, 20),
            Source(1, 20),
            Source(1, 20)
        ),
        requestFactory = filmsRequestFactory
    )

    override fun onReachedItemPosition(position: Int) = pagination.onItemReached(position)

    init {
        pagination.start()
        viewModelScope.launch {
            pagination.state.collect { state ->
                Timber.d(state.toString())
                paginationState.value = state
            }
        }
    }

    override fun retryLoadPage() = pagination.retry()

    override fun refresh() = pagination.refresh()

    override fun filmClicked(film: FilmModel) = Unit

    override fun onCleared() {
        pagination.clear()
        super.onCleared()
    }
}

class FilmsRequestFactory @Inject constructor(
    private val repository: FilmsRepository
) : RequestFactory<FilmModel> {

    private companion object {
        const val ENGLISH_POPULAR_FILMS = 0
        const val RUSSIAN_TOP_RATED_FILMS = 1
    }

    override suspend fun create(
        limit: Int,
        offset: Int,
        sourceIndex: Int
    ): Page<FilmModel> {
        val page = offset / limit
        return when (sourceIndex) {
            ENGLISH_POPULAR_FILMS -> Page(
                list = repository.getPopularFilms(
                    language = EN,
                    page = page
                ),
                totalPages = 2
            )
            RUSSIAN_TOP_RATED_FILMS -> Page(
                list = repository.getTopRatedFilms(
                    language = RUS,
                    page = page
                ),
                totalPages = 2
            )
            else -> Page(
                list = repository.getUpcomingFilms(
                    language = EN,
                    page = page
                ),
                totalPages = 2
            )
        }
    }
}