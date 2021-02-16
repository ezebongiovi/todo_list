package com.edipasquale.todo.view.page

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.edipasquale.todo.R
import com.edipasquale.todo.view.activity.MainActivity

class TasksPage {
    private lateinit var scenario: ActivityScenario<MainActivity>

    fun launch(): TasksPage {
        scenario = launchActivity()

        return this
    }

    fun moveToState(state: Lifecycle.State): ListOfTasksPageActions {
        scenario.moveToState(state)

        return ListOfTasksPageActions()
    }

    class ListOfTasksPageActions {

        fun navigateToCreateTask(): CreateTaskPage.CreateTaskPageActions {
            Espresso.onView(withId(R.id.fab)).perform(ViewActions.click())

            return CreateTaskPage.CreateTaskPageActions()
        }
    }
}