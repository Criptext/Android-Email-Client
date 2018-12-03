package com.criptext.mail.utils.generaldatasource.data

import android.content.ContentResolver
import com.criptext.mail.api.models.UntrustedDeviceInfo
import com.criptext.mail.db.models.Label
import com.criptext.mail.signal.PreKeyBundleShareData

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
    data class PostUserData(val deviceID: Int, val filePath: String, val key: ByteArray,
                            val randomId: String,
                            val keyBundle: PreKeyBundleShareData.DownloadBundle?): GeneralRequest()
    data class TotalUnreadEmails(val currentLabel: String): GeneralRequest()
    data class SyncPhonebook(val contentResolver: ContentResolver): GeneralRequest()
    data class Logout(val shouldDeleteAllData: Boolean): GeneralRequest()
    data class DeleteAccount(val password: String): GeneralRequest()
}