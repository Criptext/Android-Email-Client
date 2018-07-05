package com.email.scenes.emaildetail.ui

/**
 * Created by gabriel on 2/28/18.
 */

interface EmailDetailUIObserver {
    fun onBackButtonPressed()
    fun onStarredButtonPressed(isStarred: Boolean)
}