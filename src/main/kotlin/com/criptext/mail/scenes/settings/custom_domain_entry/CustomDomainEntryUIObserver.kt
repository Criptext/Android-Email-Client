package com.criptext.mail.scenes.settings.custom_domain_entry

import com.criptext.mail.utils.uiobserver.UIObserver

interface CustomDomainEntryUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onNextButtonPressed()
    fun onDomainTextChanged(text: String)
}