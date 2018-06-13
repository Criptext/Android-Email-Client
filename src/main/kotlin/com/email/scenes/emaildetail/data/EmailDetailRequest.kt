package com.email.scenes.emaildetail.data

import com.email.db.MailFolders
import com.email.db.models.Label
import com.email.scenes.label_chooser.SelectedLabels

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
            val chosenLabel: MailFolders?,
            val threadId: String,
            val currentLabel: Label
    ): EmailDetailRequest()

    data class MoveEmail(
            val chosenLabel: MailFolders?,
            val emailId: Long,
            val currentLabel: Label
    ): EmailDetailRequest()
}
