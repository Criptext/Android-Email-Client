package com.criptext.mail.websocket.data

import com.criptext.mail.SecureEmail
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.PeerEmailLabelsChangedStatusUpdate
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.EmailLabelDao
import com.criptext.mail.db.dao.LabelDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper

class UpdatePeerEmailLabelsChangedStatusWorker(private val eventId: Long,
                                               private val dao: EmailDao,
                                               private val emailLabelDao: EmailLabelDao,
                                               private val labelDao: LabelDao,
                                               httpClient: HttpClient,
                                               activeAccount: ActiveAccount,
                                               override val publishFn: (EventResult.UpdatePeerEmailChangedLabelsStatus) -> Unit,
                                               private val peerEmailLabelsChangedStatusUpdate: PeerEmailLabelsChangedStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerEmailChangedLabelsStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): EventResult.UpdatePeerEmailChangedLabelsStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerEmailChangedLabelsStatus>)
            : EventResult.UpdatePeerEmailChangedLabelsStatus? {

        if(!peerEmailLabelsChangedStatusUpdate.metadataKeys.isEmpty()){

            val emailIds = dao.getAllEmailsByMetadataKey(peerEmailLabelsChangedStatusUpdate.metadataKeys).map { it.id }
            val removedLabelIds = labelDao.get(peerEmailLabelsChangedStatusUpdate.labelsRemoved).map { it.id }
            val addedLabelIds = labelDao.get(peerEmailLabelsChangedStatusUpdate.labelsAdded)

            emailLabelDao.deleteRelationByLabelsAndEmailIds(removedLabelIds, emailIds)


            val labelEmails = getLabelEmailRelationsFromEmailIds(emailIds, addedLabelIds)
            emailLabelDao.insertAll(labelEmails)


            val update = EmailChangedLabelsPeerStatusUpdate(peerEmailLabelsChangedStatusUpdate)
            apiClient.acknowledgeEvents(eventId)
            return EventResult.UpdatePeerEmailChangedLabelsStatus.Success(update)
        }
        return EventResult.UpdatePeerEmailChangedLabelsStatus.Success(null)
    }

    private fun getLabelEmailRelationsFromEmailIds(emailIds: List<Long>, labels: List<Label>): List<EmailLabel> {
        val selectedLabels = SelectedLabels()
        val labelsWrapper = labels.map { LabelWrapper(it) }
        selectedLabels.addMultipleSelected(labelsWrapper)


        return emailIds.flatMap{ emailId ->
            selectedLabels.toIDs().map{ labelId ->
                EmailLabel(emailId = emailId, labelId = labelId)
            }
        }
    }

    override fun cancel() {
        TODO("not implemented")
    }
}