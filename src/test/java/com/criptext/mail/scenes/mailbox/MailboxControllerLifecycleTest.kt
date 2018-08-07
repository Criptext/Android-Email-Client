package com.criptext.mail.scenes.mailbox

import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.LoadParams
import com.criptext.mail.scenes.mailbox.data.MailboxRequest
import com.criptext.mail.utils.EmailAddressUtils
import io.mockk.verifySequence
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be instance of`
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 5/9/18.
 */
class MailboxControllerLifecycleTest: MailboxControllerTest() {
    @Before
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun `should forward onStart and onStop to FeedController`() {
        controller.onStart(null)
        controller.onStop()

        verifySequence {
            feedController.onStart()
            feedController.onStop()
        }
    }

    @Test
    fun `on a cold start should try get menu info and load threads from db`() {
        controller.onStart(null)

        sentRequests.size `should be` 2

        val firstRequest = sentRequests[0]
        val secondRequest = sentRequests[1]


        firstRequest `should be instance of` MailboxRequest.GetMenuInformation::class.java

        secondRequest `should equal` MailboxRequest.LoadEmailThreads(
                label = Label.defaultItems.inbox.text,
                loadParams = LoadParams.Reset(size = 20),
                userEmail = "gabriel@${Contact.mainDomain}"
        )
    }

    @Test
    fun `onStart, should not try to load threads if already synced and is not empty`() {
        // set as already synced
        model.lastSync = System.currentTimeMillis()
        // set as not empty
        model.threads.addAll(MailboxTestUtils.createEmailPreviews(20))

        controller.onStart(null)


        if (sentRequests.size > 0) {
            sentRequests.forEach {
                it `should not be instance of` MailboxRequest.LoadEmailThreads::class.java
            }
        }
    }
}