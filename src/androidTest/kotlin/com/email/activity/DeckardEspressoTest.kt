package com.email.activity

import com.email.R

import android.support.test.rule.ActivityTestRule

import org.junit.Rule
import org.junit.Test

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.runner.AndroidJUnit4
import com.email.splash.SplashActivity
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeckardEspressoTest {


    @get:Rule
    val mActivityRule = ActivityTestRule(SplashActivity::class.java)

    @Test
    @Throws(InterruptedException::class)
    fun testActivityShouldHaveText() {
        onView(withId(R.id.text)).check(matches(withText("Hello Espresso!")))
    }
}
