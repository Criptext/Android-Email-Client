package com.criptext.mail.scenes.settings.custom_domain_entry.domainconfiguration

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class DomainConfigurationUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onNextButtonPressed()
    abstract fun onCopyButtonClicked(text: String)
}