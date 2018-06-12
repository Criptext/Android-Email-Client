package com.email.scenes.emailDetail

import com.email.R
import com.email.scenes.emaildetail.data.EmailDetailRequest
import com.email.scenes.emaildetail.ui.FullEmailListAdapter
import com.email.scenes.mailbox.OnMoveThreadsListener
import io.mockk.*
import org.amshove.kluent.`should be instance of`
import org.junit.Before
import org.junit.Test

class EmailDetailControllerUIEventsTest: EmailDetailControllerTest(){

    private val fullEmailEventListener = CapturingSlot<FullEmailListAdapter.OnFullEmailEventListener>()

    @Before
    override fun setUp() {
        super.setUp()
        every {
            scene.attachView(capture(fullEmailEventListener), any())
        }just Runs
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


}