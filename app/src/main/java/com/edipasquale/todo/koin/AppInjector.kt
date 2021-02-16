package com.edipasquale.todo.koin

import android.app.Application
import androidx.room.Room
import com.apollographql.apollo.ApolloClient
import com.edipasquale.todo.BuildConfig
import com.edipasquale.todo.db.DATABASE_NAME
import com.edipasquale.todo.db.TaskDao
import com.edipasquale.todo.db.TaskDatabase
import com.edipasquale.todo.repository.AuthRepository
import com.edipasquale.todo.repository.TasksRepository
import com.edipasquale.todo.source.local.LocalSource
import com.edipasquale.todo.source.local.LocalSourceImpl
import com.edipasquale.todo.source.network.GraphQLSource
import com.edipasquale.todo.source.network.GraphQLSourceImpl
import com.edipasquale.todo.source.network.interceptor.AuthInterceptor
import com.edipasquale.todo.viewmodel.AuthViewModel
import com.edipasquale.todo.viewmodel.CreateTaskViewModel
import com.edipasquale.todo.viewmodel.TasksViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val CONST_SERVER_URL = "CONST_SERVER_URL"

class AppInjector {
    companion object {
        @ExperimentalCoroutinesApi
        private val appModule = module {
            // ViewModels
            viewModel { TasksViewModel(androidApplication(), get()) }
            viewModel { CreateTaskViewModel(androidApplication(), get()) }
            viewModel { AuthViewModel(androidApplication(), get()) }

            // Repositories
            factory { TasksRepository(get(), get()) }
            factory { AuthRepository(get()) }

            // Sources
            factory<GraphQLSource> { GraphQLSourceImpl(get()) }
            factory<LocalSource> { LocalSourceImpl(get()) }

            // Database
            single<TaskDao> { get<TaskDatabase>().tasksDao() }
            single<TaskDatabase> {
                Room.databaseBuilder(androidContext(), TaskDatabase::class.java, DATABASE_NAME)
                    .build()
            }

            // Apollo Client
            single <ApolloClient> {
                ApolloClient.builder()
                    .serverUrl(get<String>(named(CONST_SERVER_URL)))
                    .okHttpClient(OkHttpClient.Builder()
                        .addInterceptor(AuthInterceptor(androidContext()))
                        .build())
                    .build()
            }

            single<String>(named(CONST_SERVER_URL)) { BuildConfig.SERVER_URL }
        }

        @ExperimentalCoroutinesApi
        fun start(application: Application) {

            startKoin {
                androidContext(application)
                modules(listOf(appModule))
            }
        }
    }
}