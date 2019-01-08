package com.criptext.mail.push.data

import android.content.res.Resources
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.models.DeviceInfo
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.scenes.mailbox.data.UpdateBannerData
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.utils.EventHelper
import com.criptext.mail.utils.EventLoader
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.whispersystems.libsignal.DuplicateMessageException

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        signalClient: SignalClient,
        private val dbEvents: EventLocalDB,
        activeAccount: ActiveAccount,
        private val loadedThreadsCount: Int?,
        private val label: Label,
        private val pushData: Map<String, String>,
        private val shouldPostNotification: Boolean,
        httpClient: HttpClient,
        override val publishFn: (
                PushResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<PushResult.UpdateMailbox> {


    override val canBeParallelized = false

    private val eventHelper = EventHelper(dbEvents, httpClient, activeAccount, signalClient, false)
    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)

    override fun catchException(ex: Exception): PushResult.UpdateMailbox {
        val message = createErrorMessage(ex)
        return PushResult.UpdateMailbox.Failure(label, message, ex, pushData, shouldPostNotification)
    }

    private fun processFailure(failure: Result.Failure<Triple<List<EmailPreview>, UpdateBannerData?, List<DeviceInfo?>>,
            Exception>): PushResult.UpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            PushResult.UpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    mailboxThreads = null,
                    shouldPostNotification = shouldPostNotification,
                    pushData = pushData)
        else
            PushResult.UpdateMailbox.Failure(
                    mailboxLabel = label,
                    message = createErrorMessage(failure.error),
                    exception = failure.error,
                    pushData = pushData,
                    shouldPostNotification = shouldPostNotification)
    }

    override fun work(reporter: ProgressReporter<PushResult.UpdateMailbox>)
            : PushResult.UpdateMailbox? {
        eventHelper.setupForMailbox(label, loadedThreadsCount)
        val operationResult = EventLoader.getEvents(apiClient)
                .flatMap(eventHelper.processEvents)

        val newData = mutableMapOf<String, String>()
        newData.putAll(pushData)


        return when(operationResult) {
            is Result.Success -> {
                val metadataKey = newData["metadataKey"]?.toLong()
                if(metadataKey != null) {
                    val email = dbEvents.getEmailByMetadataKey(metadataKey)
                    if(email != null){
                        newData["preview"] = email.preview
                        newData["body"] = email.subject
                        newData["title"] = dbEvents.getFromContactByEmailId(email.id)[0].name

                        PushResult.UpdateMailbox.Success(
                                mailboxLabel = label,
                                isManual = true,
                                mailboxThreads = operationResult.value.first,
                                pushData = newData,
                                shouldPostNotification = shouldPostNotification
                        )
                    }else{
                        PushResult.UpdateMailbox.Failure(
                                mailboxLabel = label,
                                message = createErrorMessage(Resources.NotFoundException()),
                                exception = Resources.NotFoundException(),
                                pushData = pushData,
                                shouldPostNotification = shouldPostNotification)
                    }
                }else {
                    PushResult.UpdateMailbox.Failure(
                            mailboxLabel = label,
                            message = createErrorMessage(Resources.NotFoundException()),
                            exception = Resources.NotFoundException(),
                            pushData = pushData,
                            shouldPostNotification = shouldPostNotification)
                }
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private val createErrorMessage: (ex: Exception) -> UIMessage = { ex ->
        when(ex) {
            is DuplicateMessageException ->
                UIMessage(resId = R.string.email_already_decrypted)
            else -> {
                UIMessage(resId = R.string.failed_getting_emails)
            }
        }
    }
}
