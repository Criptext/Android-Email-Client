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
        private val db: MailboxLocalDB,
        private val signalClient: SignalClient)
    : DetailedSocketDataHttpClient {

    override fun requestMailDetail(emailData: String, onMailDetailReady: (SocketData.MailDetailResponse) -> Unit) {
        val operation = parseEmail(emailData)
        when(operation) {
            is Result.Success -> {
                onMailDetailReady(SocketData.MailDetailResponse.Ok(operation.value))
            } is Result.Failure -> {
                operation.error.printStackTrace()
            }
        }
    }

    private val parseEmail: (input: String) -> Result<EmailThread, Exception> = {
        input ->
        Result.of {
            loadMetadataContentFromString(input = input)
        }.flatMap(db.getEmailThreadOperation).mapError(HttpErrorHandlingHelper.httpExceptionsToNetworkExceptions)
    }

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
                    val insertedEmailId = db.addEmail(email)
                    db.createContacts(metaData.fromName, metaData.fromRecipientId, insertedEmailId, ContactTypes.FROM)
                    db.createContacts(null, metaData.to, insertedEmailId, ContactTypes.TO)
                    db.createContacts(null, metaData.bcc, insertedEmailId, ContactTypes.BCC)
                    db.createContacts(null,metaData.cc, insertedEmailId, ContactTypes.CC)
                    db.createLabelsForEmailInbox(insertedEmailId)
                    return metaData.threadId
                } else -> {
                    throw Exception("")
            }
        }
    }
}
