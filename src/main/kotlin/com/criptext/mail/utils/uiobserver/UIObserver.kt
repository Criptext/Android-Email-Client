package com.criptext.mail.utils.uiobserver

import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.utils.ui.data.DialogResult

interface UIObserver{
    fun onGeneralOkButtonPressed(result: DialogResult)
    fun onGeneralCancelButtonPressed(result: DialogResult)
    fun onOkButtonPressed(password: String)
    fun onCancelButtonPressed()
    fun onLinkAuthConfirmed(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun onLinkAuthDenied(untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo)
    fun onSnackbarClicked()
    fun onSyncAuthConfirmed(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
    fun onSyncAuthDenied(trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo)
}