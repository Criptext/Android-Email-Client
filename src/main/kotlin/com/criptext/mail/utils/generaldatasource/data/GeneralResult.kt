package com.criptext.mail.utils.generaldatasource.data


import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.api.models.SyncStatusData
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.push.data.IntentExtrasData
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData
import com.criptext.mail.scenes.settings.cloudbackup.data.CloudBackupData
import com.criptext.mail.scenes.settings.data.UserSettingsData
import com.criptext.mail.scenes.settings.profile.data.ProfileResult
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.eventhelper.EventHelperResultData
import com.criptext.mail.utils.UIMessage

sealed class GeneralResult {
    sealed class DeviceRemoved: GeneralResult()  {
        data class Success(val activeAccount: ActiveAccount?): DeviceRemoved()
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

    sealed class BackgroundAccountsUpdateMailbox : GeneralResult() {
        abstract fun getDestinationMailbox(): Label
        data class Success(
                val shouldUpdateUI: Boolean,
                val mailboxLabel: Label,
                val updateBannerData: UpdateBannerData?,
                val syncEventsList: List<DeviceInfo?>,
                val isManual: Boolean) : BackgroundAccountsUpdateMailbox() {

            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : BackgroundAccountsUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Unauthorized(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : BackgroundAccountsUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Forbidden(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : BackgroundAccountsUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class EnterpriseSuspended(val mailboxLabel: Label): BackgroundAccountsUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }
    }

    sealed class ActiveAccountUpdateMailbox : GeneralResult() {
        abstract fun getDestinationMailbox(): Label
        data class Success(
                val shouldNotify: Boolean,
                val mailboxLabel: Label,
                val data: EventHelperResultData?,
                val isManual: Boolean) : ActiveAccountUpdateMailbox() {

            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class SuccessAndRepeat(
                val shouldNotify: Boolean,
                val mailboxLabel: Label,
                val data: EventHelperResultData?,
                val isManual: Boolean) : ActiveAccountUpdateMailbox() {

            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : ActiveAccountUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class Unauthorized(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : ActiveAccountUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        class SessionExpired: ActiveAccountUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return Label.defaultItems.inbox
            }
        }

        data class Forbidden(
                val mailboxLabel: Label,
                val message: UIMessage,
                val exception: Exception?) : ActiveAccountUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }

        data class EnterpriseSuspended(val mailboxLabel: Label): ActiveAccountUpdateMailbox() {
            override fun getDestinationMailbox(): Label {
                return mailboxLabel
            }
        }
    }

    sealed class LinkAccept: GeneralResult() {
        data class Success(val linkAccount: ActiveAccount, val deviceId: Int, val uuid: String, val deviceType: DeviceUtils.DeviceType): LinkAccept()
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
        data class Success(val activeAccountTotal: Int, val extraAccountsData: List<Pair<String, Int>>): TotalUnreadEmails()
        data class Failure(val message: UIMessage): TotalUnreadEmails()
    }

    sealed class SyncPhonebook: GeneralResult() {
        class Success: SyncPhonebook()
        data class Failure(val exception: Exception, val message: UIMessage): SyncPhonebook()
    }

    sealed class Logout: GeneralResult() {
        data class Success(val activeAccount: ActiveAccount?, val oldAccountEmail: String, val oldAccountId: Long): Logout()
        class Failure: Logout()
    }

    sealed class DeleteAccount: GeneralResult() {
        data class Success(val activeAccount: ActiveAccount?): DeleteAccount()
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
        data class Progress(val message: UIMessage, val progress: Int, val drawable: Int? = null ): LinkData()
        data class Failure(val message: UIMessage,
                           val exception: Exception): LinkData()
    }

    sealed class SyncBegin: GeneralResult() {
        class Success: SyncBegin()
        data class Failure(val message: UIMessage): SyncBegin()
    }

    sealed class SyncStatus: GeneralResult() {
        data class Success(val syncStatusData: SyncStatusData): SyncStatus()
        class Waiting: SyncStatus()
        class Denied: SyncStatus()
    }

    sealed class SyncAccept: GeneralResult() {
        data class Success(val syncAccount: ActiveAccount, val deviceId: Int, val uuid: String, val deviceType: DeviceUtils.DeviceType): SyncAccept()
        data class Failure(val message: UIMessage): SyncAccept()
    }

    sealed class SyncDeny: GeneralResult() {
        class Success: SyncDeny()
        data class Failure(val message: UIMessage): SyncDeny()
    }

    sealed class ResendEmail: GeneralResult() {
        data class Success(val position: Int, val isSecure: Boolean): ResendEmail()
        data class Failure(val message: UIMessage): ResendEmail()
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

    sealed class Set2FA: GeneralResult() {
        data class Success(val hasTwoFA: Boolean): Set2FA()
        data class Failure(val message: UIMessage, val twoFAAttempt: Boolean): Set2FA()
    }

    sealed class ChangeToNextAccount: GeneralResult() {
        data class Success(val activeAccount: ActiveAccount): ChangeToNextAccount()
        class Failure: ChangeToNextAccount()
    }

    sealed class GetUserSettings : GeneralResult() {
        data class Success(val userSettings: UserSettingsData): GetUserSettings()
        data class Failure(val message: UIMessage): GetUserSettings()
        data class Unauthorized(val message: UIMessage): GetUserSettings()
        class SessionExpired(): GetUserSettings()
        class Forbidden: GetUserSettings()
        class EnterpriseSuspended: GetUserSettings()
    }

    sealed class LinkCancel: GeneralResult() {
        class Success: LinkCancel()
        data class Failure(val message: UIMessage): LinkCancel()
    }

    sealed class SyncCancel: GeneralResult() {
        class Success : SyncCancel()
        data class Failure(val message: UIMessage) : SyncCancel()
    }

    sealed class RestoreMailbox : GeneralResult() {
        class Success: RestoreMailbox()
        data class Progress(val progress: Int): RestoreMailbox()
        data class SyncError(val message: UIMessage) : RestoreMailbox()
        data class Failure(val message: UIMessage) : RestoreMailbox()
    }

    sealed class Report: GeneralResult() {
        class Success : Report()
        data class Failure(val message: UIMessage) : Report()
    }

    sealed class UserEvent: GeneralResult() {
        class Success : UserEvent()
        data class Failure(val message: UIMessage) : UserEvent()
    }

    sealed class GetEmailPreview: GeneralResult() {
        data class Success(val emailPreview: EmailPreview,
                           val isTrash: Boolean, val isSpam: Boolean,
                           val doReply: Boolean = false,
                           val activityMessage: ActivityMessage? = null): GetEmailPreview()
        data class Failure(val message: UIMessage): GetEmailPreview()
    }

    sealed class SetActiveAccountFromPush : GeneralResult() {
        data class Success(val activeAccount: ActiveAccount, val extrasData: IntentExtrasData): SetActiveAccountFromPush()
        class Failure: SetActiveAccountFromPush()
    }

    sealed class UpdateLocalDomainAndAliasData: GeneralResult() {
        class Success : UpdateLocalDomainAndAliasData()
        data class Failure(val message: UIMessage) : UpdateLocalDomainAndAliasData()
    }

    sealed class ChangeBlockRemoteContentSetting: GeneralResult() {
        data class Success(val newBlockRemoteContent: Boolean): ChangeBlockRemoteContentSetting()
        data class Failure(val newBlockRemoteContent: Boolean, val message: UIMessage) : ChangeBlockRemoteContentSetting()
    }

    sealed class ResendConfirmationLink: GeneralResult() {
        class Success: ResendConfirmationLink()
        data class Failure(val message: UIMessage): ResendConfirmationLink()
    }


    sealed class SetProfilePicture : GeneralResult() {
        class Success: SetProfilePicture()
        data class Failure(val message: UIMessage,
                           val exception: Exception?): SetProfilePicture()
        class EnterpriseSuspended: SetProfilePicture()
    }

    sealed class SetCloudBackupActive : GeneralResult() {
        data class Success(val cloudBackupData: CloudBackupData): SetCloudBackupActive()
        data class Failure(val message: UIMessage,
                           val exception: Exception?,
                           val cloudBackupData: CloudBackupData): SetCloudBackupActive()
    }
}