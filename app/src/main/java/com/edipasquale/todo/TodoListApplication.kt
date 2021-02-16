package com.edipasquale.todo

import android.app.Application
import com.edipasquale.todo.koin.AppInjector

class TodoListApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        AppInjector.start(this)
    }
}