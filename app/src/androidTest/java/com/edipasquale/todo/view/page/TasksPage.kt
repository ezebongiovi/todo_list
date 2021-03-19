package com.edipasquale.todo.view.page

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.edipasquale.todo.R
import com.edipasquale.todo.view.activity.MainActivity
import com.edipasquale.todo.view.fragment.TasksFragment

class TasksPage {
    private lateinit var _scenario: ActivityScenario<MainActivity>

    fun launchWithActivity(): TasksPage {
        _scenario = launchActivity()

        return this
    }

    fun launchWithFragment(fragment: TasksFragment): FragmentScenario<TasksFragment> =
        launchFragment(themeResId = R.style.Theme_TodoList) { fragment }

    fun moveToState(state: Lifecycle.State): ListOfTasksPageActions {
        _scenario.moveToState(state)

        return ListOfTasksPageActions(_scenario)
    }

    class ListOfTasksPageActions(private val _scenario: ActivityScenario<MainActivity>) {

        fun navigateToCreateTask(): CreateTaskPage.CreateTaskPageActions {
            Espresso.onView(withId(R.id.fab)).perform(ViewActions.click())

            return CreateTaskPage.CreateTaskPageActions()
        }

        fun scenario(): ActivityScenario<MainActivity> = _scenario
    }
}