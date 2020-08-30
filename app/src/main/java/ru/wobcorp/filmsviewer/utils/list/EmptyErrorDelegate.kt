package ru.wobcorp.filmsviewer.utils.list

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import ru.wobcorp.filmsviewer.databinding.EmptyLoadingErrorDelegateBinding

object EmptyError

fun emptyLoadingErrorDelegate(
    action: () -> Unit
) = adapterDelegateViewBinding<EmptyError, Any, EmptyLoadingErrorDelegateBinding>(
    { layoutInflater, root ->
        EmptyLoadingErrorDelegateBinding.inflate(
            layoutInflater,
            root,
            false
        )
    }
) {
    binding.retryButton.setOnClickListener { action.invoke() }
}