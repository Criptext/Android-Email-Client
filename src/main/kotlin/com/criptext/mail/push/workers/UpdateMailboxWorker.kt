package com.criptext.mail.push.workers

import android.content.res.Resources
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.whispersystems.libsignal.DuplicateMessageException
import com.squareup.picasso.Picasso
import android.graphics.Bitmap
import com.criptext.mail.api.Hosts
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.utils.*


/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        signalClient: SignalClient,
        private val dbEvents: EventLocalDB,
        private val activeAccount: ActiveAccount,
        storage: KeyValueStorage,
        private val loadedThreadsCount: Int?,
        private val label: Label,
        private val pushData: Map<String, String>,
        private val shouldPostNotification: Boolean,
        httpClient: HttpClient,
        override val publishFn: (
                PushResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<PushResult.UpdateMailbox> {


    override val canBeParallelized = false

    private val eventHelper = EventHelper(dbEvents, httpClient, storage, activeAccount, signalClient, false)
    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)
    private var shouldCallAgain = false

    override fun catchException(ex: Exception): PushResult.UpdateMailbox {
        val message = createErrorMessage(ex)
        return PushResult.UpdateMailbox.Failure(label, message, ex, pushData, shouldPostNotification)
    }

    private fun processFailure(failure: Result.Failure<EventHelperResultData,
            Exception>): PushResult.UpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            PushResult.UpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    mailboxThreads = null,
                    shouldPostNotification = shouldPostNotification,
                    pushData = pushData,
                    senderImage = null)
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
        val requestEvents = EventLoader.getEvents(apiClient)
        shouldCallAgain = (requestEvents as? Result.Success)?.value?.second ?: false
        val operationResult = requestEvents
                .flatMap(eventHelper.processEvents)

        val newData = mutableMapOf<String, String>()
        newData.putAll(pushData)


        return when(operationResult) {
            is Result.Success -> {
                if(shouldCallAgain) {
                    callAgainResult(operationResult.value.emailPreviews, newData, null)
                }else{
                    PushResult.UpdateMailbox.Success(
                            mailboxLabel = label,
                            isManual = true,
                            mailboxThreads = operationResult.value.emailPreviews,
                            pushData = newData,
                            shouldPostNotification = shouldPostNotification,
                            senderImage = null
                    )
                }
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    private fun callAgainResult(emailPreviews: List<EmailPreview>,
                                newData: Map<String, String>, bm: Bitmap?): PushResult.UpdateMailbox? {
        return PushResult.UpdateMailbox.SuccessAndRepeat(
                mailboxLabel = label,
                isManual = true,
                mailboxThreads = emailPreviews,
                pushData = newData,
                shouldPostNotification = shouldPostNotification,
                senderImage = bm
        )
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
