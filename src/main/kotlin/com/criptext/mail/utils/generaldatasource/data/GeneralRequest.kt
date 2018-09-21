package com.criptext.mail.utils.generaldatasource.data

import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.db.models.Label

sealed class GeneralRequest {
    data class DeviceRemoved(val letAPIKnow: Boolean): GeneralRequest()
    data class ConfirmPassword(val password: String): GeneralRequest()
    data class ResetPassword(val recipientId: String): GeneralRequest()
    data class UpdateMailbox(
            val label: Label,
            val loadedThreadsCount: Int): GeneralRequest()
    data class LinkAccept(val untrustedDeviceInfo: UntrustedDeviceInfo): GeneralRequest()
    data class LinkDenied(val untrustedDeviceInfo: UntrustedDeviceInfo): GeneralRequest()
    class DataFileCreation: GeneralRequest()
}