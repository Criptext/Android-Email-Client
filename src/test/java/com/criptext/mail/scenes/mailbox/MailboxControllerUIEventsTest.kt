package com.criptext.mail.scenes.mailbox

import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.data.LoadParams
import com.criptext.mail.scenes.mailbox.data.MailboxRequest
import com.criptext.mail.scenes.mailbox.ui.EmailThreadAdapter
import com.criptext.mail.scenes.mailbox.ui.MailboxUIObserver
import com.criptext.mail.utils.generaldatasource.data.GeneralRequest
import io.mockk.*
import org.amshove.kluent.`should be empty`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 5/9/18.
 */

class MailboxControllerUIEventsTest : MailboxControllerTest() {

    private val onThreadEventListenerSlot = CapturingSlot<EmailThreadAdapter.OnThreadEventListener>()
    private val onDrawerMenuEventListenerSlot = CapturingSlot<DrawerMenuItemListener>()
    private val observerSlot = CapturingSlot<MailboxUIObserver>()
    @Before
    override fun setUp() {
        super.setUp()
        every {
            scene.attachView(capture(onThreadEventListenerSlot),
                    capture(onDrawerMenuEventListenerSlot), capture(observerSlot), any(),
                    activeAccount.name, activeAccount.userEmail)
        } just Runs
    }

    @Test
    fun `should be able to add items to selectedThreads with the onToggleThreadSelection event`() {

        controller.onStart(null)
        clearMocks(virtualListView)
        clearMocks(scene)

        val listener = onThreadEventListenerSlot.captured

        // add threads to select
        model.threads.addAll(MailboxTestUtils.createEmailPreviews(20))


        //Select 3 threads
        val initialEmailThreads = model.threads.toList()
        listener.onToggleThreadSelection(initialEmailThreads[0], 0)
        listener.onToggleThreadSelection(initialEmailThreads[2], 2)
        listener.onToggleThreadSelection(initialEmailThreads[3], 3)

        verifySequence {
            virtualListView.notifyItemChanged(0)
            virtualListView.notifyItemChanged(2)
            virtualListView.notifyItemChanged(3)
        }
        verify { scene.showMultiModeBar(any()) }
        model.selectedThreads.length() `should equal` 3
    }

    @Test
    fun `should be able to remove items to selectedThreads with the onToggleThreadSelection event`() {

        controller.onStart(null)
        clearMocks(virtualListView)
        clearMocks(scene)

        val listener = onThreadEventListenerSlot.captured

        // add threads to select
        model.threads.addAll(MailboxTestUtils.createEmailPreviews(20))

        //Mark 3 threads as selected
        val initialEmailThreads = model.threads.toList()
        model.selectedThreads.add(initialEmailThreads[0])
        model.selectedThreads.add(initialEmailThreads[2])
        model.selectedThreads.add(initialEmailThreads[3])

        // deselect 3 threads
        listener.onToggleThreadSelection(initialEmailThreads[3], 3)
        listener.onToggleThreadSelection(initialEmailThreads[2], 2)
        listener.onToggleThreadSelection(initialEmailThreads[0], 0)

        verifySequence {
            virtualListView.notifyItemChanged(3)
            virtualListView.notifyItemChanged(2)
            virtualListView.notifyItemChanged(0)
        }

        verify {
            scene.hideMultiModeBar()
        }

        model.selectedThreads.length() `should equal` 0
    }

    @Test
    fun `after clicking button to move to spam, should send MoveEmailThread request`() {
        controller.onStart(null)
        sentRequests.clear() // ignore requests sent during `onStart`()

        // capture onMoveThreadsListener
        val onMoveThreadsListenerSlot = CapturingSlot<OnMoveThreadsListener>()
        every {
            scene.showDialogMoveTo(capture(onMoveThreadsListenerSlot), Label.defaultItems.inbox.text)
        } just Runs

        // set 2 selected threads
        val threads = MailboxTestUtils.createEmailPreviews(20)
        model.threads.addAll(threads)
        model.selectedThreads.add(threads[0])
        model.selectedThreads.add(threads[2])

        //trigger bulk move
        controller.onOptionsItemSelected(R.id.mailbox_move_to)
        //trigger click move to spam
        onMoveThreadsListenerSlot.captured.onMoveToSpamClicked()

        val sentRequest = sentRequests.first()
        sentRequest `should be instance of` MailboxRequest.MoveEmailThread::class.java
    }

    @Test
    fun `pulling down should force mailbox to update`() {
        controller.onStart(null)
        clearMocks(virtualListView)
        sentGeneralRequests.clear()

        // set existing threads
        model.threads.addAll(MailboxTestUtils.createEmailPreviews(20))

        observerSlot.captured.onRefreshMails() // trigger pull down to refresh

        // verify ui is showing "refreshing" animation
        verify { scene.showRefresh() }

        val sentRequest = sentGeneralRequests.first()
        sentRequest `should equal` GeneralRequest.UpdateMailbox(label = model.selectedLabel,
                loadedThreadsCount = 20, isActiveAccount = true, activeAccount = null)
    }

    @Test
    fun `after clicking a navigation label, should clear threads list and load threads with new label`() {
        controller.onStart(null)
        sentRequests.clear()
        clearMocks(virtualListView)

        // trigger ui event
        onDrawerMenuEventListenerSlot.captured.onNavigationItemClick(NavigationMenuOptions.TRASH)

        // should change selected label
        model.selectedLabel `should equal` Label.defaultItems.trash
        
        // should send load request
        val sentRequest = sentRequests.first()
        sentRequest `should equal` MailboxRequest.LoadEmailThreads(
                label = Label.defaultItems.trash.text,
                loadParams = LoadParams.Reset(size = 20),
                userEmail = "gabriel@${Contact.mainDomain}",
                filterUnread = false)
    }
}