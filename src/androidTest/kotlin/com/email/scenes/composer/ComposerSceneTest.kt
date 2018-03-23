package com.email.scenes.composer

import android.support.test.espresso.Espresso.onIdle
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import com.email.R
import com.email.api.ApiCall
import com.email.db.KeyValueStorage
import com.email.db.TestDatabase
import com.email.signal.InDBUser
import com.email.signal.InMemoryUser
import com.email.signal.SignalKeyGenerator
import com.email.signal.TestUser
import com.email.utils.TestActivity
import com.email.utils.enqueueSuccessfulResponses
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBe
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by gabriel on 3/21/18.
 */

class ComposerSceneTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)
    private val keyGenerator = SignalKeyGenerator.Default()
    private val server = MockWebServer()
    private lateinit var db: TestDatabase
    private lateinit var storage: KeyValueStorage
    private lateinit var controller: ComposerController
    private lateinit var model: ComposerModel

    private lateinit var currentUser: TestUser

    init {
        ApiCall.baseUrl = server.url("v1/mock").toString()
    }


    @Before
    fun setup() {
        val activity = mActivityRule.activity

        db = TestDatabase.getAppDatabase(activity)
        db.resetDao().deleteAllData(1)

        storage = KeyValueStorage.SharedPrefs(activity)
        currentUser = InDBUser(db, storage, keyGenerator, "gabriel", 1).setup()

        activity.setLayoutOnUiThread(R.layout.activity_composer)

        onIdle()

        model = ComposerModel()
        controller = ComposerActivity.initController(db, activity, activity, model)
        activity.controller = controller
    }

    @Test
    fun should_send_new_email_to_server_without_issues_and_close_the_composer() {
        val recipientUser = InMemoryUser(keyGenerator, "mayer", 1)
        val recipientKeyBundle = recipientUser.fetchAPreKeyBundle().toJSON().toString()

        server.enqueueSuccessfulResponses(listOf("[$recipientKeyBundle]", "OK"))

        mActivityRule.activity.startOnUiThread()

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

        // should be finishing if all went ok
        mActivityRule.activity.isFinishing `shouldBe` true
    }

    @Test
    fun should_show_error_if_server_could_not_post_new_email() {

        val recipientUser = InMemoryUser(keyGenerator, "mayer", 1)
        val recipientKeyBundle = recipientUser.fetchAPreKeyBundle().toJSON().toString()

        server.enqueueSuccessfulResponses(listOf("[$recipientKeyBundle]"))
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))


        mActivityRule.activity.startOnUiThread()

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

        mActivityRule.activity.isFinishing `shouldBe` false
    }
}