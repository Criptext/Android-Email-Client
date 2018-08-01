package com.email.websocket.data

import com.email.api.HttpClient
import com.email.api.models.PeerThreadLabelsChangedStatusUpdate
import com.email.bgworker.BackgroundWorker
import com.email.bgworker.ProgressReporter
import com.email.db.dao.EmailDao
import com.email.db.dao.EmailLabelDao
import com.email.db.dao.LabelDao
import com.email.db.models.ActiveAccount
import com.email.db.models.EmailLabel
import com.email.db.models.Label
import com.email.scenes.label_chooser.SelectedLabels
import com.email.scenes.label_chooser.data.LabelWrapper

class UpdatePeerThreadLabelsChangedStatusWorker(private val eventId: Long,
                                                private val dao: EmailDao,
                                                private val labelDao: LabelDao,
                                                private val emailLabelDao: EmailLabelDao,
                                                httpClient: HttpClient,
                                                activeAccount: ActiveAccount,
                                                override val publishFn: (EventResult.UpdatePeerThreadChangedLabelsStatus) -> Unit,
                                                private val peerThreadLabelsChangedStatusUpdate: PeerThreadLabelsChangedStatusUpdate
                       ) : BackgroundWorker<EventResult.UpdatePeerThreadChangedLabelsStatus> {

    override val canBeParallelized = false
    private val apiClient = WebSocketAPIClient(httpClient, activeAccount.jwt)


    override fun catchException(ex: Exception): EventResult.UpdatePeerThreadChangedLabelsStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun work(reporter: ProgressReporter<EventResult.UpdatePeerThreadChangedLabelsStatus>)
            : EventResult.UpdatePeerThreadChangedLabelsStatus? {

        if(!peerThreadLabelsChangedStatusUpdate.threadIds.isEmpty()){

            val emailIds = dao.getEmailsFromThreadIds(peerThreadLabelsChangedStatusUpdate.threadIds).map { it.id }
            val removedLabelIds = labelDao.get(peerThreadLabelsChangedStatusUpdate.labelsRemoved).map { it.id }
            val addedLabelIds = labelDao.get(peerThreadLabelsChangedStatusUpdate.labelsAdded)

            emailLabelDao.deleteRelationByLabelsAndEmailIds(removedLabelIds, emailIds)
            val labelEmails = getLabelEmailRelationsFromEmailIds(emailIds, addedLabelIds)
            emailLabelDao.insertAll(labelEmails)


            val update = ThreadChangedLabelsPeerStatusUpdate(peerThreadLabelsChangedStatusUpdate)
            apiClient.acknowledgeEvents(eventId)
            return EventResult.UpdatePeerThreadChangedLabelsStatus.Success(update)
        }
        return EventResult.UpdatePeerThreadChangedLabelsStatus.Success(null)
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