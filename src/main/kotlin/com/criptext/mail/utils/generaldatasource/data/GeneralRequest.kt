package com.criptext.mail.utils.generaldatasource.data

sealed class GeneralRequest {
    data class DeviceRemoved(val letAPIKnow: Boolean): GeneralRequest()
    data class ConfirmPassword(val password: String): GeneralRequest()
    data class ResetPassword(val recipientId: String): GeneralRequest()
}