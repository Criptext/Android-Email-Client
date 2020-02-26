package com.criptext.mail.scenes.settings.custom_domain

import com.criptext.mail.utils.uiobserver.UIObserver

interface CustomDomainUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onRemoveDomain(domainName: String, position: Int)
    fun onRemoveDomainConfirmed(domainName: String, position: Int)
    fun onRemoveDeviceCancel()
}