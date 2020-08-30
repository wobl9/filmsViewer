@file:JvmName("EmptyContentDelegateKt")

package ru.wobcorp.filmsviewer.utils.list

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import ru.wobcorp.filmsviewer.databinding.ViewEmptyDataBinding

typealias EmptyResId = Int

fun emptyContentDelegate(
) = adapterDelegateViewBinding<EmptyResId, Any, ViewEmptyDataBinding>(
    { layoutInflater, root -> ViewEmptyDataBinding.inflate(layoutInflater, root, false) }
) {
    bind {
        binding.emptyDataText.text = context.getString(item)
    }
}