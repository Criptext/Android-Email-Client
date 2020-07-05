package com.criptext.mail.push.workers

import android.os.Build
import com.crashlytics.android.Crashlytics
import com.criptext.mail.R
import com.criptext.mail.api.EmailInsertionAPIClient
import com.criptext.mail.api.Hosts
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.ServerErrorException
import com.criptext.mail.bgworker.BackgroundWorker
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.AntiPushMap
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.PushTypes
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.scenes.mailbox.data.MailboxAPIClient
import com.criptext.mail.signal.SignalClient
import com.criptext.mail.signal.SignalEncryptedData
import com.criptext.mail.signal.SignalStoreCriptext
import com.criptext.mail.signal.SignalUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.eventhelper.EventHelper
import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso
import org.whispersystems.libsignal.DuplicateMessageException


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
    private lateinit var activeAccount: ActiveAccount
    private lateinit var signalClient: SignalClient
    private var notificationId: Int = -1

    override fun catchException(ex: Exception): PushResult.NewEmail {
        val message = createErrorMessage(ex)
        return PushResult.NewEmail.Failure(label, message, ex, pushData, shouldPostNotification,
                notificationId,
                activeAccount)
    }

    private fun processFailure(failure: Result.Failure<String,
            Exception>): PushResult.NewEmail {
        return if (failure.error is EventHelper.NothingNewException)
            PushResult.NewEmail.Success(
                    mailboxLabel = label,
                    isManual = true,
                    shouldPostNotification = shouldPostNotification,
                    pushData = pushData,
                    senderImage = null,
                    notificationId = notificationId,
                    activeAccount = activeAccount)
        else
            PushResult.NewEmail.Failure(
                    mailboxLabel = label,
                    message = createErrorMessage(failure.error),
                    exception = failure.error,
                    pushData = pushData,
                    shouldPostNotification = shouldPostNotification,
                    notificationId = notificationId,
                    activeAccount = activeAccount)
    }

    override fun work(reporter: ProgressReporter<PushResult.NewEmail>)
            : PushResult.NewEmail? {

        val dbAccount = dbEvents.getAccount(pushData["account"], pushData["domain"]) ?: return PushResult.NewEmail.Failure(
                mailboxLabel = label,
                message = createErrorMessage(EventHelper.AccountNotFoundException()),
                exception = EventHelper.AccountNotFoundException(),
                pushData = pushData,
                shouldPostNotification = shouldPostNotification,
                notificationId = -1,
                activeAccount = activeAccount)

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
                    notificationId = notificationId,
                    activeAccount = activeAccount)
        }



        signalClient = SignalClient.Default(SignalStoreCriptext(db, activeAccount))
        apiClient = MailboxAPIClient(httpClient, activeAccount.jwt)
        emailInsertionApiClient = EmailInsertionAPIClient(httpClient, activeAccount.jwt)

        val newData = mutableMapOf<String, String>()
        newData.putAll(pushData)

        val operationResult = Result.of {
            val encryptedPreview = newData["preview"] ?: throw NullPointerException()
            getDecryptedEmailPreview(signalClient = signalClient, encryptedPreview = encryptedPreview, metadata = newData)
        }

        return when(operationResult) {
            is Result.Success -> {
                newData["preview"] = operationResult.value
                newData["subject"] = pushData["body"] ?: ""
                newData["hasInlineImages"] = pushData["hasInlineImages"] ?: ""
                newData["name"] = pushData["title"] ?: pushData["senderId"].plus("@${pushData["senderDomain"]}")
                newData["email"] = pushData["email"] ?: pushData["senderId"].plus("@${pushData["senderDomain"]}")
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
                        notificationId = notificationId,
                        activeAccount = activeAccount
                )
            }

            is Result.Failure -> processFailure(operationResult)
        }
    }

    private fun getDecryptedEmailPreview(signalClient: SignalClient,
                                         encryptedPreview: String,
                                         metadata: Map<String, String>): String {
        val senderId = getSenderId(metadata)
        val messageType = SignalEncryptedData.Type.fromInt(metadata["previewMessageType"]?.toInt()) ?: return encryptedPreview
        return if (senderId.first != null) {
            val encryptedData = SignalEncryptedData(
                    encryptedB64 = encryptedPreview,
                    type = messageType)

            decryptMessage(signalClient = signalClient,
                    recipientId = senderId.second, deviceId = senderId.first!!,
                    encryptedData = encryptedData,
                    isFromBob = senderId.second == SignalUtils.externalRecipientId)
        } else
            encryptedPreview
    }

    private fun getSenderId(metadata: Map<String, String>): Pair<Int?, String>{
        val senderDeviceId = metadata["deviceId"]?.toInt()
        val senderDomain = metadata["senderDomain"] ?: throw NullPointerException()
        val senderRecipientId = if(senderDomain.isEmpty() || senderDomain == Contact.mainDomain) metadata["senderId"]!!
        else metadata["senderId"].plus("@$senderDomain")
        return Pair(senderDeviceId, senderRecipientId)
    }

    private fun decryptMessage(signalClient: SignalClient, recipientId: String, deviceId: Int,
                               encryptedData: SignalEncryptedData, isFromBob: Boolean): String {
        return try {
            signalClient.decryptMessage(recipientId = recipientId,
                    deviceId = deviceId,
                    encryptedData = encryptedData)
        } catch (ex: Exception) {
            if (ex is DuplicateMessageException) throw ex
            Crashlytics.logException(if(isFromBob) EmailInsertionSetup.BobDecryptionException() else ex)
            ""
        }
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
                    notificationId = db.antiPushMapDao().insert(AntiPushMap(0, metadataKey, activeAccount.id)).toInt()
            }
            PushTypes.linkDevice.actionCode(),
            PushTypes.syncDevice.actionCode() -> {
                val randomId = data["randomId"]
                if(!randomId.isNullOrEmpty())
                    notificationId = db.antiPushMapDao().insert(AntiPushMap(0, randomId, activeAccount.id)).toInt()
        }
            else -> {
                val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                val type = PushTypes.fromActionString(action)
                notificationId =  if(isPostNougat) type.requestCodeRandom() else type.requestCode()
            }
        }
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
