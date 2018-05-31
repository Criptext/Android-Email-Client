package com.email.scenes.composer

import com.email.R
import com.email.db.models.Contact
import com.email.scenes.ActivityMessage
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.params.MailboxParams
import com.email.utils.UIMessage
import io.mockk.*
import org.junit.Before
import org.junit.Test

class ComposerControllerDataSourceEventsTest: ComposerControllerTest() {
    private lateinit var listenerSlot: CapturingSlot<(ComposerResult) -> Unit>

    @Before
    override fun setUp() {
        super.setUp()
        listenerSlot = CapturingSlot()
        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs
    }

    private fun runAfterSuccessfullyClickingSendButton(fn: () -> Unit) {
        controller.onStart(null)
        clickSendButton()
        fn()
    }

    private fun simulateMailSaveEvent(result: ComposerResult.SaveEmail) {
        listenerSlot.captured(result)
    }

    private val mockedComposerInputData =
        ComposerInputData(
            to = listOf(Contact(id = 0, email = "mayer@jigl.com", name = "Mayer Mizrachi")),
                cc = emptyList(), bcc = emptyList(), subject = "test email",
                body = "this is a test")

    @Test
    fun `after receiving ack of mail saved without errors, should exit to mailbox scene with SendMail param`() {
        runAfterSuccessfullyClickingSendButton {
            clearMocks(host)

            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(emailId = 1, threadId = "1:1",
                    onlySave = false, composerInputData = mockedComposerInputData))

            val sendMailMessageWithExpectedEmailId: (ActivityMessage?) -> Boolean = {
                (it as ActivityMessage.SendMail).emailId == 1L
            }

            verify { host.exitToScene(MailboxParams(), match(sendMailMessageWithExpectedEmailId) ) }

        }
    }

    @Test
    fun `after receiving ack of only for save mail saved without errors, should finish the scene`() {
        runAfterSuccessfullyClickingSendButton {
            clearMocks(host)

            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(emailId = 1, threadId = "1:1",
                    onlySave = true, composerInputData = mockedComposerInputData))

            verify { host.finishScene() }
        }
    }

    @Test
    fun `after receiving ack of failed to save mail, should show an error message`() {
        runAfterSuccessfullyClickingSendButton {
            clearMocks(host)

            simulateMailSaveEvent(ComposerResult.SaveEmail.Failure())

            verify { scene.showError(UIMessage(R.string.error_saving_as_draft)) }
        }
    }
}