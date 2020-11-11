package com.criptext.mail.scenes.import_mailbox

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver


abstract class ImportMailboxUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onAnotherDevicePressed()
    abstract fun onFromCloudPressed()
    abstract fun onFromFilePressed()
    abstract fun onSkipPressed()
    abstract fun onSkipContinuePressed()
}
