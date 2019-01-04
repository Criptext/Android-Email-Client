package com.criptext.mail.utils.uiobserver

import com.criptext.mail.api.models.TrustedDeviceInfo
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.utils.ui.data.DialogResult

interface UIObserver{
    fun onGeneralOkButtonPressed(result: DialogResult)
    fun onOkButtonPressed(password: String)
    fun onCancelButtonPressed()
    fun onLinkAuthConfirmed(untrustedDeviceInfo: UntrustedDeviceInfo)
    fun onLinkAuthDenied(untrustedDeviceInfo: UntrustedDeviceInfo)
    fun onSnackbarClicked()
    fun onSyncAuthConfirmed(trustedDeviceInfo: TrustedDeviceInfo)
    fun onSyncAuthDenied(trustedDeviceInfo: TrustedDeviceInfo)
}