package com.criptext.mail.mocks

import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.EmailInsertionSetup
import java.util.*

/**
 * Created by gabriel on 6/28/18.
 */
object MockEmailData {
    fun createNewEmail(dateMilis: Long, number: Int): Email =
            Email(id = number.toLong(), messageId = number.toString(),
                            threadId = "thread$number", unread = true, secure = true,
                            content = "this is message #$number", preview = "message #$number",
                            subject = "message #$number", delivered = DeliveryTypes.DELIVERED,
                            date = Date(dateMilis + number), metadataKey = number + 100L,
                            isMuted = false, unsentDate = Date(dateMilis + number),
                            trashDate = Date(dateMilis + number))

    fun createNewEmail(number: Int) = createNewEmail(System.currentTimeMillis(), number)

    fun createNewEmails(max: Int) = (1..max).map{ createNewEmail(it)}

    fun insertEmailsNeededForTests(db: TestDatabase, labels: List<Label>){
        val fromContact = Contact(1,"mayer@criptext.com", "Mayer Mizrachi")
        (1..2).forEach {
            val seconds = if (it < 10) "0$it" else it.toString()
            val metadata = EmailMetadata.DBColumns(to = listOf("gabriel@criptext.com"),  cc = emptyList(), bcc = emptyList(),
                    fromContact = fromContact, messageId = "gabriel/1/$it",
                    date = "2018-02-21 14:00:$seconds", threadId = "thread#$it",
                    subject = "Test #$it", unread = true, metadataKey = it + 100L,
                    status = DeliveryTypes.NONE, unsentDate = "2018-02-21 14:00:$seconds", secure = true,
                    trashDate = "2018-02-21 14:00:$seconds")
            val decryptedBody = "Hello, this is message #$it"
            EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                    decryptedBody = decryptedBody, labels = labels, files = emptyList(), fileKey = null)
        }
    }
}