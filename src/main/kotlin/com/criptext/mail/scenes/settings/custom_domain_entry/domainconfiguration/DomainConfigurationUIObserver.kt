package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration

import com.criptext.mail.utils.uiobserver.UIObserver

interface DomainConfigurationUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onNextButtonPressed()
    fun onCopyButtonClicked(text: String)
}