package ru.wobcorp.filmsviewer.utils.list

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import ru.wobcorp.filmsviewer.databinding.LoadingPageDelegateBinding
import ru.wobcorp.filmsviewer.presentation.models.FilmsUiList.LoadingPage

fun loadingPageDelegate() =
    adapterDelegateViewBinding<LoadingPage, Any, LoadingPageDelegateBinding>(
        { layoutInflater, root -> LoadingPageDelegateBinding.inflate(layoutInflater, root, false) }
    ) { Unit }