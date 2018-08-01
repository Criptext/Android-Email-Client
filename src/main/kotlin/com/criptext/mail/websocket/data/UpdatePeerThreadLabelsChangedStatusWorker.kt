package com.criptext.mail.websocket.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.PeerThreadLabelsChangedStatusUpdate
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