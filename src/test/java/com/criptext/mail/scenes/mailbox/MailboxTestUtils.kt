package com.criptext.mail.scenes.mailbox

import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.FullEmail
import com.criptext.mail.db.models.Label
import com.criptext.mail.email_preview.EmailPreview
import com.criptext.mail.scenes.mailbox.data.EmailThread
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
                            date = Date(dateMilis + number), metadataKey = number + 100L,
                            isMuted = false, unsentDate = Date(dateMilis + number), trashDate = null,
                            fromAddress = "Mayer Mizrachi <mayer@jigl.com>", replyTo = null)

    fun createNewEmail(number: Int) = createNewEmail(System.currentTimeMillis(), number)

    fun createEmailThreads(size: Int): List<EmailThread> {
        val dateMilis = System.currentTimeMillis()
        return (1..size)
                .map {
                    val email = createNewEmail(dateMilis, it)
                    val fullEmail = FullEmail(email, labels = listOf(Label.defaultItems.inbox),
                            to = listOf(Contact(1, "gabriel@criptext.com", "gabriel", true)),
                                    cc = emptyList(), bcc = emptyList(), files = emptyList(),
                            from = Contact(2, "mayer@criptext.com", name = "Mayer", isTrusted = true), fileKey = null)
                    EmailThread(fullEmail, listOf(), Label.defaultItems.inbox.text, 0, false)
                }
    }

    fun createEmailPreviews(size: Int) = createEmailThreads(size)
                                           .map { EmailPreview.fromEmailThread(it )}
}
