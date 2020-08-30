package ru.wobcorp.filmsviewer.utils.list

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import ru.wobcorp.filmsviewer.databinding.ViewEmptyLoadingBinding

object EmptyLoading

fun emptyLoadingDelegate(
) = adapterDelegateViewBinding<EmptyLoading, Any, ViewEmptyLoadingBinding>(
    { layoutInflater, root -> ViewEmptyLoadingBinding.inflate(layoutInflater, root, false) }
) { Unit }


