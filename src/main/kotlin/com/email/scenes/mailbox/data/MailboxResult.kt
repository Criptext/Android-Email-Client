package com.email.scenes.mailbox.data

import com.email.db.LabelTextTypes
import com.email.db.models.Label
import com.email.utils.UIMessage
import org.json.JSONObject

/**
 * Created by sebas on 3/20/18.
 */

sealed class MailboxResult {

    sealed class UpdateEmailThreadsLabelsRelations: MailboxResult() {
        class Success: UpdateEmailThreadsLabelsRelations()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : UpdateEmailThreadsLabelsRelations()
    }

    sealed class GetLabels : MailboxResult() {
        class Success(val labels: List<Label>,
                      val defaultSelectedLabels: List<Label>): GetLabels()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : GetLabels()
    }

    sealed class LoadEmailThreads : MailboxResult() {
        abstract fun getDestinationMailbox(): LabelTextTypes
        class Success(
                val emailThreads: List<EmailThread>,
                val mailboxLabel: LabelTextTypes): LoadEmailThreads() {

            override fun getDestinationMailbox(): LabelTextTypes {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: LabelTextTypes,
                val message: UIMessage,
                val exception: Exception) : LoadEmailThreads() {

            override fun getDestinationMailbox(): LabelTextTypes {
                return mailboxLabel
            }
        }
    }

    sealed class UpdateMailbox : MailboxResult() {
        abstract fun getDestinationMailbox(): LabelTextTypes
        data class Success(
                val mailboxLabel: LabelTextTypes,
                val mailboxThreads: List<EmailThread>,
                val isManual: Boolean): UpdateMailbox() {

            override fun getDestinationMailbox(): LabelTextTypes {
                return mailboxLabel
            }
        }

        data class Failure(
                val mailboxLabel: LabelTextTypes,
                val message: UIMessage ): UpdateMailbox() {
            override fun getDestinationMailbox(): LabelTextTypes {
                return mailboxLabel
            }
        }
    }

    sealed class SendMail: MailboxResult() {
        class Success(val emailId: Int, val response: JSONObject): SendMail()
        data class Failure(val message: UIMessage): SendMail()
    }

    sealed class UpdateMail: MailboxResult() {
        class Success: UpdateMail()
        class Failure: UpdateMail()
    }

}

