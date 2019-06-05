package com.criptext.mail.androidtest

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager

/**
 * Activity to run tests on a real device or emulator. Should be as minimalist as possible
 * Created by gabriel on 5/24/18.
 */

class TestActivity: Activity() {

    private fun keepAwake() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepAwake()
    }
}