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
import ru.wobcorp.filmsviewer.utils.pagination.PaginationImpl
import ru.wobcorp.filmsviewer.utils.pagination.PaginationState
import ru.wobcorp.filmsviewer.utils.pagination.RequestFactory
import javax.inject.Inject
import kotlin.random.Random

interface FilmsListViewModel {
    val paginationState: LiveData<PaginationState<FilmModel>>
    fun onReachedItemPosition(position: Int)
    fun filmClicked(film: FilmModel)
    fun refresh()
    fun retryLoadPage()
}

class FilmsListViewModelImpl @Inject constructor(
    filmsRequestFactory: FilmsRequestFactory,
    private val filmsRepository: FilmsRepository
) : ViewModel(), FilmsListViewModel {

    override val paginationState = MutableLiveData<PaginationState<FilmModel>>()

    private val pagination = PaginationImpl(viewModelScope, 20, filmsRequestFactory)

    override fun onReachedItemPosition(position: Int) = pagination.onItemReached(position)

    init {
        pagination.start()
        viewModelScope.launch {
            pagination.state.collect { state ->
                paginationState.value = state
            }
        }
    }

    override fun retryLoadPage() = pagination.retry()

    override fun refresh() = pagination.refresh()

    override fun filmClicked(film: FilmModel) = filmsRepository.setLanguage(
        if (Random.nextBoolean()) {
            RUS
        } else {
            EN
        }
    )
}

class FilmsRequestFactory @Inject constructor(
    private val repository: FilmsRepository
) : RequestFactory<FilmModel> {
    override suspend fun create(page: Int): List<FilmModel> = repository.getFilms(page)
}