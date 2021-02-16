package com.edipasquale.todo.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.edipasquale.todo.R
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.view.adapter.holder.TaskViewHolder

class TasksAdapter: ListAdapter<TaskEntity, TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.holder_task,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
    }

    private object DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {

        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return newItem.id == oldItem.id || newItem._id == oldItem._id
        }

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem == newItem
        }
    }
}