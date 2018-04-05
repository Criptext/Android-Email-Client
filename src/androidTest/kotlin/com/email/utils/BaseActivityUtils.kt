package com.email.utils

import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.email.BaseActivity
import com.email.scenes.ActivityMessage

/**
 * Created by gabriel on 4/4/18.
 */
fun BaseActivity.startOnUiThread(activityMessage: ActivityMessage?) {
    runOnUiThread { controller.onStart(activityMessage) }
}

fun didLaunchActivity(activityClass: Class<*>) {
    intended(hasComponent(activityClass.name))
}
