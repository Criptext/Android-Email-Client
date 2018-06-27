package com.email.scenes.emailDetail

import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.data.EmailDetailResult
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
    fun `after successfully loading emails should send request to read them`() {
        val result = EmailDetailResult.LoadFullEmailsFromThreadId.Success(
                fullEmailList = createEmailItemsInThread(2)
        )

        controller.onStart(null)
        sentRequests.clear()

        // trigger load emails success event
        listenerSlot.captured(result)


        sentRequests.size `should be` 1
        sentRequests.first() `should equal` EmailDetailRequest.ReadEmails(emailIds = listOf(2, 1),
                metadataKeys = listOf(2, 1))

    }
}