package ru.wobcorp.filmsviewer.utils.list

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import ru.wobcorp.filmsviewer.databinding.LoadingPageErrorItemBinding

object LoadingPageError

fun loadingPageErrorDelegate(action: () -> Unit) =
    adapterDelegateViewBinding<LoadingPageError, Any, LoadingPageErrorItemBinding>(
        { layoutInflater, root -> LoadingPageErrorItemBinding.inflate(layoutInflater, root, false) }
    ) {
        binding.errorButton.setOnClickListener { action.invoke() }
    }