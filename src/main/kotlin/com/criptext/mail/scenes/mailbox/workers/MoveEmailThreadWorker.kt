package com.criptext.mail.scenes.mailbox.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.mailbox.data.MailboxResult
import com.criptext.mail.utils.ContactUtils
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.batch
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.peerdata.PeerChangeThreadLabelData
import com.criptext.mail.utils.peerdata.PeerDeleteThreadData
import com.github.kittinunf.result.Result

/**
 * Created by sebas on 04/05/18.
 */

class MoveEmailThreadWorker(
        private val db: MailboxLocalDB,
        private val pendingDao: PendingEventDao,
        private val chosenLabel: String?,
        private val selectedThreadIds: List<String>,
        private val currentLabel: Label,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        accountDao: AccountDao,
        override val publishFn: (
                MailboxResult.MoveEmailThread) -> Unit)
    : BackgroundWorker<MailboxResult.MoveEmailThread> {

    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao,
            storage, accountDao)

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    private val defaultItems = Label.DefaultItems()
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.MoveEmailThread =
            if(ex is ServerErrorException) {
                when {
                    ex.errorCode == ServerCodes.Unauthorized ->
                        MailboxResult.MoveEmailThread.Unauthorized(UIMessage(R.string.device_removed_remotely_exception))
                    ex.errorCode == ServerCodes.Forbidden ->
                        MailboxResult.MoveEmailThread.Forbidden()
                    else -> MailboxResult.MoveEmailThread.Failure(
                            message = createErrorMessage(ex),
                            exception = ex)
                }
            }
            else MailboxResult.MoveEmailThread.Failure(
                    message = createErrorMessage(ex),
                    exception = ex)


    private fun getLabelEmailRelationsFromEmailIds(emailIds: List<Long>, label: String): List<EmailLabel> {
        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(label, activeAccount.id)))


        return emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    EmailLabel(emailId = emailId, labelId = labelId)
                }
            }
    }

    override fun work(reporter: ProgressReporter<MailboxResult.MoveEmailThread>)
            : MailboxResult.MoveEmailThread? {

        if(chosenLabel == null){
            //It means the threads will be deleted permanently
            val result = Result.of { db.deleteThreads(threadIds = selectedThreadIds, activeAccount = activeAccount) }
            return when (result) {
                is Result.Success -> {
                    selectedThreadIds.asSequence().batch(PeerEventsApiHandler.BATCH_SIZE).forEach { batch ->
                        peerEventHandler.enqueueEvent(PeerDeleteThreadData(batch).toJSON())
                    }
                    MailboxResult.MoveEmailThread.Success(selectedThreadIds, chosenLabel)
                }
                is Result.Failure -> {
                    catchException(result.error)
                }
            }
        }

        val rejectedLabels = defaultItems.rejectedLabelsByMailbox(currentLabel).map { it.id }
        val emailIds = selectedThreadIds.flatMap { threadId ->
            db.getEmailsByThreadId(threadId, rejectedLabels, activeAccount.id).map { it.id }
        }

        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(chosenLabel, activeAccount.id)))

        val peerSelectedLabels = selectedLabels.toList()
                .filter { it.text != currentLabel.text }
                .toList().map { it.text }
        val peerRemovedLabels = if(currentLabel == defaultItems.trash
                || currentLabel == defaultItems.spam)
            listOf(currentLabel.text)
        else
            emptyList()

        val result = Result.of {
                    if(currentLabel == defaultItems.trash && chosenLabel == Label.LABEL_SPAM){
                        //Mark as spam from trash
                        db.deleteRelationByLabelAndEmailIds(labelId = defaultItems.trash.id,
                                emailIds = emailIds)
                    }
                    val labelEmails = getLabelEmailRelationsFromEmailIds(emailIds, chosenLabel)
                    db.createLabelEmailRelations(labelEmails)
                    if(chosenLabel == Label.LABEL_SPAM){
                        val fromContacts = db.updateSpamCounter(emailIds, activeAccount.id, activeAccount.userEmail)
                        if(fromContacts.isNotEmpty()) {
                            apiClient.postReportSpam(fromContacts,
                                    ContactUtils.ContactReportTypes.spam,
                                    null)
                        }
                        db.updateIsTrusted(fromContacts, false)
                    }
                    if(chosenLabel == Label.LABEL_TRASH){
                        db.setTrashDate(emailIds, activeAccount.id)
                    }
                }

        return when (result) {
            is Result.Success -> {
                selectedThreadIds.asSequence().batch(PeerEventsApiHandler.BATCH_SIZE).forEach { batch ->
                    peerEventHandler.enqueueEvent(
                            PeerChangeThreadLabelData(batch, peerRemovedLabels,
                                    peerSelectedLabels).toJSON())
                }
                MailboxResult.MoveEmailThread.Success(selectedThreadIds, chosenLabel)
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
            else -> UIMessage(resId = R.string.local_error, args = arrayOf(ex.toString()))
        }
    }
}
