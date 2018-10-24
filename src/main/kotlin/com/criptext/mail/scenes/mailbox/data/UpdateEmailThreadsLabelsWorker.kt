package com.criptext.mail.scenes.mailbox.data

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.utils.ServerErrorCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerChangeThreadLabelData
import com.github.kittinunf.result.Result

/**
 * Created by sebas on 04/05/18.
 */

class UpdateEmailThreadsLabelsWorker(
        private val db: MailboxLocalDB,
        private val pendingDao: PendingEventDao,
        private val selectedLabels: SelectedLabels,
        private val selectedThreadIds: List<String>,
        private val currentLabel: Label,
        private val shouldRemoveCurrentLabel: Boolean,
        httpClient: HttpClient,
        activeAccount: ActiveAccount,
        override val publishFn: (
                MailboxResult.UpdateEmailThreadsLabelsRelations) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateEmailThreadsLabelsRelations> {

    private val defaultItems = Label.DefaultItems()
    override val canBeParallelized = false

    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)
    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient, activeAccount.jwt, pendingDao)

    override fun catchException(ex: Exception): MailboxResult.UpdateEmailThreadsLabelsRelations =
            if(ex is ServerErrorException) {
                when {
                    ex.errorCode == ServerErrorCodes.Unauthorized ->
                        MailboxResult.UpdateEmailThreadsLabelsRelations.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                    ex.errorCode == ServerErrorCodes.Forbidden ->
                        MailboxResult.UpdateEmailThreadsLabelsRelations.Forbidden()
                    else -> MailboxResult.UpdateEmailThreadsLabelsRelations.Failure(
                            message = createErrorMessage(ex),
                            exception = ex)
                }
            }
            else MailboxResult.UpdateEmailThreadsLabelsRelations.Failure(
                    message = createErrorMessage(ex),
                    exception = ex)


    private fun removeCurrentLabelFromEmails(emailIds: List<Long>) {
        if(currentLabel == defaultItems.starred
          || currentLabel == defaultItems.sent)
            db.deleteRelationByLabelAndEmailIds(Label.defaultItems.inbox.id, emailIds)
        else
            db.deleteRelationByLabelAndEmailIds(currentLabel.id, emailIds)
    }

    private fun updateLabelEmailRelations(emailIds: List<Long>) {
        db.deleteRelationByEmailIds(emailIds = emailIds)

        val emailLabels =
                emailIds.flatMap { emailId ->
                    selectedLabels.toIDs().map { labelId ->
                        EmailLabel(emailId = emailId, labelId = labelId)
                    }
                }
        db.createLabelEmailRelations(emailLabels)
    }

    override fun work(reporter: ProgressReporter<MailboxResult.UpdateEmailThreadsLabelsRelations>)
            : MailboxResult.UpdateEmailThreadsLabelsRelations? {

        val selectedLabelsList = selectedLabels.toList().map { it.label }
        val rejectedLabels = defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val systemLabels = db.getLabelsByName(Label.defaultItems.toList().map { it.text })
                .filter { !rejectedLabels.contains(it.id) }
                .filter { it.text != Label.LABEL_STARRED }

        val peerSelectedLabels = selectedLabels.toList()
                .filter { it.text != currentLabel.text }
                .toList().map { it.text }
        val peerRemovedLabels = db.getLabelsFromThreadIds(selectedThreadIds)
                .filter { !selectedLabelsList.contains(it) }
                .filter { (!systemLabels.contains(it)) }
                .map { it.text }
                .toMutableList()


        val emailIds = selectedThreadIds.flatMap { threadId ->
            db.getEmailsByThreadId(threadId, rejectedLabels).map { it.id }
        }

        val result =
            if(shouldRemoveCurrentLabel) {
                if(currentLabel == Label.defaultItems.spam){
                    peerRemovedLabels.removeAll(peerRemovedLabels)
                    peerRemovedLabels.add(Label.LABEL_SPAM)
                }else
                    peerRemovedLabels.add(currentLabel.text)
                Result.of { removeCurrentLabelFromEmails(emailIds)}
            }else {
                Result.of{ updateLabelEmailRelations(emailIds) }
            }

        return when(result){
            is Result.Success -> {
                peerEventHandler.enqueueEvent(
                        PeerChangeThreadLabelData(selectedThreadIds, peerRemovedLabels,
                        peerSelectedLabels).toJSON())
                MailboxResult.UpdateEmailThreadsLabelsRelations.Success()
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }
    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when (ex) {
            is ServerErrorException -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> UIMessage(resId = R.string.failed_getting_emails)
        }
    }
}
