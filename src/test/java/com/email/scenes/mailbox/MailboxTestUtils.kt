package com.email.scenes.mailbox

import com.email.db.DeliveryTypes
import com.email.db.models.Contact
import com.email.db.models.Email
import com.email.db.models.FullEmail
import com.email.db.models.Label
import com.email.scenes.mailbox.data.EmailThread
import java.util.*

/**
 * Created by gabriel on 5/2/18.
 */
object MailboxTestUtils {

    fun createEmailThreads(size: Int): List<EmailThread> {
        val dateMilis = System.currentTimeMillis()
        return (1..size)
                .map {
                    val email = Email(id = it.toLong(), key = it.toString(), threadid = "thread$it", unread = true,
                            secure = true, content = "this is message #$it", preview = "message #$it",
                            subject = "message #$it", delivered = DeliveryTypes.DELIVERED,
                            date = Date(dateMilis + it), isTrash = false, isDraft = false)
                    val fullEmail = FullEmail(email, labels = listOf(Label.defaultItems.inbox),
                            to = listOf(Contact(1, "gabriel@criptext.com", "gabriel")),
                                    cc = emptyList(), bcc = emptyList(), files = emptyList(),
                            from = Contact(2, "mayer@criptext.com", name = "Mayer"))
                    EmailThread(fullEmail, listOf(Label.defaultItems.inbox), 0)
                }
    }
}
