package com.criptext.mail.scenes.emaildetail

import com.criptext.mail.scenes.emaildetail.data.EmailDetailRequest
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 6/27/18.
 */

class EmailDetailControllerResultTest: EmailDetailControllerTest() {
    private lateinit var listenerSlot: CapturingSlot<(EmailDetailResult) -> Unit>
    @Before
    override fun setUp() {
        super.setUp()
        listenerSlot = CapturingSlot()
        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs
    }

    @Test
    fun `after successfully loading emails should send request to read any unread emails`() {
        val loadedEmails = createEmailItemsInThread(4)
                .mapIndexed { index, fullEmail ->
                    // only the latter half are unread
                    if (index < 2) fullEmail.email.unread = true
                    fullEmail
                }
        val result = EmailDetailResult.LoadFullEmailsFromThreadId.Success(
                fullEmailList = loadedEmails
        )

        controller.onStart(null)
        sentRequests.clear()

        // trigger load emails success event
        listenerSlot.captured(result)


        sentRequests.size `should be` 1
        sentRequests.first() `should equal` EmailDetailRequest.ReadEmails(emailIds = listOf(4, 3),
                metadataKeys = listOf(104L, 103L))

    }
}