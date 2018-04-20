package com.email.utils

import android.app.Activity
import android.content.Context
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

/**
 * Class used to control the android soft-keyboard
 * Created by gabriel on 5/18/17.
 */

class KeyboardManager(val act: Activity) {
    val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    fun hideKeyboard() {
        val focusedView = act.currentFocus
        if (focusedView != null) {
            imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            focusedView.clearFocus()
        }
        act.window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
    }

    fun showKeyboard(inputView: View) {
        imm.showSoftInput(inputView, InputMethodManager.SHOW_IMPLICIT)
    }
}