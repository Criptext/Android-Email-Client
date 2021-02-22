package com.criptext.mail.scenes.syncing

import com.criptext.mail.utils.generaldatasource.data.GeneralResult


interface SyncingUIObserver {
    fun onBackButtonPressed()
    fun onLinkingHasFinished()
    fun onSubmitButtonPressed()
    fun onRetrySyncOk(result: GeneralResult)
    fun onRetrySyncCancel()
}
