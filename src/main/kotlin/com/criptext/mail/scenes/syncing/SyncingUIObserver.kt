package com.criptext.mail.scenes.syncing

import com.criptext.mail.utils.generaldatasource.data.GeneralResult


interface SyncingUIObserver {
    fun onBackButtonPressed()
    fun onLinkingHasFinished()
    fun onResendDeviceLinkAuth(username: String)
    fun onBackPressed()
    fun onRetrySyncOk(result: GeneralResult)
    fun onRetrySyncCancel()
}
