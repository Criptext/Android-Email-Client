package com.criptext.mail.scenes.settings.privacy

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.uiobserver.UIObserver

abstract class PrivacyUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onTwoFASwitched(isChecked: Boolean)
    abstract fun onReadReceiptsSwitched(isChecked: Boolean)
    abstract fun onBlockRemoteContentSwitched(isChecked: Boolean)
}