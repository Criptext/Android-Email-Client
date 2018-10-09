package com.criptext.mail.utils.generaldatasource.data


import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.utils.UIMessage
import java.util.*

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
        class Failure: ConfirmPassword()
    }

    sealed class ResetPassword : GeneralResult() {
        data class Success(val email: String): ResetPassword()
        data class Failure(val message: UIMessage): ResetPassword()
    }

    sealed class UpdateMailbox : GeneralResult() {
        abstract fun getDestinationMailbox(): Label
        data class Success(
                val mailboxLabel: Label,
                val mailboxThreads: List<EmailPreview>?,
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
        data class Success(val deviceId: Int, val uuid: String): LinkAccept()
        data class Failure(val message: UIMessage): LinkAccept()
    }

    sealed class LinkDeny: GeneralResult() {
        class Success: LinkDeny()
        data class Failure(val message: UIMessage): LinkDeny()
    }

    sealed class DataFileCreation: GeneralResult() {
        data class Success(val key: ByteArray, val filePath: String): DataFileCreation()
        data class Failure(val message: UIMessage): DataFileCreation()
    }

    sealed class PostUserData: GeneralResult() {
        class Success: PostUserData()
        data class Failure(val message: UIMessage): PostUserData()
    }
}