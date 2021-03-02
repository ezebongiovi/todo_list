package com.edipasquale.todo.source.network

import com.apollographql.apollo.ApolloClient
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.*
import com.edipasquale.todo.extensions.tasksAsEntities
import com.edipasquale.todo.extensions.toTaskEntity
import com.example.todolist.CreateTaskMutation
import com.example.todolist.GetAllTasksQuery

open class GraphQLSourceImpl(_apolloClient: ApolloClient) : GraphQLSource(_apolloClient) {

    override suspend fun createTask(task: TaskEntity): APIResult<TaskEntity, APIError> {
        val mutation = CreateTaskMutation(task.name, task.note, task.isDone)

        return when (val response = executeMutation(mutation)) {
            is Failure -> response
            is Success -> {
                val createTaskResponse = response.value.createTask

                if (createTaskResponse == null)
                    Failure(APIError(ERROR_INVALID_DATA))
                else
                    Success(createTaskResponse.toTaskEntity())
            }
        }
    }

    override suspend fun getAllTasks(): APIResult<List<TaskEntity>, APIError> {
        return when (val response = executeQuery(GetAllTasksQuery())) {
            is Success -> {
                Success(response.value.tasksAsEntities())
            }

            is Failure -> response
        }
    }
}
