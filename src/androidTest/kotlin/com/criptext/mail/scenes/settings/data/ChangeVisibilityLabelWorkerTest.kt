package com.criptext.mail.scenes.settings.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.Label
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangeVisibilityLabelWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var settingsLocalDB: SettingsLocalDB

    private val labelName = "Cute Dogs"
    private var labelId: Long = 0

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        settingsLocalDB = SettingsLocalDB(db.labelDao(), db.accountDao(), db.contactDao())

        labelId = db.labelDao().insert(Label(
                id = 0,
                text = labelName,
                type = LabelTypes.CUSTOM,
                visible = true,
                color = "000000"
        ))

    }

    @Test
    fun test_should_set_label_visibility_as_true(){

        val worker = newWorker(labelId, true)
        worker.work(mockk())

        val labelInserted = db.labelDao().get(labelName)
        labelInserted.visible shouldEqual true

    }

    @Test
    fun test_should_set_label_visibility_as_false(){

        val worker = newWorker(labelId, false)
        worker.work(mockk())

        val labelInserted = db.labelDao().get(labelName)
        labelInserted.visible shouldEqual false

    }

    private fun newWorker(labelId: Long, isVisible: Boolean): ChangeVisibilityLabelWorker =
            ChangeVisibilityLabelWorker(
                    labelId = labelId,
                    isVisible = isVisible,
                    db = settingsLocalDB,
                    publishFn = {})

}