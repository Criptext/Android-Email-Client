package com.criptext.mail.scenes.emaildetail.data

import com.criptext.mail.db.models.FileKey
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.label_chooser.SelectedLabels

/**
 * Created by sebas on 3/12/18.
 */

sealed class EmailDetailRequest{

    data class GetSelectedLabels(val threadId: String): EmailDetailRequest()

    class LoadFullEmailsFromThreadId(
            val threadId: String,
            val currentLabel: Label): EmailDetailRequest()

    class UnsendFullEmailFromEmailId(
            val emailId: Long, val position: Int): EmailDetailRequest()

    data class UpdateEmailThreadsLabelsRelations(
            val selectedLabels: SelectedLabels,
            val threadId: String,
            val currentLabel: Label,
            val removeCurrentLabel: Boolean
    ): EmailDetailRequest()

    data class UpdateUnreadStatus(
            val threadId: String,
            val updateUnreadStatus: Boolean,
            val currentLabel: Label
    ): EmailDetailRequest()

    data class MoveEmailThread(
            val chosenLabel: String?,
            val threadId: String,
            val currentLabel: Label
    ): EmailDetailRequest()

    data class MoveEmail(
            val chosenLabel: String?,
            val emailId: Long,
            val currentLabel: Label
    ): EmailDetailRequest()

    data class ReadEmails(
            val emailIds: List<Long>,
            val metadataKeys: List<Long>
    ): EmailDetailRequest()

    data class DownloadFile(
            val fileToken: String,
            val emailId: Long,
            val fileKey: String?
    ): EmailDetailRequest()
}
