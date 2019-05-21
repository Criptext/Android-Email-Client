package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerChangeEmailLabelData
import com.github.kittinunf.result.Result

/**
 * Created by danieltigse on 05/01/18.
 */

class UpdateEmailThreadLabelsWorker(
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        accountDao: AccountDao,
        private val exitAndReload: Boolean,
        private val db: EmailDetailLocalDB,
        private val pendingDao: PendingEventDao,
        private val currentLabel: Label,
        private val selectedLabels: SelectedLabels,
        private val threadId: String,
        private val removeCurrentLabel: Boolean,
        override val publishFn: (
                EmailDetailResult.UpdateEmailThreadsLabelsRelations) -> Unit)
    : BackgroundWorker<EmailDetailResult.UpdateEmailThreadsLabelsRelations> {

    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient,
            activeAccount, pendingDao, storage, accountDao)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.UpdateEmailThreadsLabelsRelations {

        val message = createErrorMessage(ex)
        return EmailDetailResult.UpdateEmailThreadsLabelsRelations.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.UpdateEmailThreadsLabelsRelations>)
            : EmailDetailResult.UpdateEmailThreadsLabelsRelations? {

        val selectedLabelsList = db.getLabelsById(selectedLabels.toList().map { it.label.id }, activeAccount.id)
        val trueCurrentLabel = db.getLabelById(currentLabel.id, activeAccount.id) ?: Label.defaultItems.all_mail
        val rejectedLabels = Label.defaultItems.rejectedLabelsByMailbox(trueCurrentLabel).map { it.id }
        val systemLabels = db.getLabelsByName(Label.defaultItems.toList().map { it.text }, activeAccount.id)
                .filter { !rejectedLabels.contains(it.id) }
                .filter { it.text != Label.LABEL_STARRED }
        val emails = db.getFullEmailsFromThreadId(threadId = threadId,
                rejectedLabels = rejectedLabels, account = activeAccount)
        val emailIds = emails.map { it.email.id }
        val removedLabels = if(trueCurrentLabel == Label.defaultItems.starred
                || trueCurrentLabel == Label.defaultItems.sent) listOf(Label.defaultItems.inbox.text)
        else
            listOf(trueCurrentLabel.text)

        val peerSelectedLabels = selectedLabels.toList()
                .filter { it.text != trueCurrentLabel.text }
                .toList().map { it.text }
        val peerRemovedLabels = db.getLabelsFromThreadId(threadId)
                .filter { !selectedLabelsList.contains(it) }
                .filter { (!systemLabels.contains(it)) }
                .map { it.text }
                .toMutableList()



        val result =
            if(removeCurrentLabel){
                if(trueCurrentLabel == Label.defaultItems.spam){
                    peerRemovedLabels.removeAll(peerRemovedLabels)
                    peerRemovedLabels.add(Label.LABEL_SPAM)
                }else
                    peerRemovedLabels.add(trueCurrentLabel.text)
                    Result.of {
                        if(trueCurrentLabel == Label.defaultItems.starred
                                || trueCurrentLabel == Label.defaultItems.sent){
                            db.deleteRelationByLabelAndEmailIds(Label.defaultItems.inbox.id, emailIds, activeAccount.id)
                        }
                        else{
                            db.deleteRelationByLabelAndEmailIds(trueCurrentLabel.id, emailIds, activeAccount.id)
                    } }

            } else {
                Result.of {
                    db.deleteRelationByEmailIds(emailIds = emailIds)

                    val emailLabels = arrayListOf<EmailLabel>()
                    emailIds.flatMap { emailId ->
                        selectedLabels.toIDs().map { labelId ->
                            emailLabels.add(EmailLabel(
                                    emailId = emailId,
                                    labelId = labelId))
                        }
                    }
                    db.createLabelEmailRelations(emailLabels)
                }
            }

        return when(result){
            is Result.Success -> {
                peerEventHandler.enqueueEvent(
                        PeerChangeEmailLabelData(emails.map { it.email.metadataKey },
                        peerRemovedLabels,
                        peerSelectedLabels).toJSON())
                val labels = db.getLabelsFromThreadId(threadId)
                EmailDetailResult.UpdateEmailThreadsLabelsRelations.Success(threadId, labels, exitAndReload)
            }
            is Result.Failure -> {
                catchException(result.error)
            }
        }

    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { _ ->
        UIMessage(resId = R.string.failed_getting_emails)
    }
}
