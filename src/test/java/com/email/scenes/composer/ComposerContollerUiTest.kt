package com.email.scenes.composer

import com.email.db.models.*
import com.email.scenes.composer.data.*
import io.mockk.every
import io.mockk.verify
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
                to = listOf(Contact(id = 0, name = "Tester", email = "tester@jigl.com")),
                cc = emptyList(), bcc = emptyList(), subject = "", body = "")
        every { scene.getDataInputByUser() } returns inputData


        controller.onStart(null)
        clickSendButton()

        verify(inverse = true) { scene.showError(any()) }
    }

    @Test
    fun `On send btn clicked, should show error if email has no recipients`() {
        val inputData = ComposerInputData(
                to = emptyList(), cc = emptyList(), bcc = emptyList(), subject = "", body = "")

        every { scene.getDataInputByUser() } returns inputData


        controller.onStart(null)
        clickSendButton()

        verify { scene.showError(any()) }
    }





}