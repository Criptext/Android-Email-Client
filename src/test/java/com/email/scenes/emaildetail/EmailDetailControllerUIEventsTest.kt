package com.email.scenes.emaildetail

import com.email.ExternalActivityParams
import com.email.R
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.data.EmailDetailResult
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.mailbox.OnMoveThreadsListener
import com.email.utils.UIMessage
import com.email.utils.virtuallist.VirtualList
import io.mockk.*
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.junit.Before
import org.junit.Test

class EmailDetailControllerUIEventsTest: EmailDetailControllerTest(){

    private val fullEmailEventListener = CapturingSlot<FullEmailListAdapter.OnFullEmailEventListener>()
    private lateinit var listenerSlot: CapturingSlot<(EmailDetailResult) -> Unit>

    @Before
    override fun setUp() {
        super.setUp()
        every {
            scene.attachView(capture(fullEmailEventListener), any())
        }just Runs

        listenerSlot = CapturingSlot()
        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs
    }

    private fun simulateDownloadEvent(result: EmailDetailResult.DownloadFile) {
        listenerSlot.captured(result)
    }

    private fun simulateLoadOfEmails(size: Int) {
        listenerSlot.captured(EmailDetailResult.LoadFullEmailsFromThreadId.Success(createEmailItemsInThread(size)))
    }

    @Test
    fun `after clicking option to move to spam, should send MoveEmailThread request`() {

        controller.onStart(null)
        sentRequests.clear() // ignore requests sent during `onStart`()

        // capture onMoveThreadsListener
        val onMoveThreadsListenerSlot = CapturingSlot<OnMoveThreadsListener>()
        every {
            scene.showDialogMoveTo(capture(onMoveThreadsListenerSlot))
        } just Runs

        controller.onOptionsItemSelected(R.id.mailbox_move_to)
        //trigger click move to spam
        onMoveThreadsListenerSlot.captured.onMoveToSpamClicked()

        val sentRequest = sentRequests.first()
        sentRequest `should be instance of` EmailDetailRequest.MoveEmailThread::class.java
    }

    @Test
    fun `Loaded emails should be added to model`() {
        controller.onStart(null)
        sentRequests.clear()
        simulateLoadOfEmails(2)

        model.fullEmailList.size `should be equal to` 2
    }

    @Test
    fun `clicking an attachment should trigger download worker`() {
        controller.onStart(null)
        sentRequests.clear()
        simulateLoadOfEmails(2)

        val selectedIndex = 0
        val selectedFile = model.fullEmailList[selectedIndex].files[selectedIndex]
        fullEmailEventListener.captured.onAttachmentSelected(0, 0)

        verify { dataSource.submitRequest(EmailDetailRequest.DownloadFile(selectedFile.token, selectedFile.emailId)) }
    }

    @Test
    fun `download progress should be set to respective file`() {
        controller.onStart(null)
        sentRequests.clear()
        simulateLoadOfEmails(2)
        val selectedIndex = 0
        val selectedFile = model.fullEmailList[selectedIndex].files[selectedIndex]

        simulateDownloadEvent(EmailDetailResult.DownloadFile.Progress(selectedFile.emailId, selectedFile.token, 50))
        model.fullEmailList[selectedIndex].files[selectedIndex].progress `should be equal to` 50

        verify { scene.updateAttachmentProgress(selectedIndex, selectedIndex) }
    }

    @Test
    fun `download success should call launch activity`() {
        controller.onStart(null)
        sentRequests.clear()

        simulateDownloadEvent(EmailDetailResult.DownloadFile.Success("/test.pdf"))
        host.activityLaunched `should be equal to` true
    }

    @Test
    fun `download failure should display error message`() {
        controller.onStart(null)
        sentRequests.clear()
        simulateLoadOfEmails(2)
        val selectedIndex = 0
        val selectedFile = model.fullEmailList[selectedIndex].files[selectedIndex]

        simulateDownloadEvent(EmailDetailResult.DownloadFile.Failure(selectedFile.token, UIMessage(R.string.error_downloading_file)))
        verify { scene.showError(UIMessage(R.string.error_downloading_file)) }
    }


}