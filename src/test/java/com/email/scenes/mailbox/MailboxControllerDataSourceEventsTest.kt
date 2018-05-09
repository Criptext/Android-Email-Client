package com.email.scenes.mailbox

import com.email.scenes.mailbox.data.MailboxRequest
import com.email.scenes.mailbox.data.MailboxResult
import io.mockk.*
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 5/9/18.
 */
class MailboxControllerDataSourceEventsTest: MailboxControllerTest() {
    private lateinit var listenerSlot: CapturingSlot<(MailboxResult) -> Unit>

    @Before
    override fun setUp() {
        super.setUp()
        listenerSlot = CapturingSlot()
        every { dataSource::listener.set(capture(listenerSlot)) } just Runs
    }

    @Test
    fun `after loading threads, should add them to the model and render them`() {
        controller.onStart(null)
        clearMocks(virtualListView)

        val loadedThreads = MailboxTestUtils.createEmailThreads(20)

        // trigger load complete event
        listenerSlot.captured(MailboxResult.LoadEmailThreads.Success(emailThreads = loadedThreads,
                isReset = false,
                mailboxLabel = model.selectedLabel.text))

        model.threads.size `should equal` 20
        verify { virtualListView.notifyDataSetChanged() }
    }

    @Test
    fun `after loading threads, should try to fetch pending events with UpdateMailbox request`() {
        controller.onStart(null)

        val loadedThreads = MailboxTestUtils.createEmailThreads(20)

        // trigger load complete event
        listenerSlot.captured(MailboxResult.LoadEmailThreads.Success(emailThreads = loadedThreads,
                isReset = false,
                mailboxLabel = model.selectedLabel.text))

        // verify UpdateMailbox request sent
        verify {
            dataSource.submitRequest(MailboxRequest.UpdateMailbox(label = model.selectedLabel,
                loadedThreadsCount = loadedThreads.size))
        }
    }

    @Test
    fun `after loading threads, should not send UpdateMailbox request if already updated recently`() {
        model.lastSync = System.currentTimeMillis() // pretend it was updated recently
        controller.onStart(null)

        val loadedThreads = MailboxTestUtils.createEmailThreads(20)

        // clear all calls to data source up to this point
        clearMocks(dataSource)

        // trigger load complete event
        listenerSlot.captured(MailboxResult.LoadEmailThreads.Success(emailThreads = loadedThreads,
                isReset = false,
                mailboxLabel = model.selectedLabel.text))

        // verify no requests were NOT sent
        verify(inverse = true) {
            dataSource.submitRequest(any())
        }
    }

    @Test
    fun `after loading threads, if isReset is true should replace existing threads`() {
        controller.onStart(null)
        clearMocks(virtualListView)

        // set existing threads
        model.threads.addAll(MailboxTestUtils.createEmailThreads(40))

        val threadsFromUpdate = MailboxTestUtils.createEmailThreads(20)

        // trigger load complete event
        listenerSlot.captured(MailboxResult.LoadEmailThreads.Success(emailThreads = threadsFromUpdate,
                isReset = true,
                mailboxLabel = model.selectedLabel.text))

        model.threads.size `should be` 20
        verify { virtualListView.notifyDataSetChanged() }

    }

    @Test
    fun `after updating mailbox, if loadedThreads is not null, should use them to replace existing threads`() {
        controller.onStart(null)
        clearMocks(virtualListView)

        // set existing threads
        model.threads.addAll(MailboxTestUtils.createEmailThreads(40))

        val threadsFromUpdate = MailboxTestUtils.createEmailThreads(20)

        // trigger load complete event
        listenerSlot.captured(MailboxResult.UpdateMailbox.Success(mailboxThreads = threadsFromUpdate,
                mailboxLabel = model.selectedLabel, isManual = false))

        model.threads.size `should be` 20
        verify { virtualListView.notifyDataSetChanged() }

    }

    @Test
    fun `after updating mailbox, if loadedThreads is null, should NOT update anything`() {
        controller.onStart(null)
        clearMocks(virtualListView)

        // set existing threads
        model.threads.addAll(MailboxTestUtils.createEmailThreads(40))

        // trigger load complete event
        listenerSlot.captured(MailboxResult.UpdateMailbox.Success(mailboxThreads = null,
                mailboxLabel = model.selectedLabel, isManual = false))

        model.threads.size `should be` 40
        verify(inverse = true) { virtualListView.notifyDataSetChanged() }
    }

    @Test
    fun `after updating mailbox, if isManual is true, should try to clear "refreshing" animation`() {
        controller.onStart(null)
        clearMocks(virtualListView)
        clearMocks(scene)

        // set existing threads
        model.threads.addAll(MailboxTestUtils.createEmailThreads(40))

        // trigger load complete event
        listenerSlot.captured(MailboxResult.UpdateMailbox.Success(mailboxThreads = null,
                mailboxLabel = model.selectedLabel, isManual = true))

        verify { scene.clearRefreshing() }
    }
}
