package com.criptext.mail.scenes.settings.privacyandsecurity.pinscreen

import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource

interface LockScreenUIObserver {
    fun onForgotPinYesPressed(dataSource: GeneralDataSource)
}