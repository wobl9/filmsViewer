package ru.wobcorp.filmsviewer.utils.list

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

@SuppressLint("DiffUtilEquals")
class AnyEqualsDiffUtilItemCallback <T : Any> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
}