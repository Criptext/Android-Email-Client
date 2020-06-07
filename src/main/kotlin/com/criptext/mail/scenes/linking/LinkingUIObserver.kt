package com.criptext.mail.scenes.linking

import com.criptext.mail.IHostActivity
import com.criptext.mail.utils.generaldatasource.data.GeneralDataSource
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.uiobserver.UIObserver


abstract class LinkingUIObserver(generalDataSource: GeneralDataSource, host: IHostActivity): UIObserver(generalDataSource, host) {
    abstract fun onBackButtonPressed()
    abstract fun onLinkingHasFinished()
    abstract fun onCancelSync()
    abstract fun onKeepWaitingOk()
    abstract fun onKeepWaitingCancel()
    abstract fun onRetrySyncOk(result: GeneralResult)
    abstract fun onRetrySyncCancel()
}