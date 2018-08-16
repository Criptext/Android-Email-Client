package com.criptext.mail.scenes.settings.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateCustomLabelWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var settingsLocalDB: SettingsLocalDB
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "")

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        settingsLocalDB = SettingsLocalDB.Default(db)
    }

    @Test
    fun test_should_create_a_custom_label(){

        val labelName = "__LABEL__"
        val worker = newWorker(labelName)
        val result = worker.work(mockk()) as SettingsResult.CreateCustomLabel.Success

        result.label.text shouldEqual labelName

        db.labelDao().get(labelName) shouldNotBe null
    }

    private fun newWorker(labelName: String): CreateCustomLabelWorker =
            CreateCustomLabelWorker(
                    labelName = labelName,
                    settingsLocalDB = settingsLocalDB,
                    activeAccount = activeAccount,
                    httpClient = mockk(),
                    publishFn = {})

}