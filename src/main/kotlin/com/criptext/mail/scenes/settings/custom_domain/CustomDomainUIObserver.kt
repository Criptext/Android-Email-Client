package com.criptext.mail.scenes.settings.custom_domain

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class CustomDomainUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onRemoveDomain(domainName: String, position: Int)
    abstract fun onRemoveDomainConfirmed(domainName: String, position: Int)
    abstract fun onValidateDomainPressed(domainName: String, position: Int)
    abstract fun onRemoveDeviceCancel()
}