package ru.wobcorp.filmsviewer.presentation.filmslist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.bumptech.glide.Glide
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import ru.wobcorp.filmsviewer.R
import ru.wobcorp.filmsviewer.databinding.FilmsListFragmentBinding
import ru.wobcorp.filmsviewer.databinding.ViewFilmItemBinding
import ru.wobcorp.filmsviewer.domain.FilmModel
import ru.wobcorp.filmsviewer.presentation.filmslist.di.DaggerFilmsListComponent
import ru.wobcorp.filmsviewer.presentation.models.FilmsUiList
import ru.wobcorp.filmsviewer.presentation.models.FilmsUiList.FilmUi
import ru.wobcorp.filmsviewer.presentation.models.toUi
import ru.wobcorp.filmsviewer.utils.glide.RemoteImage
import ru.wobcorp.filmsviewer.utils.list.*
import ru.wobcorp.filmsviewer.utils.pagination.PaginationState
import timber.log.Timber
import javax.inject.Inject

class FilmsListFragment : Fragment() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val viewModel by viewModels<FilmsListViewModelImpl> { factory }

    private var _binding: FilmsListFragmentBinding? = null

    private val binding
        get() = requireNotNull(_binding)

    private val adapter = AsyncListDifferDelegationAdapter<Any>(
        AnyEqualsDiffUtilItemCallback(),
        filmsDelegate(onClick = { filmUi ->
            viewModel.filmClicked(filmUi.film)
        }),
        loadingPageDelegate(),
        emptyLoadingDelegate(),
        emptyContentDelegate(),
        loadingPageErrorDelegate(action = { viewModel.retryLoadPage() }),
        emptyLoadingErrorDelegate(action = { viewModel.retryLoadPage() })
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerFilmsListComponent.factory().create(requireContext()).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FilmsListFragmentBinding.inflate(inflater)
        binding.apply {

            filmsRecycler.adapter = adapter

            swipeLayout.setOnRefreshListener {
                viewModel.refresh()
            }

            filmsRecycler.onItemReached { viewModel.onReachedItemPosition(it) }
        }

        viewModel.paginationState.observe(viewLifecycleOwner, Observer { state ->
            handleState(state)
        })

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun handleState(state: PaginationState<FilmModel>) {
        adapter.items = when (state) {
            PaginationState.Empty -> emptyList()
            PaginationState.EmptyLoading -> listOf(EmptyLoading)
            PaginationState.EmptyData -> listOf(R.string.empty_content)
            is PaginationState.EmptyError -> listOf(EmptyError)
            is PaginationState.Content -> state.content.map { it.toUi() }
            is PaginationState.FullContent -> state.content.map { it.toUi() }
            is PaginationState.Refreshing -> listOf(EmptyLoading)
            is PaginationState.LoadingPage -> state.content.map { it.toUi() } + listOf(FilmsUiList.LoadingPage)
            is PaginationState.LoadingPageError -> {
                state.content.map { it.toUi() } + listOf(LoadingPageError)
            }
        }
        binding.swipeLayout.isRefreshing = false
        Timber.d("$state  size: ${adapter.items}")
    }

    private fun filmsDelegate(
        onClick: (FilmUi) -> Unit
    ) = adapterDelegateViewBinding<FilmUi, Any, ViewFilmItemBinding>(
        { layoutInflater, root -> ViewFilmItemBinding.inflate(layoutInflater, root, false) }
    ) {
        itemView.setOnClickListener { onClick(item) }
        bind {
            val film = item.film
            binding.apply {
                filmTitleText.text = film.title
                Glide.with(posterImage)
                    .load(RemoteImage(film.imageLink))
                    .into(posterImage)
            }
        }
    }

    private inline fun RecyclerView.onItemReached(crossinline action: (position: Int) -> Unit) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, state: Int) {
                super.onScrollStateChanged(recyclerView, state)
                if (state == SCROLL_STATE_DRAGGING) return
                val position =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                action.invoke(position)
            }
        })
    }
}