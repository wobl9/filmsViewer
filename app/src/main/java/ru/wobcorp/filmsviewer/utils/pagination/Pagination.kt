package ru.wobcorp.filmsviewer.utils.pagination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
interface Pagination<T : Any> {
    val state: StateFlow<PaginationState<T>>
    fun start()
    fun onItemReached(position: Int)
    fun refresh()
    fun retry()
    fun clear()
}

@ExperimentalCoroutinesApi
class PaginationImpl<T : Any> constructor(
    private val scope: CoroutineScope,
    private val pageSize: Int,
    private val requestFactory: RequestFactory<T>
) : Pagination<T> {

    private companion object {
        const val FIRST_PAGE = 0
    }

    private val _state = MutableStateFlow<PaginationState<T>>(PaginationState.Empty)
    private var request: Job? = null
    private var currentPage = FIRST_PAGE
    private var currentData = emptyList<T>()

    override val state: StateFlow<PaginationState<T>>
        get() = _state

    override fun start() {
        updateState { PaginationState.EmptyLoading }
        loadFirstPage()
    }

    override fun onItemReached(position: Int) {
        if (state.value is PaginationState.Content && state.value.content.size - position < pageSize / 2) {
            updateState { PaginationState.LoadingPage(currentData) }
            loadNewPage()
        }
    }

    override fun refresh() {
        updateState { PaginationState.Refreshing(currentData) }
        loadFirstPage()
    }

    override fun retry() = updateState { currentState ->
        when (currentState) {
            is PaginationState.EmptyError -> {
                loadNewPage()
                PaginationState.EmptyLoading
            }
            is PaginationState.LoadingPageError -> {
                loadNewPage()
                PaginationState.LoadingPage(currentState.content)
            }
            else -> currentState
        }
    }
    override fun clear() {
        request?.cancel()
    }

    private fun loadFirstPage() {
        currentPage = FIRST_PAGE
        currentData = emptyList()
        loadNewPage()
    }

    private fun loadNewPage() {
        if (state.value is PaginationState.FullContent) return
        currentPage++
        request?.cancel()
        request = scope.launch {
            try {
                requestFactory.create(currentPage).let(::onPageLoaded)
            } catch (throwable: Throwable) {
                currentPage--
                onPageLoadingError(throwable)
            }
        }
    }

    private fun onPageLoaded(items: List<T>) = updateState {
        when {
            items.size == pageSize -> {
                currentData = currentData + items
                PaginationState.Content(currentData)
            }
            items.isEmpty() && currentData.isEmpty() -> PaginationState.EmptyData
            else -> {
                currentData = currentData + items
                PaginationState.FullContent(currentData)
            }
        }
    }

    private fun onPageLoadingError(error: Throwable) = updateState { currentState ->
        when {
            currentData.isNotEmpty() -> PaginationState.LoadingPageError(
                error,
                currentState.content
            )
            else -> PaginationState.EmptyError(error)
        }
    }

    private inline fun updateState(newState: (oldState: PaginationState<T>) -> PaginationState<T>) {
        _state.value = newState(state.value)
    }
}

sealed class PaginationState<out T> {
    abstract val content: List<T>

    object Empty : PaginationState<Nothing>() {
        override val content: List<Nothing> = emptyList()
    }

    object EmptyLoading : PaginationState<Nothing>() {
        override val content: List<Nothing> = emptyList()
    }

    object EmptyData : PaginationState<Nothing>() {
        override val content: List<Nothing> = emptyList()
    }

    class EmptyError(val error: Throwable) : PaginationState<Nothing>() {
        override val content: List<Nothing> = emptyList()
    }

    class Content<T : Any>(override val content: List<T>) : PaginationState<T>()
    class FullContent<T : Any>(override val content: List<T>) : PaginationState<T>()
    class Refreshing<T : Any>(override val content: List<T>) : PaginationState<T>()
    class LoadingPage<T : Any>(override val content: List<T>) : PaginationState<T>()
    class LoadingPageError<T : Any>(
        val error: Throwable,
        override val content: List<T>
    ) : PaginationState<T>()
}

interface RequestFactory<T> {
    suspend fun create(page: Int): List<T> = emptyList()
    suspend fun create(limit: Int, offset: Int): List<T> = emptyList()
}