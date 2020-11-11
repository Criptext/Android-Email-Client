package com.criptext.mail.utils.generaldatasource.data

import android.content.ContentResolver
import android.graphics.Bitmap
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.CustomDomain
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.scenes.settings.data.AliasData
import com.criptext.mail.scenes.settings.data.SettingsRequest
import com.criptext.mail.scenes.settings.data.SettingsResult
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.ContactUtils
import com.criptext.mail.utils.UIMessage

sealed class GeneralRequest {
    data class DeviceRemoved(val letAPIKnow: Boolean): GeneralRequest()
    data class ConfirmPassword(val password: String): GeneralRequest()
    data class ResetPassword(val recipientId: String, val domain: String): GeneralRequest()
    data class BackgroundAccountsUpdateMailbox(
            val accounts: List<Account>,
            val label: Label): GeneralRequest()
    data class ActiveAccountUpdateMailbox(val label: Label): GeneralRequest()
    data class LinkAccept(val untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo): GeneralRequest()
    data class LinkDenied(val untrustedDeviceInfo: DeviceInfo.UntrustedDeviceInfo): GeneralRequest()
    class DataFileCreation(val recipientId: String, val domain: String): GeneralRequest()
    data class PostUserData(val deviceID: Int, val filePath: String, val key: ByteArray,
                            val randomId: String,
                            val keyBundle: PreKeyBundleShareData.DownloadBundle?,
                            val activeAccount: ActiveAccount): GeneralRequest()
    data class TotalUnreadEmails(val currentLabel: String): GeneralRequest()
    data class SyncPhonebook(val contentResolver: ContentResolver): GeneralRequest()
    data class Logout(val shouldDeleteAllData: Boolean, val letAPIKnow: Boolean): GeneralRequest()
    data class DeleteAccount(val password: String): GeneralRequest()
    data class SetReadReceipts(val readReceipts: Boolean): GeneralRequest()
    data class CheckForKeyBundle(val deviceId: Int): GeneralRequest()
    data class LinkData(val key: String, val dataAddress: String, val authorizerId: Int): GeneralRequest()
    class LinkDataReady: GeneralRequest()
    class SyncBegin: GeneralRequest()
    class SyncStatus: GeneralRequest()
    data class SyncAccept(val trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo): GeneralRequest()
    data class SyncDenied(val trustedDeviceInfo: DeviceInfo.TrustedDeviceInfo): GeneralRequest()
    data class ResendEmail(val emailId: Long, val position: Int): GeneralRequest()
    data class ChangeContactName(val fullName: String, val recipientId: String, val domain: String) : GeneralRequest()
    class GetRemoteFile(val uris: List<String>, val contentResolver: ContentResolver): GeneralRequest()
    data class Set2FA(val twoFA: Boolean): GeneralRequest()
    class ChangeToNextAccount: GeneralRequest()
    class GetUserSettings: GeneralRequest()
    data class LinkCancel(val recipientId: String, val domain: String, val jwt: String, val deviceId: Int?): GeneralRequest()
    class SyncCancel: GeneralRequest()
    data class RestoreMailbox(val filePath: String, val passphrase: String?, val isLocal: Boolean = false): GeneralRequest()
    data class Report(val emails: List<String>, val type: ContactUtils.ContactReportTypes): GeneralRequest()
    data class UserEvent(val event: Int): GeneralRequest()
    data class GetEmailPreview(val threadId: String, val userEmail: String, val doReply: Boolean = false,
                               val activityMessage: ActivityMessage? = null): GeneralRequest()
    data class SetActiveAccountFromPush(val recipientId: String, val domain: String, val extras: IntentExtrasData) : GeneralRequest()
    data class UpdateLocalDomainAndAliasData(val customDomains: List<CustomDomain>, val aliases: List<AliasData>) : GeneralRequest()
    data class ChangeBlockRemoteContentSetting(val newBlockRemoteContent: Boolean) : GeneralRequest()
    class ResendConfirmationLink: GeneralRequest()
    data class SetProfilePicture(val image: Bitmap): GeneralRequest()
    data class SetCloudBackupActive(val cloudBackupData: CloudBackupData): GeneralRequest()
}