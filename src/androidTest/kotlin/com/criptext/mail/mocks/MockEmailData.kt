package com.criptext.mail.mocks

import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import com.criptext.mail.utils.EmailUtils
import java.io.File
import java.util.*

/**
 * Created by gabriel on 6/28/18.
 */
object MockEmailData {
    private val fromContact = Contact(1,"mayer@criptext.com", "Mayer Mizrachi", true, 0, 0)

    fun createNewEmail(dateMilis: Long, number: Int, accountId: Long): Email =
            Email(id = number.toLong(), messageId = number.toString(),
                            threadId = "thread$number", unread = true, secure = true,
                            content = "this is message #$number", preview = "message #$number",
                            subject = "message #$number", delivered = DeliveryTypes.DELIVERED,
                            date = Date(dateMilis + number), metadataKey = number + 100L,
                            unsentDate = Date(dateMilis + number),
                            trashDate = Date(dateMilis + number), boundary = null,
                            fromAddress = fromContact.email, replyTo = null, accountId = accountId)

    fun createNewEmail(number: Int, accountId: Long) = createNewEmail(System.currentTimeMillis(), number, accountId)

    fun createNewEmails(max: Int, accountId: Long) = (1..max).map{ createNewEmail(it, accountId)}

    fun insertEmailsNeededForTests(db: TestDatabase, labels: List<Label>, filesDir: File, recipientId: String,
                                   domain: String,
                                   toList: List<String> = listOf("gabriel@criptext.com"), accountId: Long){
        (1..2).forEach {
            val seconds = if (it < 10) "0$it" else it.toString()
            val metadata = EmailMetadata.DBColumns(to = toList,  cc = emptyList(), bcc = emptyList(),
                    fromContact = fromContact, messageId = "gabriel/1/$it",
                    date = "2018-02-21 14:00:$seconds", threadId = "thread#$it",
                    subject = "Test #$it", unread = true, metadataKey = it + 100L,
                    status = DeliveryTypes.NONE, unsentDate = "2018-02-21 14:00:$seconds", secure = true,
                    trashDate = "2018-02-21 14:00:$seconds", replyTo = null, boundary = null)
            val decryptedBody = "Hello, this is message #$it"
            EmailUtils.saveEmailInFileSystem(
                    filesDir = filesDir,
                    recipientId = recipientId,
                    metadataKey = metadata.metadataKey,
                    content = decryptedBody,
                    headers = null,
                    domain = domain)
            EmailInsertionSetup.exec(dao = db.emailInsertionDao(),
                    metadataColumns = metadata, preview = decryptedBody,
                    labels = labels, files = emptyList(), fileKey = null, accountId = accountId)
        }
    }
}