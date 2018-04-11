package com.email.websocket

import com.email.api.ApiCall
import com.email.api.HttpErrorHandlingHelper
import com.email.db.*
import com.email.db.models.*
import com.email.scenes.mailbox.data.EmailThread
import com.email.signal.SignalClient
import com.email.signal.SignalStoreCriptext
import com.email.utils.DateUtils
import com.email.utils.HTMLUtils
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import okhttp3.OkHttpClient
import org.json.JSONObject

/**
 * Created by gabriel on 9/27/17.
 */

class DetailedSocketDataOkHttpClient(
        private val account: ActiveAccount,
        private val database: AppDatabase)
    : DetailedSocketDataHttpClient {


    private val signalClient = SignalClient.Default(SignalStoreCriptext(database))
    override fun requestMailDetail(emailData: String, onMailDetailReady: (SocketData.MailDetailResponse) -> Unit) {
        val operation = parseEmail(emailData)
        when(operation) {
            is Result.Success -> {
                onMailDetailReady(SocketData.MailDetailResponse.Ok(operation.value))
            } is Result.Failure -> {
                operation.error.printStackTrace()
            }
        }

        val operationResult = parseEmail(emailData).
                mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    }

    private val parseEmail: (input: String) -> Result<EmailThread, Exception> = {
        input ->
        Result.of {
            loadMetadataContentFromString(input = input)
        }.flatMap(getEmailThreadOperation).mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    }

    private val getEmailThreadOperation: (threadId: String) -> Result<EmailThread, Exception> = {
        threadId ->
        Result.of {
            val email = database.emailDao().getLatestEmailFromThreadId(threadId)
            getEmailThreadFromEmail(email)
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    }
    private fun getEmailThreadFromEmail(email: Email): EmailThread {
        val id = email.id!!
        val labels = database.emailLabelDao().getLabelsFromEmail(id)
        val contactsCC = database.emailContactDao().getContactsFromEmail(id, ContactTypes.CC)
        val contactsBCC = database.emailContactDao().getContactsFromEmail(id, ContactTypes.BCC)
        val contactsFROM = database.emailContactDao().getContactsFromEmail(id, ContactTypes.FROM)
        val contactsTO = database.emailContactDao().getContactsFromEmail(id, ContactTypes.TO)
        val files = database.fileDao().getAttachmentsFromEmail(id)

        val contactFrom = if(contactsFROM.isEmpty()) {
            null
        } else {
            contactsFROM[0]
        }

        return EmailThread(
                latestEmail = FullEmail(
                        email = email,
                        bcc = contactsBCC,
                        cc = contactsCC,
                        from = contactFrom,
                        files = files,
                        labels = labels,
                        to = contactsTO ),
                labelsOfMail = database.emailLabelDao().getLabelsFromEmail(email.id!!) as ArrayList<Label>
        )

    }
    private val httpClient = OkHttpClient()

    private data class DecryptData(
            val from: String,
            val deviceId: Int,
            val encryptedData: String)


    private val decryptBody: (input: DecryptData) -> Result<String, Exception> = {
        input ->
        Result.of {
            signalClient.decryptMessage(
                    recipientId = input.from,
                    deviceId = input.deviceId,
                    encryptedB64 = input.encryptedData)
        }.mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    }

    fun getBodyFromEmail(uuid: String): String {
        val request = ApiCall.getBodyFromEmail(
                token = account.jwt,
                uuid= uuid
        )
        return ApiCall.executeRequest(request)
    }

    private fun loadMetadataContentFromString(input: String): String {
            val fullData = JSONObject(input)
            val metaData = EmailMetaData(stringMetadata = fullData.getString("params"))

            val resultOperationDecryptAndInsert = Result.of {
                val encryptedBody = getBodyFromEmail(metaData.bodyKey)
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
                            id=null,
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
                            delivered = DeliveryTypes.RECEIVED,
                            content = bodyContent
                    )
                    val insertedEmailId = addEmail(email)
                    createContacts(metaData.fromRecipientId, insertedEmailId, ContactTypes.FROM)
                    createContacts(metaData.to, insertedEmailId, ContactTypes.TO)
                    createContacts(metaData.bcc, insertedEmailId, ContactTypes.BCC)
                    createContacts(metaData.cc, insertedEmailId, ContactTypes.CC)
                    createLabelsForEmailInbox(insertedEmailId)
                    return metaData.threadId
                } else -> {
                    throw Exception("")
            }
        }
    }

    fun addEmail(email: Email): Int {
        database.emailDao().insert(email)
        return database.emailDao().getLastInsertedId()
    }

    fun createLabelsForEmailInbox(insertedEmailId: Int) {
        val labelInbox = database.labelDao().get(LabelTextTypes.INBOX)
        database.emailLabelDao().insert(EmailLabel(
                labelId = labelInbox.id!!,
                emailId = insertedEmailId))
    }

    private fun insertContact(contactEmail: String, emailId: Int, type: ContactTypes) {
        if(contactEmail.isNotEmpty()) {
            val contact = Contact(email = contactEmail, name = contactEmail)
            val emailContact = EmailContact(
                    contactId = contactEmail,
                    emailId = emailId,
                    type = type)
            database.contactDao().insert(contact)
            database.emailContactDao().insert(emailContact)
        }
    }

    fun createContacts(contacts: String, insertedEmailId: Int, type: ContactTypes) {
        if(type == ContactTypes.FROM) {
            insertContact(
                    contactEmail = contacts,
                    emailId = insertedEmailId,
                    type = type)
            return
        }

        val contactsList = contacts.split(",")
        contactsList.forEach { contactEmail ->
            insertContact(
                    contactEmail = contactEmail,
                    emailId = insertedEmailId,
                    type = type)
        }
    }
}
