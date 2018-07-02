package com.email.scenes.mailbox

import com.email.db.DeliveryTypes
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.email_preview.EmailPreview
import com.email.scenes.mailbox.data.EmailThread
import java.util.*

/**
 * Created by gabriel on 5/2/18.
 */
object MailboxTestUtils {

    fun createNewEmail(dateMilis: Long, number: Int): Email =
            Email(id = number.toLong(), messageId = number.toString(),
                            threadId = "thread$number", unread = true, secure = true,
                            content = "this is message #$number", preview = "message #$number",
                            subject = "message #$number", delivered = DeliveryTypes.DELIVERED,
                            date = Date(dateMilis + number), metadataKey = number + 100L)

    fun createNewEmail(number: Int) = createNewEmail(System.currentTimeMillis(), number)

    fun createEmailThreads(size: Int): List<EmailThread> {
        val dateMilis = System.currentTimeMillis()
        return (1..size)
                .map {
                    val email = createNewEmail(dateMilis, it)
                    val fullEmail = FullEmail(email, labels = listOf(Label.defaultItems.inbox),
                            to = listOf(Contact(1, "gabriel@criptext.com", "gabriel")),
                                    cc = emptyList(), bcc = emptyList(), files = emptyList(),
                            from = Contact(2, "mayer@criptext.com", name = "Mayer"))
                    EmailThread(fullEmail, listOf(), Label.defaultItems.inbox.text, 0)
                }
    }

    fun createEmailPreviews(size: Int) = createEmailThreads(size)
                                           .map { EmailPreview.fromEmailThread(it )}
}
