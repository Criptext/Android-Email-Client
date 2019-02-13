package com.criptext.mail.utils.generaldatasource.data


import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData
import com.criptext.mail.scenes.signin.data.LinkStatusData
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage
import java.io.File

/**
 * Created by gabriel on 5/1/18.
 */
sealed class GeneralResult {
    sealed class DeviceRemoved: GeneralResult()  {
        class Success: DeviceRemoved()
        class Failure: DeviceRemoved()
    }

    sealed class ConfirmPassword: GeneralResult()  {
        class Success: ConfirmPassword()
        data class Failure(val message: UIMessage): ConfirmPassword()
    }

    sealed class ResetPassword : GeneralResult() {
        data class Success(val email: String): ResetPassword()
        data class Failure(val message: UIMessage): ResetPassword()
    }

    sealed class UpdateMailbox : GeneralResult() {
        abstract fun getDestinationMailbox(): Label
        data class Success(
                val shouldNotify: Boolean,
                val mailboxLabel: Label,
                val mailboxThreads: List<EmailPreview>?,
                val updateBannerData: UpdateBannerData?,
                val syncEventsList: List<DeviceInfo?>,
                val isManual: Boolean) : UpdateMailbox() {

            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class SuccessAndRepeat(
                val shouldNotify: Boolean,
                val mailboxLabel: Label,
                val mailboxThreads: List<EmailPreview>?,
                val updateBannerData: UpdateBannerData?,
                val syncEventsList: List<DeviceInfo?>,
                val isManual: Boolean) : UpdateMailbox() {

            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : UpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Unauthorized(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : UpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Forbidden(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : UpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }
    }

    sealed class LinkAccept: GeneralResult() {
        data class Success(val deviceId: Int, val uuid: String, val deviceType: DeviceUtils.DeviceType): LinkAccept()
        data class Failure(val message: UIMessage): LinkAccept()
    }

    sealed class LinkDeny: GeneralResult() {
        class Success: LinkDeny()
        data class Failure(val message: UIMessage): LinkDeny()
    }

    sealed class DataFileCreation: GeneralResult() {
        data class Success(val key: ByteArray, val filePath: String): DataFileCreation()
        data class Progress(val message: UIMessage, val progress: Int): DataFileCreation()
        data class Failure(val message: UIMessage): DataFileCreation()
    }

    sealed class PostUserData: GeneralResult() {
        class Success: PostUserData()
        data class Failure(val message: UIMessage): PostUserData()
    }

    sealed class TotalUnreadEmails: GeneralResult() {
        data class Success(val total: Int): TotalUnreadEmails()
        data class Failure(val message: UIMessage): TotalUnreadEmails()
    }

    sealed class SyncPhonebook: GeneralResult() {
        class Success: SyncPhonebook()
        data class Failure(val exception: Exception, val message: UIMessage): SyncPhonebook()
    }

    sealed class Logout: GeneralResult() {
        class Success: Logout()
        class Failure: Logout()
    }

    sealed class DeleteAccount: GeneralResult() {
        class Success: DeleteAccount()
        data class Failure(val message: UIMessage): DeleteAccount()
    }

    sealed class SetReadReceipts: GeneralResult() {
        data class Success(val readReceipts: Boolean): SetReadReceipts()
        data class Failure(val message: UIMessage, val readReceiptAttempt: Boolean): SetReadReceipts()
    }

    sealed class CheckForKeyBundle: GeneralResult() {
        data class Success(val keyBundle: PreKeyBundleShareData.DownloadBundle): CheckForKeyBundle()
        data class Failure(val message: UIMessage): CheckForKeyBundle()
    }

    sealed class LinkDataReady: GeneralResult() {
        data class Success(val key: String, val dataAddress: String): LinkDataReady()
        data class Failure(val message: UIMessage,
                           val exception: Exception): LinkDataReady()
    }

    sealed class LinkData: GeneralResult() {
        class Success: LinkData()
        data class Progress(val message: UIMessage, val progress: Int): LinkData()
        data class Failure(val message: UIMessage,
                           val exception: Exception): LinkData()
    }

    sealed class SyncStatus: GeneralResult() {
        data class Success(val syncStatusData: SyncStatusData): SyncStatus()
        class Waiting: SyncStatus()
        class Denied: SyncStatus()
    }

    sealed class SyncAccept: GeneralResult() {
        data class Success(val deviceId: Int, val uuid: String, val deviceType: DeviceUtils.DeviceType): SyncAccept()
        data class Failure(val message: UIMessage): SyncAccept()
    }

    sealed class SyncDeny: GeneralResult() {
        class Success: SyncDeny()
        data class Failure(val message: UIMessage): SyncDeny()
    }

    sealed class ResendEmail: GeneralResult() {
        data class Success(val position: Int): ResendEmail()
        class Failure: ResendEmail()
    }

    sealed class ChangeContactName : GeneralResult() {
        data class Success(val fullName: String): ChangeContactName()
        class Failure: ChangeContactName()
    }

    sealed class GetRemoteFile : GeneralResult() {
        data class Success(val remoteFiles: List<Pair<String, Long>>): GetRemoteFile()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): GetRemoteFile()
    }
}