package com.email.activity

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class DeckardActivityTest {

    @Test
    @Throws(Exception::class)
    fun testSomething() {
        assertNotNull(shadowOf(RuntimeEnvironment.application))
        assertTrue(Robolectric.setupActivity(DeckardActivity::class.java) != null)
    }
}
