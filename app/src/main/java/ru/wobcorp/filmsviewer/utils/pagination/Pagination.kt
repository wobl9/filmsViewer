package ru.wobcorp.filmsviewer.utils.pagination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.wobcorp.filmsviewer.utils.pagination.PaginationState.*

@ExperimentalCoroutinesApi
interface Pagination<T : Any> {
    val state: StateFlow<PaginationState<T>>
    fun start(initialList: List<T>? = null)
    fun onItemReached(position: Int)
    fun refresh()
    fun retry()
    fun clear()
}

@ExperimentalCoroutinesApi
class PaginationImpl<T : Any> constructor(
    private val scope: CoroutineScope,
    private val requestFactory: RequestFactory<T>,
    private val sources: Array<Source>
) : Pagination<T> {

    private companion object {
        const val DEFAULT_SOURCE_START_POSITION = 0
        const val INITIAL_SOURCE_INDEX = 0
    }

    override val state: StateFlow<PaginationState<T>>
        get() = _state

    private var request: Job? = null

    private var currentSourceStartPosition: Int = DEFAULT_SOURCE_START_POSITION

    private var currentSourceIndex = INITIAL_SOURCE_INDEX

    private val currentPage
        get() = state.value.page

    private val initialPage = sources.first().initialPage

    private val _state = MutableStateFlow<PaginationState<T>>(
        Empty(content = emptyList(), page = initialPage)
    )

    private val pageSize
        get() = sources[currentSourceIndex].pageSize

    private val halfOfPage
        get() = pageSize / 2

    private val allSourcesHasEmptyContent
        get() = state.value is Empty && currentSourceIndex == sources.lastIndex

    init {
        require(sources.isNotEmpty()) { "At least one Source of data required" }
    }

    override fun start(initialList: List<T>?) {
        when {
            initialList == null -> {
                updateState { EmptyLoading(emptyList(), initialPage) }
                loadPage(initialPage)
            }
            initialList.isEmpty() -> {
                updateState { Empty() }
            }
            initialList.size < pageSize -> {
                updateState { FullContent(initialList, currentPage) }
            }
            else -> {
                updateState { Content(initialList, currentPage) }
            }
        }
    }

    override fun onItemReached(position: Int) {
        if (state.value is Content && state.value.content.size - position + currentSourceStartPosition < halfOfPage) {
            updateState { currentState ->
                LoadingPage(currentState.content, currentState.page)
            }
            loadPage(currentPage + 1)
        }
    }

    override fun refresh() {
        updateState {
            Refreshing(emptyList(), initialPage)
        }
        currentSourceStartPosition = DEFAULT_SOURCE_START_POSITION
        currentSourceIndex = INITIAL_SOURCE_INDEX
        loadPage(initialPage)
    }

    override fun retry() {
        updateState { currentState ->
            when (currentState) {
                is EmptyError -> EmptyLoading(page = initialPage)
                is LoadingPageError -> LoadingPage(currentState.content, currentState.page + 1)
                else -> currentState
            }
        }
        loadPage(currentPage)
    }

    override fun clear() {
        request?.cancel()
    }

    private fun loadPage(page: Int) {
        if (state.value is FullContent) return
        request?.cancel()
        request = scope.launch {
            try {
                onPageLoaded(
                    items = requestFactory.create(
                        limit = pageSize,
                        offset = (page) * pageSize,
                        currentSourceIndex = currentSourceIndex
                    ),
                    page = page
                )
            } catch (throwable: Throwable) {
                onPageLoadingError(throwable)
            }
        }
    }

    private fun onPageLoaded(items: List<T>, page: Int) {
        updateState { currentState ->
            when {
                items.size == pageSize -> {
                    Content(
                        content = currentState.content + items,
                        page = page
                    )
                }
                items.isEmpty() && currentState.content.isEmpty() -> EmptyData(
                    content = emptyList(),
                    page = currentState.page
                )
                else ->
                    FullContent(
                        content = currentState.content + items,
                        page = currentState.page
                    )
            }
        }
        if (state.value is FullContent || allSourcesHasEmptyContent.not()) {
            checkoutSource()
        }
    }

    private fun onPageLoadingError(error: Throwable) = updateState { currentState ->
        when {
            currentState.content.isNotEmpty() -> LoadingPageError(
                error = error,
                content = currentState.content,
                page = currentState.page
            )
            else -> EmptyError(error, initialPage)
        }
    }

    private fun checkoutSource() {
        if (currentSourceIndex >= sources.lastIndex) return
        currentSourceIndex++
        updateState { currentState ->
            currentSourceStartPosition = currentState.content.size
            LoadingPage(currentState.content, 0)
        }
        loadPage(sources[currentSourceIndex].initialPage)
    }

    private inline fun updateState(newState: (oldState: PaginationState<T>) -> PaginationState<T>) {
        _state.value = newState(state.value)
    }
}

sealed class PaginationState<out T> {

    companion object {
        const val FIRST_PAGE = 0
    }

    abstract val content: List<T>
    abstract val page: Int

    class Empty(
        override val content: List<Nothing> = emptyList(),
        override val page: Int = FIRST_PAGE
    ) : PaginationState<Nothing>()

    class EmptyLoading(
        override val content: List<Nothing> = emptyList(),
        override val page: Int = FIRST_PAGE
    ) : PaginationState<Nothing>()

    class EmptyData(
        override val content: List<Nothing> = emptyList(),
        override val page: Int = FIRST_PAGE
    ) : PaginationState<Nothing>()

    class EmptyError(
        val error: Throwable,
        override val page: Int = FIRST_PAGE
    ) : PaginationState<Nothing>() {
        override val content: List<Nothing> = emptyList()
    }

    class Content<T : Any>(
        override val content: List<T>,
        override val page: Int
    ) : PaginationState<T>()

    class FullContent<T : Any>(
        override val content: List<T>,
        override val page: Int
    ) : PaginationState<T>()

    class Refreshing<T : Any>(
        override val content: List<T>,
        override val page: Int
    ) : PaginationState<T>()

    class LoadingPage<T : Any>(
        override val content: List<T>,
        override val page: Int
    ) : PaginationState<T>()

    class LoadingPageError<T : Any>(
        val error: Throwable,
        override val content: List<T>,
        override val page: Int
    ) : PaginationState<T>()
}

class Source(val initialPage: Int, val pageSize: Int)

interface RequestFactory<T> {
    suspend fun create(limit: Int, offset: Int, currentSourceIndex: Int): List<T> = emptyList()
}