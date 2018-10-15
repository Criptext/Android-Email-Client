package com.criptext.mail.scenes.linking

import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.uiobserver.UIObserver


interface LinkingUIObserver: UIObserver {
    fun onBackButtonPressed()
    fun onLinkingHasFinished()
    fun onCancelSync()
    fun onKeepWaitingOk()
    fun onKeepWaitingCancel()
    fun onRetrySyncOk(result: GeneralResult)
    fun onRetrySyncCancel()
}