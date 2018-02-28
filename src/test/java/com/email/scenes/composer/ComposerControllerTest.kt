package com.email.scenes.composer

import com.email.DB.models.Contact
import com.email.R
import com.email.mocks.MockedWorkRunner
import com.email.scenes.composer.data.ComposerDataSource
import com.email.scenes.composer.data.SendMailWorker
import com.email.scenes.composer.mocks.MockedComposerScene
import com.email.scenes.composer.ui.UIData
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 2/27/18.
 */

class ComposerControllerTest {

    private lateinit var model: ComposerModel
    private lateinit var scene: MockedComposerScene
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: ComposerDataSource
    private lateinit var controller: ComposerController


    @Before
    fun setUp() {
        model = ComposerModel()
        scene = MockedComposerScene()
        runner = MockedWorkRunner()
        dataSource = ComposerDataSource(runner)
        controller = ComposerController(model, scene, dataSource)
    }

    @Test
    fun `should set uiObserver on start and clear it on stop`() {
        controller.onStart()

        scene.observer `should not be` null

        controller.onStop()

        scene.observer `should be` null
    }

    @Test
    fun `should bind the view with the model on start and update the model with input data on stop`() {
        model.subject = "New email"
        model.body = "some test"

        controller.onStart()

        val displayedData = scene.displayedData

        displayedData!!.subject `should equal` model.subject
        displayedData.body `should equal` model.body

        // simulate user input new data
        val newDisplayedData = UIData(to = emptyList(), cc = emptyList(), bcc = emptyList(),
                subject = "Updated subject", body ="i wrote some changes")
        scene.displayedData = newDisplayedData

        controller.onStop()

        model.subject `should equal` newDisplayedData.subject
        model.body `should equal` newDisplayedData.body
    }

    @Test
    fun `if user has not send a recipient, should show error after clicking send button`() {
        controller.onStart()

        controller.onOptionsItemSelected(R.id.composer_send)

        scene.lastError!!.resId `should equal` R.string.no_recipients_error
        runner.assertPendingWork(emptyList())
    }


    @Test
    fun `if user has set an invalid recipient, should show error after clicking send button`() {
        controller.onStart()

        // simulate user input wrong address
        scene.displayedData = UIData(
                to = listOf(Contact.Invalid(email="email", name="gabriel")),
                cc = emptyList(), bcc = emptyList(),
                subject = "", body = "")
        scene.observer!!.onRecipientListChanged()

        controller.onOptionsItemSelected(R.id.composer_send)

        scene.lastError!!.resId `should equal` R.string.invalid_address_error
        runner.assertPendingWork(emptyList())
    }

    @Test
    fun `if user has set a valid recipient, should try to send mail`() {
        controller.onStart()

        // simulate user input wrong address
        scene.displayedData = UIData(
                to = listOf(Contact(email="bob@domain.com", name="gabriel")),
                cc = emptyList(), bcc = emptyList(),
                subject = "", body = "")
        scene.observer!!.onRecipientListChanged()

        controller.onOptionsItemSelected(R.id.composer_send)

        scene.lastError `should be` null
        runner.assertPendingWork(listOf(SendMailWorker::class.java))
    }

}