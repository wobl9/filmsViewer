package ru.wobcorp.filmsviewer.utils.pagination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.wobcorp.filmsviewer.utils.pagination.PaginationState.*
import timber.log.Timber

@ExperimentalCoroutinesApi
interface Pagination<T : Any> {
    val state: StateFlow<PaginationState<T>>
    fun start(page: Page<T>? = null)
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
        const val INITIAL_SOURCE_INDEX = 0
        const val INTERNAL_ERROR = "Internal pagination error."
        const val LOAD_TRIGGER = 10
    }

    override val state: StateFlow<PaginationState<T>>
        get() = _state

    private var request: Job? = null

    private var currentSourceIndex = INITIAL_SOURCE_INDEX

    private val initialPage = sources.first().initialPage

    private val currentPage
        get() = state.value.page

    private val _state = MutableStateFlow<PaginationState<T>>(
        Empty(content = emptyList(), page = initialPage)
    )

    private val pageSize
        get() = sources[currentSourceIndex].pageSize

    private val isLastSource
        get() = currentSourceIndex == sources.lastIndex

    private val contentSize
        get() = state.value.content.size

    private val currentSourceInitialPage
        get() = sources[currentSourceIndex].initialPage

    init {
        require(sources.isNotEmpty()) { "At least one Source of data required" }
    }

    override fun start(page: Page<T>?) {
        val initialList = page?.list
        when {
            initialList == null -> {
                updateState { EmptyLoading(emptyList(), initialPage) }
                loadPage(initialPage)
            }
            initialList.isEmpty() && isLastSource.not() -> {
                updateState { EmptyData() }
                onItemReached(0)
            }
            initialList.isEmpty() -> {
                updateState { EmptyData() }
            }
            initialList.size < pageSize && isLastSource -> {
                updateState { FullContent(initialList, currentPage) }
            }
            initialList.size < pageSize && isLastSource.not() -> {
                updateState { Content(initialList, currentPage, true) }
            }
            initialList.size == pageSize -> {
                updateState { Content(initialList, currentPage, page.totalPages == currentPage) }
            }
            else -> throw IllegalStateException("$INTERNAL_ERROR Invalid initial state:${state.value} ")
        }
    }

    override fun onItemReached(position: Int) {
        val needLoadMore = contentSize - position < LOAD_TRIGGER
        val state = state.value
        when {
            state is Content && needLoadMore -> {
                if (state.isLastPage && isLastSource.not()) {
                    checkoutNextSourceAndUpdateState()
                    loadPage(currentSourceInitialPage)
                } else {
                    updateState { currentState ->
                        LoadingPage(currentState.content, currentState.page)
                    }
                    loadPage(currentPage + 1)
                }
            }
            state is EmptyData && isLastSource.not() -> {
                checkoutNextSourceAndUpdateState()
                loadPage(currentSourceInitialPage)
            }
        }
    }

    override fun refresh() {
        updateState {
            Refreshing(emptyList(), initialPage)
        }
        currentSourceIndex = INITIAL_SOURCE_INDEX
        loadPage(initialPage)
    }

    override fun retry() {
        updateState { currentState ->
            when (currentState) {
                is EmptyError -> EmptyLoading(page = initialPage)
                is LoadingPageError -> LoadingPage(currentState.content, currentState.page + 1)
                else -> throw IllegalStateException("$INTERNAL_ERROR Impossible to retry in state: $currentState")
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
                val response = requestFactory.create(
                    limit = pageSize,
                    offset = (page) * pageSize,
                    sourceIndex = currentSourceIndex
                )
                require(response.totalPages >= page) { "Loaded page number: $page is more than total pages: ${response.totalPages}" }
                onPageLoaded(
                    items = response.list,
                    page = page,
                    isLastPage = response.totalPages == page
                )
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                onPageLoadingError(throwable, page)
            }
        }
    }

    private fun onPageLoaded(items: List<T>, page: Int, isLastPage: Boolean) {
        updateState { currentState ->
            when {
                items.isEmpty() && currentState.content.isEmpty() -> EmptyData(
                    content = emptyList(),
                    page = currentState.page
                )
                isLastPage && isLastSource -> FullContent(
                    content = currentState.content + items,
                    page = currentState.page
                )
                else -> Content(
                    content = currentState.content + items,
                    page = page,
                    isLastPage = isLastPage
                )
            }
        }
        //Trying to load next page automatically in case of EmptyData.
        if (state.value is EmptyData) {
            onItemReached(0)
        }
    }

    private fun onPageLoadingError(error: Throwable, page: Int) = updateState { currentState ->
        when {
            currentState.content.isNotEmpty() -> LoadingPageError(
                error = error,
                content = currentState.content,
                page = page - 1
            )
            else -> EmptyError(error, initialPage)
        }
    }

    private fun checkoutNextSourceAndUpdateState() {
        require(currentSourceIndex < sources.lastIndex) { "$INTERNAL_ERROR Current source is out of bound" }
        currentSourceIndex++
        val newSource = sources[currentSourceIndex]
        updateState { currentState ->
            when (currentState) {
                is EmptyData -> EmptyLoading(page = newSource.initialPage)
                is Content -> LoadingPage(
                    page = newSource.initialPage,
                    content = currentState.content
                )
                else -> throw IllegalStateException("$INTERNAL_ERROR Impossible to change state when state is: $currentState")
            }
        }
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
        override val page: Int,
        val isLastPage: Boolean
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
    suspend fun create(
        limit: Int,
        offset: Int,
        sourceIndex: Int
    ): Page<T>
}