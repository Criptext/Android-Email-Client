package com.email.scenes.composer

import com.email.R
import com.email.bgworker.AsyncTaskWorkRunner
import com.email.db.*
import com.email.db.dao.*
import com.email.db.models.*
import com.email.mocks.MockedIHostActivity
import com.email.scenes.composer.data.ComposerAPIClient
import com.email.scenes.composer.data.ComposerDataSource
import com.email.scenes.composer.data.ComposerResult
import com.email.scenes.composer.mocks.*
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

/**
 * Created by danieltigse on 4/19/18.
 */

@RunWith(RobolectricTestRunner::class)
class ComposerSceneControllerSendTest {

    private lateinit var scene: MockedComposerScene
    private lateinit var model: ComposerModel
    private lateinit var controller: ComposerController
    private lateinit var dataSource: ComposerDataSource
    private lateinit var host: MockedIHostActivity

    @Before
    fun createComposerSceneController() {
        scene = MockedComposerScene()
        host = MockedIHostActivity()
        model = ComposerModel(fullEmail = null, composerType = null)

        val db = ComposerLocalDB(contactDao = MockedContactDao(), labelDao = MockedLabelDao(),
                emailDao = MockedEmailDao(), emailLabelDao = MockedEmailLabelDao(),
                emailContactDao = MockedEmailContactJoinDao(), accountDao = MockedAccountDao())

        dataSource = ComposerDataSource(db, AsyncTaskWorkRunner())
        controller = ComposerController(model, scene, host, dataSource)
    }

    private fun clickSendButton() {
        controller.onOptionsItemSelected(R.id.composer_send)
    }

    @Test
    fun `On send btn clicked, should save mail with no errors if mail has recipients`() {

        val data = scene.getDataInputByUser()
        controller.updateModelWithInputData(data)
        controller.onStart(null)
        clickSendButton()
        scene.showedError `should be` false
    }

    private fun runAfterSuccessfullyClickingSendButton(fn: () -> Unit) {
        val data = scene.getDataInputByUser()
        controller.updateModelWithInputData(data)
        controller.onStart(null)
        clickSendButton()
        fn()
    }

    private fun simulateMailSaveEvent(result: ComposerResult.SaveEmail) {
        controller.onEmailSavesAsDraft(result)
    }

    @Test
    fun `after receiving ack of mail saved without errors, should exit scene and let MailBox send the email`() {
        runAfterSuccessfullyClickingSendButton {

            //simulate ack without errors
            simulateMailSaveEvent(ComposerResult.SaveEmail.Success(1, false))

            host.isFinished `should be` true

        }
    }

    @Test
    fun `after receiving ack of failed to save mail, should show an error message`() {
        runAfterSuccessfullyClickingSendButton {

            scene.showedError `should be` false

            //simulate send operation failed
            simulateMailSaveEvent(ComposerResult.SaveEmail.Failure())

            scene.showedError `should be` true
        }
    }
}