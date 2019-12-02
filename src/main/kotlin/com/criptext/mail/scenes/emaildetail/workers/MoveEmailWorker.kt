package com.criptext.mail.scenes.emaildetail.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.emaildetail.data.EmailDetailAPIClient
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.ContactUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.batch
import com.criptext.mail.utils.generaldatasource.data.GeneralAPIClient
import com.criptext.mail.utils.peerdata.PeerChangeEmailLabelData
import com.criptext.mail.utils.peerdata.PeerDeleteEmailData
import com.github.kittinunf.result.Result

/**
 * Created by danieltigse on 5/6/18.
 */

class MoveEmailWorker(
        private val db: EmailDetailLocalDB,
        private val emailDao: EmailDao,
        private val accountDao: AccountDao,
        private val storage: KeyValueStorage,
        private val pendingDao: PendingEventDao,
        private val chosenLabel: String?,
        private val emailId: Long,
        private val currentLabel: Label,
        httpClient: HttpClient,
        private val activeAccount: ActiveAccount,
        private val isPhishing: Boolean,
        override val publishFn: (
                EmailDetailResult.MoveEmail) -> Unit)
    : BackgroundWorker<EmailDetailResult.MoveEmail> {

    private val peerEventHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao,
            storage, accountDao)

    private val apiClient = GeneralAPIClient(httpClient, activeAccount.jwt)

    override val canBeParallelized = false

    override fun catchException(ex: Exception): EmailDetailResult.MoveEmail {

        val message = createErrorMessage(ex)
        return EmailDetailResult.MoveEmail.Failure(
                message = message,
                exception = ex)
    }

    override fun work(reporter: ProgressReporter<EmailDetailResult.MoveEmail>): EmailDetailResult.MoveEmail? {

        val emailIds = listOf(emailId)
        val metadataKeys = emailDao.getAllEmailsbyId(emailIds, activeAccount.id)
                .filter { it.delivered !in listOf(DeliveryTypes.FAIL, DeliveryTypes.SENDING) }
                .map { it.metadataKey }

        if(chosenLabel == null){
            //It means the email will be deleted permanently
            val result = Result.of { db.deleteEmail(emailId, activeAccount) }
            return when (result) {
                is Result.Success -> {
                    metadataKeys.asSequence().batch(PeerEventsApiHandler.BATCH_SIZE).forEach {
                        peerEventHandler.enqueueEvent(PeerDeleteEmailData(it).toJSON())
                    }
                    EmailDetailResult.MoveEmail.Success(emailId)
                }
                is Result.Failure -> {
                    val message = createErrorMessage(result.error)
                    EmailDetailResult.MoveEmail.Failure(message = message, exception = result.error)
                }
            }
        }

        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(chosenLabel, activeAccount.id)))
        val peerRemoveLabels = if(currentLabel == Label.defaultItems.trash
                || currentLabel == Label.defaultItems.spam)
            listOf(currentLabel.text)
        else
            emptyList()

        val result =  Result.of{
            if(currentLabel == Label.defaultItems.trash && chosenLabel == Label.LABEL_SPAM){
                //Mark as spam from trash
                db.deleteRelationByLabelAndEmailIds(labelId = Label.defaultItems.trash.id,
                        emailIds = emailIds, accountId = activeAccount.id)
            }

            val emailLabels = arrayListOf<EmailLabel>()
            emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    emailLabels.add(EmailLabel(
                            emailId = emailId,
                            labelId = labelId))
                }
            }
            db.createLabelEmailRelations(emailLabels)

            if(chosenLabel == Label.LABEL_SPAM){
                val fromContacts = db.updateSpamCounter(emailIds, activeAccount.id, activeAccount.userEmail)
                if(fromContacts.isNotEmpty()) {
                    val lastValidEmail = db.getFullEmailFromId(emailId, activeAccount)
                    if (isPhishing)
                        apiClient.postReportSpam(fromContacts,
                                ContactUtils.ContactReportTypes.phishing,
                                lastValidEmail?.headers ?: lastValidEmail?.email?.content)
                    else
                        apiClient.postReportSpam(fromContacts,
                                ContactUtils.ContactReportTypes.spam,
                                null)
                }
            }

            if(chosenLabel == Label.LABEL_TRASH){
                db.setTrashDate(emailIds, activeAccount.id)
            }}

        return when (result) {
            is Result.Success -> {
                metadataKeys.asSequence().batch(PeerEventsApiHandler.BATCH_SIZE).forEach { batch ->
                    peerEventHandler.enqueueEvent(PeerChangeEmailLabelData(batch,
                            peerRemoveLabels, selectedLabels.toList().map { it.text }).toJSON())
                }
                EmailDetailResult.MoveEmail.Success(emailId)
            }
            is Result.Failure -> {
                val message = createErrorMessage(result.error)
                EmailDetailResult.MoveEmail.Failure(message = message, exception = result.error)
            }
        }

    }

    override fun cancel() {
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex){
            is ServerErrorException -> {
                when {
                    ex.errorCode == 401 -> UIMessage(resId = R.string.device_removed_remotely_exception)
                    else -> UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
                }
            }
            else -> UIMessage(resId = R.string.unable_to_move_email, args = arrayOf(ex.toString()))
        }
    }
}
