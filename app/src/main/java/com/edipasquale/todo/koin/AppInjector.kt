package com.edipasquale.todo.koin

import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.apollographql.apollo.ApolloClient
import com.edipasquale.todo.BuildConfig
import com.edipasquale.todo.db.DATABASE_NAME
import com.edipasquale.todo.db.TaskDatabase
import com.edipasquale.todo.repository.AuthRepository
import com.edipasquale.todo.repository.TasksRepository
import com.edipasquale.todo.source.network.tasks.NetworkTasksSource
import com.edipasquale.todo.source.local.LocalTasksSource
import com.edipasquale.todo.source.local.RoomSourceImpl
import com.edipasquale.todo.source.network.auth.NetworkAuthSource
import com.edipasquale.todo.source.network.tasks.impl.GraphQLSourceImpl
import com.edipasquale.todo.source.network.auth.interceptor.AuthInterceptor
import com.edipasquale.todo.source.network.auth.impl.GraphQLAuthSource
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
import org.koin.dsl.bind
import org.koin.dsl.module

private const val CONST_SERVER_URL = "CONST_SERVER_URL"

class AppInjector {
    companion object {
        private val appModule = module {
            viewModel { TasksViewModel(androidApplication(), get()) }
            viewModel { CreateTaskViewModel(androidApplication(), get()) }
            viewModel { AuthViewModel(androidApplication(), get()) }

            factory { TasksRepository(get(), get(), get()) }
            factory { AuthRepository(get()) }
            factory { GraphQLSourceImpl(get()) } bind NetworkTasksSource::class
            factory { RoomSourceImpl(get()) } bind LocalTasksSource::class
            factory { GraphQLAuthSource(get()) } bind NetworkAuthSource::class

            single { get<TaskDatabase>().tasksDao() }
            single { WorkManager.getInstance(androidApplication())}
            single {
                Room.databaseBuilder(androidContext(), TaskDatabase::class.java, DATABASE_NAME)
                    .build()
            }

            // Apollo Client
            single<ApolloClient> {
                ApolloClient.builder()
                    .serverUrl(get<String>(named(CONST_SERVER_URL)))
                    .okHttpClient(
                        OkHttpClient.Builder()
                            .addInterceptor(AuthInterceptor(androidContext()))
                            .build()
                    )
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