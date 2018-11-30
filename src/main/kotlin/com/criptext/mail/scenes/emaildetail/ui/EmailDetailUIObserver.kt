package com.criptext.mail.scenes.emaildetail.ui

import android.view.View
import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

interface EmailDetailUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun showStartGuideEmailIsRead(view: View)
    fun showStartGuideMenu(view: View)
}