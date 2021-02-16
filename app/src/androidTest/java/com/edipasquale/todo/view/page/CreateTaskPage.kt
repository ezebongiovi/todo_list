package com.edipasquale.todo.view.page

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.edipasquale.todo.R
import com.edipasquale.todo.view.activity.MainActivity

class CreateTaskPage {
    private lateinit var scenario: ActivityScenario<MainActivity>

    fun launch(): CreateTaskPage {
        scenario = launchActivity()

        return this
    }

    fun moveToState(state: Lifecycle.State): CreateTaskPageActions {
        scenario.moveToState(state)

        return CreateTaskPageActions()
    }

    class CreateTaskPageActions {

        fun checkVisibility() {
            Espresso.onView(withId(R.id.button_create_task))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}