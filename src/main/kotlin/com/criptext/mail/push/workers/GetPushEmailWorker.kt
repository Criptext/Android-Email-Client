package com.criptext.mail.push.workers

import android.content.res.Resources
import android.os.Build
import com.criptext.mail.R
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.api.models.Event
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.AntiPushMap
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.PushTypes
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.EventHelper
import com.criptext.mail.utils.EventLoader
import com.criptext.mail.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.squareup.picasso.Picasso
import org.whispersystems.libsignal.DuplicateMessageException
import java.io.IOException


class GetPushEmailWorker(
        private val db: AppDatabase,
        private val dbEvents: EventLocalDB,
        private val label: Label,
        private val pushData: Map<String, String>,
        private val shouldPostNotification: Boolean,
        private val httpClient: HttpClient,
        override val publishFn: (
                PushResult.NewEmail) -> Unit)
    : BackgroundWorker<PushResult.NewEmail> {


    override val canBeParallelized = false

    private lateinit var apiClient: MailboxAPIClient
    private lateinit var emailInsertionApiClient: EmailInsertionAPIClient
    private val eventsToAcknowldege = mutableListOf<Long>()
    private lateinit var activeAccount: ActiveAccount
    private lateinit var signalClient: SignalClient
    private var notificationId: Int = -1

    override fun catchException(ex: Exception): PushResult.NewEmail {
        val message = createErrorMessage(ex)
        return PushResult.NewEmail.Failure(label, message, ex, pushData, shouldPostNotification,
                notificationId)
    }

    private fun processFailure(failure: Result.Failure<Boolean,
            Exception>): PushResult.NewEmail {
        return if (failure.error is EventHelper.NothingNewException)
            PushResult.NewEmail.Success(
                    mailboxLabel = label,
                    isManual = true,
                    shouldPostNotification = shouldPostNotification,
                    pushData = pushData,
                    senderImage = null,
                    notificationId = notificationId)
        else
            PushResult.NewEmail.Failure(
                    mailboxLabel = label,
                    message = createErrorMessage(failure.error),
                    exception = failure.error,
                    pushData = pushData,
                    shouldPostNotification = shouldPostNotification,
                    notificationId = notificationId)
    }

    override fun work(reporter: ProgressReporter<PushResult.NewEmail>)
            : PushResult.NewEmail? {

        val dbAccount = dbEvents.getAccount(pushData["account"], pushData["domain"]) ?: return PushResult.NewEmail.Failure(
                mailboxLabel = label,
                message = createErrorMessage(EventHelper.NothingNewException()),
                exception = EventHelper.NothingNewException(),
                pushData = pushData,
                shouldPostNotification = shouldPostNotification,
                notificationId = -1)

        activeAccount = ActiveAccount.loadFromDB(dbAccount)!!
        setupForAntiPush(pushData)

        val rowId = pushData["rowId"]?.toInt()

        if(rowId == null || rowId == 0){
            return PushResult.NewEmail.Failure(
                    mailboxLabel = label,
                    message = createErrorMessage(EventHelper.NothingNewException()),
                    exception = EventHelper.NothingNewException(),
                    pushData = pushData,
                    shouldPostNotification = shouldPostNotification,
                    notificationId = notificationId)
        }



        signalClient = SignalClient.Default(SignalStoreCriptext(db, activeAccount))
        apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)
        emailInsertionApiClient = EmailInsertionAPIClient(httpClient, activeAccount.jwt)

        val requestEvents = EventLoader.getEvent(apiClient, rowId)
        val operationResult = requestEvents
                .flatMap(processEvent)

        val newData = mutableMapOf<String, String>()
        newData.putAll(pushData)


        return when(operationResult) {
            is Result.Success -> {
                val metadataKey = newData["metadataKey"]?.toLong()
                if(metadataKey != null) {
                    val email = dbEvents.getEmailByMetadataKey(metadataKey, activeAccount.id)
                    if(email != null){
                        val files = dbEvents.getFullEmailById(emailId = email.id, activeAccount = activeAccount)!!.files
                        newData["preview"] = email.preview
                        newData["subject"] = email.subject
                        newData["hasInlineImages"] = (files.firstOrNull { it.cid != null }  != null).toString()
                        newData["name"] = dbEvents.getFromContactByEmailId(email.id)[0].name
                        newData["email"] = dbEvents.getFromContactByEmailId(email.id)[0].email
                        val emailAddress = newData["email"]
                        val bm = try {
                            if(emailAddress != null && EmailAddressUtils.isFromCriptextDomain(emailAddress)) {
                                val domain = EmailAddressUtils.extractEmailAddressDomain(emailAddress)
                                Picasso.get().load(Hosts.restApiBaseUrl
                                        .plus("/user/avatar/$domain/${EmailAddressUtils.extractRecipientIdFromAddress(emailAddress, domain)}")).get()
                            } else
                                null
                        } catch (ex: Exception){
                            null
                        }
                        PushResult.NewEmail.Success(
                                mailboxLabel = label,
                                isManual = true,
                                pushData = newData,
                                shouldPostNotification = shouldPostNotification,
                                senderImage = bm,
                                notificationId = notificationId
                        )
                    }else{
                        PushResult.NewEmail.Failure(
                                mailboxLabel = label,
                                message = createErrorMessage(Resources.NotFoundException()),
                                exception = Resources.NotFoundException(),
                                pushData = pushData,
                                shouldPostNotification = shouldPostNotification,
                                notificationId = notificationId)
                    }
                }else {
                    PushResult.NewEmail.Failure(
                            mailboxLabel = label,
                            message = createErrorMessage(Resources.NotFoundException()),
                            exception = Resources.NotFoundException(),
                            pushData = pushData,
                            shouldPostNotification = shouldPostNotification,
                            notificationId = notificationId)
                }
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    val processEvent: (Event) -> Result<Boolean, Exception> = { event ->
        Result.of {
            val shouldReload = processNewEmails(event)
            shouldReload.or(acknowledgeEventsIgnoringErrors(eventsToAcknowldege))
        }
    }

    private fun processNewEmails(event: Event): Boolean {
        val toIdAndMetadataPair: (Event) -> Pair<Long, EmailMetadata> =
                { Pair( it.rowid, EmailMetadata.fromJSON(it.params)) }
        val emailInsertedSuccessfully: (Pair<Long, EmailMetadata>) -> Boolean =
                { (_, metadata) ->
                    try {
                        insertIncomingEmailTransaction(metadata)
                        // insertion success, try to acknowledge it
                        true
                    } catch (ex: DuplicateMessageException) {
                        // duplicated, try to acknowledge it
                        true
                    }
                    catch (ex: Exception) {
                        // Unknown exception, probably network related, skip acknowledge
                        if(ex is DuplicateMessageException){
                            updateExistingEmailTransaction(metadata)
                        }
                        ex is DuplicateMessageException
                    }
                }
        val toEventId: (Pair<Long, EmailMetadata>) -> Long =
                { (eventId, _) -> eventId }

        val eventIdsToAcknowledge = listOf(event)
                .map(toIdAndMetadataPair)
                .filter(emailInsertedSuccessfully)
                .map(toEventId)

        if (eventIdsToAcknowledge.isNotEmpty())
            eventsToAcknowldege.addAll(eventIdsToAcknowledge)

        return eventIdsToAcknowledge.isNotEmpty()
    }

    override fun cancel() {
        TODO("CANCEL IS NOT IMPLEMENTED")
    }

    private fun setupForAntiPush(data: Map<String, String>){
        val action = data["action"] ?: return
        when(action){
            PushTypes.newMail.actionCode() -> {
                val metadataKey = data["metadataKey"]
                if(!metadataKey.isNullOrEmpty())
                    notificationId = db.antiPushMapDao().insert(AntiPushMap(0, metadataKey!!, activeAccount.id)).toInt()
            }
            PushTypes.linkDevice.actionCode(),
            PushTypes.syncDevice.actionCode() -> {
                val randomId = data["randomId"]
                if(!randomId.isNullOrEmpty())
                    notificationId = db.antiPushMapDao().insert(AntiPushMap(0, randomId!!, activeAccount.id)).toInt()
        }
            else -> {
                val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                val type = PushTypes.fromActionString(action)
                notificationId =  if(isPostNougat) type.requestCodeRandom() else type.requestCode()
            }
        }
    }

    private fun insertIncomingEmailTransaction(metadata: EmailMetadata) =
            dbEvents.insertIncomingEmail(signalClient, emailInsertionApiClient, metadata, activeAccount)

    private fun updateExistingEmailTransaction(metadata: EmailMetadata) =
            dbEvents.updateExistingEmail(metadata, activeAccount)

    private fun acknowledgeEventsIgnoringErrors(eventIdsToAcknowledge: List<Long>): Boolean {
        try {
            if(eventIdsToAcknowledge.isNotEmpty())
                apiClient.acknowledgeEvents(eventIdsToAcknowledge)
        } catch (ex: IOException) {
            // if this request fails, just ignore it, we can acknowledge again later
        }
        return eventIdsToAcknowledge.isNotEmpty()
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
