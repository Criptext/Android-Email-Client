package com.criptext.mail.scenes.emaildetail.data

import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.FileKey
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.UIMessage
import java.io.File
import java.util.*

/**
 * Created by sebas on 3/12/18.
 */

sealed class EmailDetailResult {

    sealed class GetSelectedLabels : EmailDetailResult() {
        class Success(val allLabels: List<Label>,
                      val selectedLabels: List<Label>): GetSelectedLabels()
        class Failure : GetSelectedLabels()
    }

    sealed class LoadFullEmailsFromThreadId: EmailDetailResult() {
        data class Success(val fullEmailList: List<FullEmail>, val unreadEmails: Int,
                           val changeAccountMessage: UIMessage?): LoadFullEmailsFromThreadId()
        class Failure: LoadFullEmailsFromThreadId()
    }

    sealed class UnsendFullEmailFromEmailId: EmailDetailResult() {
        data class Success(val position: Int, val unsentDate: Date): UnsendFullEmailFromEmailId()
        data class Failure(
                val position: Int,
                val message: UIMessage,
                val exception: Exception): UnsendFullEmailFromEmailId()
        data class Unauthorized(val message: UIMessage): UnsendFullEmailFromEmailId()
        class Forbidden: UnsendFullEmailFromEmailId()
    }

    sealed class UpdateUnreadStatus: EmailDetailResult(){
        data class Success(val threadId: String, val unread: Boolean): UpdateUnreadStatus()
        data class Failure(val message: UIMessage): UpdateUnreadStatus()
        data class Unauthorized(val message: UIMessage): UpdateUnreadStatus()
        class Forbidden: UpdateUnreadStatus()
    }

    sealed class ReadEmails: EmailDetailResult(){
        data class Success(val readEmailsQuantity: Int): ReadEmails()
        data class Failure(val message: UIMessage): ReadEmails()
    }

    sealed class MarkAsReadEmail: EmailDetailResult(){
        data class Success(val metadataKeys: List<Long>, val threadId: String, val unread: Boolean): MarkAsReadEmail()
        data class Failure(val message: UIMessage): MarkAsReadEmail()
    }

    sealed class UpdateEmailThreadsLabelsRelations: EmailDetailResult() {
        data class Success(val threadId: String, val selectedLabels: List<Label>,
                           val exitAndReload: Boolean): UpdateEmailThreadsLabelsRelations()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : UpdateEmailThreadsLabelsRelations()
    }

    sealed class MoveEmailThread: EmailDetailResult() {
        data class Success(val threadId: String?): MoveEmailThread()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : MoveEmailThread()
        data class Unauthorized(val message: UIMessage) : MoveEmailThread()
        class Forbidden : MoveEmailThread()
    }

    sealed class MoveEmail: EmailDetailResult() {
        data class Success(val emailId: Long): MoveEmail()
        data class Failure(
                val message: UIMessage,
                val exception: Exception) : MoveEmail()
        data class Unauthorized(val message: UIMessage) : MoveEmail()
        class Forbidden : MoveEmail()
    }

    sealed class DownloadFile : EmailDetailResult() {
        data class Success(val emailId: Long, val filetoken: String, val filepath: String, val cid: String?): DownloadFile()
        data class Failure(val emailId: Long, val fileToken: String, val message: UIMessage): DownloadFile()
        data class Unauthorized(val message: UIMessage): DownloadFile()
        class Forbidden: DownloadFile()
        class EnterpriseSuspended: DownloadFile()
        data class Progress(val emailId: Long, val filetoken: String, val progress: Int) : DownloadFile()
    }

    sealed class CopyToDownloads : EmailDetailResult() {
        data class Success(val file: File, val message: UIMessage): CopyToDownloads()
        data class Failure(val message: UIMessage): CopyToDownloads()
    }

    sealed class DeleteDraft : EmailDetailResult() {
        data class Success(val id: Long): DeleteDraft()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): DeleteDraft()
    }

    sealed class UpdateContactIsTrusted : EmailDetailResult() {
        data class Success(val fromContact: Contact, val metadataKey: Long, val newIsTrusted: Boolean): UpdateContactIsTrusted()
        data class Failure(
                val message: UIMessage,
                val exception: Exception): UpdateContactIsTrusted()
    }
}
