package com.criptext.mail.utils.uiobserver

import com.criptext.mail.api.models.UntrustedDeviceInfo

interface UIObserver{
    fun onOkButtonPressed(password: String)
    fun onCancelButtonPressed()
    fun onLinkAuthConfirmed(untrustedDeviceInfo: UntrustedDeviceInfo)
    fun onLinkAuthDenied(untrustedDeviceInfo: UntrustedDeviceInfo)
}