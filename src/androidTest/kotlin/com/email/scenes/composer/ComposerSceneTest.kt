package com.email.scenes.composer

import android.support.test.espresso.Espresso.onIdle
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.matcher.ViewMatchers.*
import com.email.scenes.mailbox.MailboxActivity
import com.email.R
import com.email.api.ApiCall
import com.email.signal.InMemoryUser
import com.email.utils.*
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Rule
import org.junit.Test

/**
 * Created by gabriel on 3/21/18.
 */

class ComposerSceneTest {

    private val server = MockWebServer()


    init {
        ApiCall.baseUrl = server.url("v1/mock").toString()
    }

    @get:Rule
    val mActivityRule = ActivityWithActiveUserTestRule(ComposerActivity::class.java, { ComposerModel() })

    @Test
    fun should_write_new_email_without_issues_and_switch_to_mailbox() {
        val act = mActivityRule.activity
        val controller = act.controller

        // create recipient in memory and mock keybundle server response
        val recipientUser = InMemoryUser(mActivityRule.keyGenerator, "mayer", 1)
        val recipientKeyBundle = recipientUser.fetchAPreKeyBundle().toJSON().toString()

        // [POST /keybundle res, POST /email res]
        server.enqueueSuccessfulResponses(listOf("[$recipientKeyBundle]", "OK"))

        // input email data
        onView(withId(ComposerScene.INPUT_BODY_ID))
                .perform(typeText("Hello this is a test."))
        onView(withId(ComposerScene.INPUT_TO_ID))
                .perform(typeText("mayer@jigl.com"))
        onView(withId(ComposerScene.INPUT_SUBJECT_ID))
                .perform(typeText("test email"))

        // press send
        controller.onOptionsItemSelected(R.id.composer_send)
        onIdle()

        // should be finishing composer and launching mailbox if all went ok
        act.isFinishing shouldBe true
        didLaunchActivity(MailboxActivity::class.java)

        // wait for activity transition
        Thread.sleep(500)

        // assert that mail was correctly encrypted and sent to server
        server.assertSentRequests(listOf(
            ExpectedRequest(needsJwt = true, method = "POST",  path = "/keybundle",
            assertBodyFn = { json ->
                val recipients = json.getJSONArray("recipients")
                recipients.length() shouldEqual 1
                recipients[0] shouldEqual "mayer"

                val knownAddresses = json.getJSONObject("knownAddresses")
                knownAddresses.length() shouldEqual 0
            }),
            ExpectedRequest(needsJwt = true, method = "POST",  path = "/email",
            assertBodyFn = { json ->
                val subject = json.getString("subject")
                subject shouldEqual "test email"
                val recipients = json.getJSONArray("criptextEmails")
                recipients.length() shouldEqual 1
            })
        ))
    }
}