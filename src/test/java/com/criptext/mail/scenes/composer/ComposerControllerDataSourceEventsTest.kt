package com.criptext.mail.scenes.composer

import android.Manifest
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.db.models.Contact
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.composer.data.ComposerInputData
import com.criptext.mail.scenes.composer.data.ComposerResult
import com.criptext.mail.scenes.params.MailboxParams
import com.criptext.mail.utils.UIMessage
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
            to = listOf(Contact(id = 0, email = "mayer@jigl.com", name = "Mayer Mizrachi", isTrusted = true,
                    score = 0, spamScore = 0)),
                cc = emptyList(), bcc = emptyList(), subject = "test email",
                body = "this is a test", attachments = null, fileKey = null, fromAddress = "")

    private val mockedAttachmentActivityMessage =
            ActivityMessage.AddAttachments(filesMetadata = listOf(Pair("/test.pdf", 46332L)), isShare = false)

    @Test
    fun `after receiving ack of mail saved without errors, should exit to mailbox scene with SendMail param`() {
        runAfterSuccessfullyClickingSendButton {
            clearMocks(host)

            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(emailId = 1, threadId = "1:1",
                    onlySave = false, composerInputData = mockedComposerInputData, attachments = emptyList(),
                    fileKey = null, preview = null, senderAddress = activeAccount.userEmail, account = activeAccount))

            val sendMailMessageWithExpectedEmailId: (ActivityMessage?) -> Boolean = {
                (it as ActivityMessage.SendMail).emailId == 1L
            }

            verify { host.goToScene(MailboxParams(), activityMessage = match(sendMailMessageWithExpectedEmailId), keep = false) }

        }
    }

    @Test
    fun `after receiving ack of only for save mail saved without errors, should finish the scene`() {
        runAfterSuccessfullyClickingSendButton {
            clearMocks(host)

            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(emailId = 1, threadId = "1:1",
                    onlySave = true, composerInputData = mockedComposerInputData, attachments = emptyList(),
                    fileKey = null, preview = null, senderAddress = activeAccount.userEmail, account = activeAccount))

            verify { host.goToScene(params = any(), activityMessage = any(), keep = false, animationData = any()) }
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
        every { host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) } returns true
        runAfterSelectingAnAttachment {
            clearMocks(host)
            simulateAddAttachmentEvent(ComposerResult.UploadFile.Register(filepath = "/test.pdf",
                    filetoken = mockedFiletoken, uuid = model.attachments[0].uuid, groupId = null))

            model.attachments[0].filetoken `should be` mockedFiletoken
            verify { scene.notifyAttachmentSetChanged() }
        }
    }

    @Test
    fun `after receiving ack of success file, the file should be at 100% progress`() {
        every { host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) } returns true
        runAfterSelectingAnAttachment {
            clearMocks(host)
            simulateAddAttachmentEvent(ComposerResult.UploadFile.Success(filepath = "/test.pdf", filesSize = 0L, uuid = model.attachments[0].uuid))

            model.attachments[0].uploadProgress `should be` 100
            verify { scene.notifyAttachmentSetChanged() }
        }
    }

    @Test
    fun `after receiving ack of failed file, should remove the file and show error message`() {
        every { host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) } returns true
        runAfterSelectingAnAttachment {
            clearMocks(host)
            val errorMessage = UIMessage(R.string.unable_to_upload, arrayOf("/test.pdf"))
            simulateAddAttachmentEvent(ComposerResult.UploadFile.Failure(filepath = "/test.pdf",
                    message = errorMessage, uuid = model.attachments[0].uuid))

            model.attachments.size `should be` 0
            verify { scene.showAttachmentErrorDialog(errorMessage) }
        }
    }
}