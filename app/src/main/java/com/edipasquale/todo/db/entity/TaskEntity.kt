package com.edipasquale.todo.db.entity

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tasks", indices = [Index(value = ["id"], unique = true)])
data class TaskEntity(
    @NonNull @PrimaryKey(autoGenerate = true)
    var _id: Long = 0,
    @Nullable
    val id: String? = null,
    @NonNull
    val name: String,
    @Nullable
    val note: String = "",
    @NonNull
    val isDone: Boolean = false
) {
    fun isSynced() = id != null
}