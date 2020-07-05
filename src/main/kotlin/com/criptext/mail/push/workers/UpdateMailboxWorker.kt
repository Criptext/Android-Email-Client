package com.criptext.mail.push.workers

import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.whispersystems.libsignal.DuplicateMessageException
import android.graphics.Bitmap
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.api.models.Event
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.utils.*
import com.criptext.mail.utils.eventhelper.EventHelper
import com.criptext.mail.utils.eventhelper.EventHelperListener
import com.criptext.mail.utils.eventhelper.EventHelperResultData
import com.criptext.mail.utils.eventhelper.EventLoader
import com.github.kittinunf.result.mapError
import org.json.JSONObject


/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val signalClient: SignalClient,
        private val dbEvents: EventLocalDB,
        private val activeAccount: ActiveAccount,
        private val storage: KeyValueStorage,
        private val label: Label,
        private val httpClient: HttpClient,
        override val publishFn: (
                PushResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<PushResult.UpdateMailbox> {


    override val canBeParallelized = false

    private lateinit var eventHelper: EventHelper
    private var parsedEmailCount = 0
    private var maxEmailCount = 0
    private val apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)
    private var shouldCallAgain = false

    override fun catchException(ex: Exception): PushResult.UpdateMailbox {
        val message = createErrorMessage(ex)
        return PushResult.UpdateMailbox.Failure(label, message, ex)
    }

    private fun processFailure(failure: Result.Failure<EventHelperResultData,
            Exception>): PushResult.UpdateMailbox {
        return if (failure.error is EventHelper.NothingNewException)
            PushResult.UpdateMailbox.Success(
                    mailboxLabel = label,
                    isManual = true,
                    senderImage = null)
        else
            PushResult.UpdateMailbox.Failure(
                    mailboxLabel = label,
                    message = createErrorMessage(failure.error),
                    exception = failure.error)
    }

    private fun setup(reporter: ProgressReporter<PushResult.UpdateMailbox>): Result<Pair<List<Event>, Boolean>, Exception>{
        val count = Result.of {
            apiClient.getPendingEventCount(Event.Cmd.newEmail)
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
        when(count){
            is Result.Success -> {
                maxEmailCount = JSONObject(count.value.body).getInt("total")
                eventHelper = EventHelper(dbEvents, httpClient, storage, activeAccount, signalClient,
                        true, false, object : EventHelperListener {
                    override fun emailHasBeenParsed() {
                        parsedEmailCount += 1
                        reporter.report(PushResult.UpdateMailbox.Progress(parsedEmailCount, maxEmailCount))
                    }
                })
                eventHelper.setupForMailbox(label)
                val requestEvents = EventLoader.getEvents(apiClient)
                shouldCallAgain = (requestEvents as? Result.Success)?.value?.second ?: false
                return requestEvents
            }
            is Result.Failure -> return Result.of { throw EventHelper.NoContentFoundException() }
        }
    }

    override fun work(reporter: ProgressReporter<PushResult.UpdateMailbox>)
            : PushResult.UpdateMailbox? {

        val operationResult = setup(reporter)
                .flatMap(eventHelper.processEvents)


        return when(operationResult) {
            is Result.Success -> {
                if(shouldCallAgain) {
                    callAgainResult(null)
                }else{
                    PushResult.UpdateMailbox.Success(
                            mailboxLabel = label,
                            isManual = true,
                            senderImage = null
                    )
                }
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    private fun callAgainResult(bm: Bitmap?): PushResult.UpdateMailbox? {
        return PushResult.UpdateMailbox.SuccessAndRepeat(
                mailboxLabel = label,
                isManual = true,
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
            is ServerErrorException ->
                UIMessage(resId = R.string.server_bad_status, args = arrayOf(ex.errorCode))
            else -> {
                UIMessage(resId = R.string.failed_getting_emails)
            }
        }
    }
}
