package com.email.scenes.mailbox.data

import com.email.db.models.Label
import com.email.scenes.mailbox.MailboxTestUtils
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.`should be`
import org.junit.Test

/**
 * Created by gabriel on 5/9/18.
 */

class LoadEmailThreadsWorkerTest: MailboxWorkerTest() {

    @Test
    fun `should load the first inbox page from database`() {
        val selectedFolder = Label.defaultItems.inbox.text
        val expectedThreads = MailboxTestUtils.createEmailThreads(20)

        // prepare db mock
        every {
            db.getEmailsFromMailboxLabel(labelTextTypes = selectedFolder, limit = 20,
                oldestEmailThread = null, rejectedLabels = any())
        } returns expectedThreads

        dataSource.submitRequest(MailboxRequest.LoadEmailThreads(Label.defaultItems.inbox.text,
                loadParams = LoadParams.NewPage(20, null)))

        runner._work(mockk()) // execute worker

        val result = lastResult as MailboxResult.LoadEmailThreads.Success

        result.isReset `should be` false
        result.mailboxLabel `should be` selectedFolder
        result.emailThreads `should be` expectedThreads
    }

    @Test
    fun `should load a n+1 inbox page from database`() {
        val selectedFolder = Label.defaultItems.inbox.text
        val newThreads = MailboxTestUtils.createEmailThreads(40).reversed()
        val currentThreads = newThreads.subList(0, 20)
        val expectedThreads = newThreads.subList(20, 40)

        // prepare db mock
        every {
            db.getEmailsFromMailboxLabel(labelTextTypes = selectedFolder, limit = 20,
                oldestEmailThread = currentThreads.last(), rejectedLabels = any())
        } returns expectedThreads

        dataSource.submitRequest(MailboxRequest.LoadEmailThreads(Label.defaultItems.inbox.text,
                loadParams = LoadParams.NewPage(20, currentThreads.last())))

        runner._work(mockk()) // execute worker

        val result = lastResult as MailboxResult.LoadEmailThreads.Success

        result.isReset `should be` false
        result.mailboxLabel `should be` selectedFolder
        result.emailThreads `should be` expectedThreads
    }

    @Test
    fun `should load inbox threads from beginning with the "Reset" parameter`() {
        val selectedFolder = Label.defaultItems.inbox.text
        val expectedThreads = MailboxTestUtils.createEmailThreads(20)

        // prepare db mock
        every {
            db.getEmailsFromMailboxLabel(labelTextTypes = selectedFolder, limit = 20,
                oldestEmailThread = null, rejectedLabels = any())
        } returns expectedThreads

        dataSource.submitRequest(MailboxRequest.LoadEmailThreads(Label.defaultItems.inbox.text,
                loadParams = LoadParams.Reset(20)))

        runner._work(mockk()) // execute worker

        val result = lastResult as MailboxResult.LoadEmailThreads.Success

        result.isReset `should be` true
        result.mailboxLabel `should be` selectedFolder
        result.emailThreads `should be` expectedThreads
    }
}