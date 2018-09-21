package com.criptext.mail.scenes.linking

import com.criptext.mail.utils.uiobserver.UIObserver


interface LinkingUIObserver: UIObserver {
    fun onBackButtonPressed()
}