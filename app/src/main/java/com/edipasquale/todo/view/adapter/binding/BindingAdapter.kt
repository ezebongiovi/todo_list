package com.edipasquale.todo.view.adapter.binding

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("app:noteVisibility")
fun noteVisibility(view: View, note: String?) {
    view.visibility = if (note.isNullOrBlank())
        View.GONE
    else
        View.VISIBLE
}