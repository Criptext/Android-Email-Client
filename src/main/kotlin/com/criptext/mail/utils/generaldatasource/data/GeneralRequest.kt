package com.criptext.mail.utils.generaldatasource.data

import android.content.ContentResolver
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.signal.PreKeyBundleShareData

sealed class GeneralRequest {
    data class DeviceRemoved(val letAPIKnow: Boolean): GeneralRequest()
    data class ConfirmPassword(val password: String): GeneralRequest()
    data class ResetPassword(val recipientId: String, val domain: String): GeneralRequest()
    data class UpdateMailbox(
            val isActiveAccount: Boolean,
            val recipientId: String,
            val domain: String,
            val label: Label,
            val loadedThreadsCount: Int): GeneralRequest()
    data class LinkAccept(val untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo): GeneralRequest()
    data class LinkDenied(val untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo): GeneralRequest()
    class DataFileCreation(val recipientId: String, val domain: String): GeneralRequest()
    data class PostUserData(val deviceID: Int, val filePath: String, val key: ByteArray,
                            val randomId: String,
                            val keyBundle: PreKeyBundleShareData.DownloadBundle?,
                            val activeAccount: ActiveAccount): GeneralRequest()
    data class TotalUnreadEmails(val currentLabel: String): GeneralRequest()
    data class SyncPhonebook(val contentResolver: ContentResolver): GeneralRequest()
    data class Logout(val shouldDeleteAllData: Boolean): GeneralRequest()
    data class DeleteAccount(val password: String): GeneralRequest()
    data class SetReadReceipts(val readReceipts: Boolean): GeneralRequest()
    data class CheckForKeyBundle(val deviceId: Int): GeneralRequest()
    data class LinkData(val key: String, val dataAddress: String, val authorizerId: Int): GeneralRequest()
    class LinkDataReady: GeneralRequest()
    class SyncStatus: GeneralRequest()
    data class SyncAccept(val trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo): GeneralRequest()
    data class SyncDenied(val trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo): GeneralRequest()
    data class ResendEmail(val emailId: Long, val position: Int): GeneralRequest()
    data class ChangeContactName(val fullName: String, val recipientId: String, val domain: String) : GeneralRequest()
    class GetRemoteFile(val uris: List<String>, val contentResolver: ContentResolver): GeneralRequest()
    data class Set2FA(val twoFA: Boolean): GeneralRequest()
    class ChangeToNextAccount: GeneralRequest()
}