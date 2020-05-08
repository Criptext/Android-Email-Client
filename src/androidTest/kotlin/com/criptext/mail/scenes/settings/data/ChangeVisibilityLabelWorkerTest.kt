package com.criptext.mail.scenes.settings.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.db.AccountTypes
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.db.SettingsLocalDB
import com.criptext.mail.db.models.Account
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.settings.labels.workers.ChangeVisibilityLabelWorker
import io.mockk.mockk
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
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1,
            domain = Contact.mainDomain, type = AccountTypes.STANDARD)

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(id = 1, recipientId = "tester", deviceId = 1,
                name = "Tester", registrationId = 1,
                identityKeyPairB64 = "_IDENTITY_", jwt = "__JWTOKEN__",
                signature = "", refreshToken = "__REFRESH__", isActive = true, domain = "criptext.com", isLoggedIn = true,
                backupPassword = null, autoBackupFrequency = 0, hasCloudBackup = false, wifiOnly = true, lastTimeBackup = null,
                type = AccountTypes.STANDARD, blockRemoteContent = false))
        settingsLocalDB = SettingsLocalDB.Default(db)

        labelId = db.labelDao().insert(Label(
                id = 0,
                text = labelName,
                type = LabelTypes.CUSTOM,
                visible = true,
                color = "000000",
                uuid = "uuid",
                accountId = activeAccount.id
        ))

    }

    @Test
    fun test_should_set_label_visibility_as_true(){

        val worker = newWorker(labelId, true)
        worker.work(mockk())

        val labelInserted = db.labelDao().get(labelName, activeAccount.id)
        labelInserted.visible shouldEqual true

    }

    @Test
    fun test_should_set_label_visibility_as_false(){

        val worker = newWorker(labelId, false)
        worker.work(mockk())

        val labelInserted = db.labelDao().get(labelName, activeAccount.id)
        labelInserted.visible shouldEqual false

    }

    private fun newWorker(labelId: Long, isVisible: Boolean): ChangeVisibilityLabelWorker =
            ChangeVisibilityLabelWorker(
                    labelId = labelId,
                    isVisible = isVisible,
                    db = settingsLocalDB,
                    activeAccount = activeAccount,
                    publishFn = {})

}