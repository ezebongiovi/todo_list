package com.edipasquale.todo.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrefUtilsTest {

    @Test
    fun saveToken() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val insertedToken = "Token-XXX"

        PrefUtils.saveAccessToken(context, insertedToken)

        assertEquals(insertedToken, PrefUtils.getAccessToken(context))
    }

    @Test(expected = AssertionError::class)
    fun instantiate() {
        PrefUtils()
    }
}