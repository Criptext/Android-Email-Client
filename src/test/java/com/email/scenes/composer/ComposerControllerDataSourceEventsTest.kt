package com.email.scenes.composer

import com.email.R
import com.email.db.models.Contact
import com.email.scenes.ActivityMessage
import com.email.scenes.composer.data.ComposerInputData
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.params.MailboxParams
import com.email.utils.UIMessage
import io.mockk.*
import org.amshove.kluent.`should be`
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

    private fun runAfterSelectingAnAttachment(fn: () -> Unit) {
        controller.onStart(mockedAttachmentActivityMessage)
        fn()
    }

    private fun simulateMailSaveEvent(result: ComposerResult.SaveEmail) {
        listenerSlot.captured(result)
    }

    private fun simulateAddAttachmentEvent(result: ComposerResult.UploadFile) {
        listenerSlot.captured(result)
    }

    private val mockedComposerInputData =
        ComposerInputData(
            to = listOf(Contact(id = 0, email = "mayer@jigl.com", name = "Mayer Mizrachi")),
                cc = emptyList(), bcc = emptyList(), subject = "test email",
                body = "this is a test")

    private val mockedAttachmentActivityMessage =
            ActivityMessage.AddAttachments(filesMetadata = listOf(Pair("/test.pdf", 46332L)))

    @Test
    fun `after receiving ack of mail saved without errors, should exit to mailbox scene with SendMail param`() {
        runAfterSuccessfullyClickingSendButton {
            clearMocks(host)

            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(emailId = 1, threadId = "1:1",
                    onlySave = false, composerInputData = mockedComposerInputData, attachments = emptyList()))

            val sendMailMessageWithExpectedEmailId: (ActivityMessage?) -> Boolean = {
                (it as ActivityMessage.SendMail).emailId == 1L
            }

            verify { host.exitToScene(MailboxParams(), match(sendMailMessageWithExpectedEmailId), false ) }

        }
    }

    @Test
    fun `after receiving ack of only for save mail saved without errors, should finish the scene`() {
        runAfterSuccessfullyClickingSendButton {
            clearMocks(host)

            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(emailId = 1, threadId = "1:1",
                    onlySave = true, composerInputData = mockedComposerInputData, attachments = emptyList()))

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

    @Test
    fun `after receiving ack of registered file, should update token value of ComposerAttachment`() {
        val mockedFiletoken = "rbesfgfgdsfdgbs"
        runAfterSelectingAnAttachment {
            clearMocks(host)
            simulateAddAttachmentEvent(ComposerResult.UploadFile.Register(filepath = "/test.pdf",
                    filetoken = mockedFiletoken))

            model.attachments[0].filetoken `should be` mockedFiletoken
            verify { scene.notifyAttachmentSetChanged() }
        }
    }

    @Test
    fun `after receiving ack of success file, the file should be at 100% progress`() {
        runAfterSelectingAnAttachment {
            clearMocks(host)
            simulateAddAttachmentEvent(ComposerResult.UploadFile.Success(filepath = "/test.pdf"))

            model.attachments[0].uploadProgress `should be` 100
            verify { scene.notifyAttachmentSetChanged() }
        }
    }

    @Test
    fun `after receiving ack of failed file, should remove the file and show error message`() {
        runAfterSelectingAnAttachment {
            clearMocks(host)
            simulateAddAttachmentEvent(ComposerResult.UploadFile.Failure(filepath = "/test.pdf",
                    message = UIMessage(R.string.network_error_exception)))

            model.attachments.size `should be` 0
            verify { scene.showAttachmentErrorDialog("/test.pdf") }
        }
    }
}