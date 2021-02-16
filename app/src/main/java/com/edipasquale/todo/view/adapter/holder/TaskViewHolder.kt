package com.edipasquale.todo.view.adapter.holder

import androidx.recyclerview.widget.RecyclerView
import com.edipasquale.todo.databinding.HolderTaskBinding
import com.edipasquale.todo.db.entity.TaskEntity

class TaskViewHolder(
    private val _binding: HolderTaskBinding
) : RecyclerView.ViewHolder(_binding.root) {

    fun onBind(task: TaskEntity) {
        _binding.task = task
    }
}