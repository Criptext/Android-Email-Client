package com.email.scenes.mailbox.data

import com.email.R
import com.email.api.HttpErrorHandlingHelper
import com.email.bgworker.BackgroundWorker
import com.email.db.*
import com.email.db.models.ActiveAccount
import com.email.db.models.Email
import com.email.db.models.Label
import com.email.signal.DecryptData
import com.email.signal.SignalClient
import com.email.utils.DateUtils
import com.email.utils.HTMLUtils
import com.email.utils.UIMessage
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONArray
import org.json.JSONObject
import org.whispersystems.libsignal.DuplicateMessageException

/**
 * Created by sebas on 3/22/18.
 */

class UpdateMailboxWorker(
        private val signalClient: SignalClient,
        private val db: MailboxLocalDB,
        private val activeAccount: ActiveAccount,
        private val label: MailFolders,
        override val publishFn: (
                MailboxResult.UpdateMailbox) -> Unit)
    : BackgroundWorker<MailboxResult.UpdateMailbox> {

    private val apiClient = MailboxAPIClient(activeAccount.jwt)
    override val canBeParallelized = false

    override fun catchException(ex: Exception): MailboxResult.UpdateMailbox {

        val message = createErrorMessage(ex)
        return MailboxResult.UpdateMailbox.Failure(label, message)
    }
    private fun fetchPendingEvents():Result<String, Exception> {
        return Result.of {
            apiClient.getPendingEvents()
        }
    }
    private fun selectRejectedLabels(): List<Label> {
        val commonRejectedLabels = listOf( MailFolders.SPAM, MailFolders.TRASH)
        return when(label) {
           MailFolders.SENT,
           MailFolders.INBOX,
           MailFolders.ARCHIVED,
           MailFolders.STARRED -> {
               return db.getLabelsFromLabelType(
                       labelTextTypes = commonRejectedLabels)
           }
            else -> {
                return emptyList()
            }
        }
    }

    private val parseEmails: (input: String) -> Result<List<EmailThread>, Exception> = {
        input ->
        Result.of {
            loadMetadataContentFromString(input = input)
        }.flatMap{Result.of{
            val rejectedLabels = selectRejectedLabels()
            db.getEmailsFromMailboxLabel(
                    labelTextTypes = label,
                    limit = 10,
                    rejectedLabels = rejectedLabels,
                    oldestEmailThread = null)
        }}
    }

    override fun work(): MailboxResult.UpdateMailbox? {
        val operationResult = fetchPendingEvents()
                .mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
                .flatMap(parseEmails)

        return when(operationResult) {
            is Result.Success -> {
                return MailboxResult.UpdateMailbox.Success(
                        mailboxLabel = label,
                        isManual = true,
                        mailboxThreads = operationResult.value
                )
            }

            is Result.Failure -> MailboxResult.UpdateMailbox.Failure(
                    label,
                    createErrorMessage(operationResult.error))
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

    private val decryptBody: (input: DecryptData) -> Result<String, Exception> = {
        input ->
        Result.of {
            signalClient.decryptMessage(
                            recipientId = input.from,
                            deviceId = input.deviceId,
                            encryptedB64 = input.encryptedData)
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    }

    private fun loadMetadataContentFromString(input: String) {
        val listMetadatas = JSONArray(input)
        for(i in 0 until listMetadatas.length()) {

            val fullData = JSONObject(listMetadatas.get(i).toString())
            val metaData = EmailMetaData(stringMetadata = fullData.getString("params"))

            val resultOperationDecryptAndInsert = Result.of {
                val encryptedBody = apiClient.getBodyFromEmail(metaData.bodyKey)
                DecryptData(
                        from = metaData.fromRecipientId,
                        deviceId = 1,
                        encryptedData = encryptedBody)
            }.flatMap(decryptBody)
            when(resultOperationDecryptAndInsert) {
                is Result.Success -> {

                    val bodyContent = resultOperationDecryptAndInsert.value
                    val bodyWithoutHTML = HTMLUtils.html2text(bodyContent)
                    val preview   = if (bodyWithoutHTML.length > 20 )
                                        bodyWithoutHTML.substring(0,20)
                                    else bodyWithoutHTML

                    val email = Email(
                            id = 0,
                            unread = true,
                            date = DateUtils.getDateFromString(
                                    metaData.date,
                                    null),
                            threadid = metaData.threadId,
                            subject = metaData.subject,
                            isTrash = false,
                            secure = true,
                            preview = preview,
                            key = metaData.bodyKey,
                            isDraft = false,
                            delivered = DeliveryTypes.OPENED,
                            content = bodyContent
                            )
                    val insertedEmailId = db.addEmail(email)
                    db.createContacts(metaData.fromName, metaData.fromRecipientId, insertedEmailId, ContactTypes.FROM)
                    db.createContacts(null,metaData.to, insertedEmailId, ContactTypes.TO)
                    db.createContacts(null,metaData.bcc, insertedEmailId, ContactTypes.BCC)
                    db.createContacts(null,metaData.cc, insertedEmailId, ContactTypes.CC)
                    db.createLabelsForEmailInbox(insertedEmailId)
                } else -> {

                }
            }
        }
    }

}
