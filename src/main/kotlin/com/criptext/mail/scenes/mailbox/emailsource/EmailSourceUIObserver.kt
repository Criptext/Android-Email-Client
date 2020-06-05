package com.criptext.mail.scenes.mailbox.emailsource

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class EmailSourceUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
}