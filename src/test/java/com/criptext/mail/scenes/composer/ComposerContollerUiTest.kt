package com.criptext.mail.scenes.composer

import android.Manifest
import com.criptext.mail.BaseActivity
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.ActivityMessage
import com.criptext.mail.scenes.composer.data.*
import io.mockk.every
import io.mockk.verify
import org.amshove.kluent.`should be equal to`
import org.junit.Before
import org.junit.Test

/**
 * Created by danieltigse on 4/19/18.
 */
class ComposerContollerUiTest: ComposerControllerTest() {

    @Before
    override fun setUp() {
        super.setUp()
    }


    @Test
    fun `On send btn clicked, should save mail with no errors if mail has recipients`() {
        val inputData = ComposerInputData(
                to = listOf(Contact(id = 0, name = "Tester", email = "tester@jigl.com", isTrusted = true,
                        score = 0, spamScore = 0)),
                cc = emptyList(), bcc = emptyList(), subject = "", body = "", attachments = null, fileKey = null)
        every { scene.getDataInputByUser() } returns inputData


        controller.onStart(null)
        clickSendButton()

        verify(inverse = true) { scene.showError(any()) }
    }

    @Test
    fun `On send btn clicked, should show error if email has no recipients`() {
        val inputData = ComposerInputData(
                to = emptyList(), cc = emptyList(), bcc = emptyList(), subject = "", body = "",
                fileKey = null, attachments = null)

        every { scene.getDataInputByUser() } returns inputData


        controller.onStart(null)
        clickSendButton()

        verify { scene.showError(any()) }
    }

    @Test
    fun `On start if there is an AttachmentActivityMessage, attachment should be added to composer`(){
        every { host.checkPermissions(BaseActivity.RequestCode.writeAccess.ordinal,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) } returns true
        val activityMessage = ActivityMessage.AddAttachments(filesMetadata = listOf(Pair("/test.pdf", 46332L)), isShare = false)


        controller.onStart(activityMessage)

        model.attachments.size `should be equal to` 1
        verify { scene.notifyAttachmentSetChanged() }
    }





}