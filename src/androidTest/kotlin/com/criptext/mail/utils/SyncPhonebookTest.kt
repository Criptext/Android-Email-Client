package com.criptext.mail.utils

import android.Manifest
import android.content.res.Resources
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.workers.SyncPhonebookWorker
import io.mockk.mockk
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncPhonebookTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)
    @get:Rule
    var mRuntimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    private lateinit var db: TestDatabase
    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1)

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
    }

    //This test assumes the phone has at least 1 contact with an email registered in it's phonebook.
    @Test
    fun should_load_contacts_from_phone_and_add_them_to_db(){


        val worker = newWorker()

        val result = worker.work(mockk())

        if(result is GeneralResult.SyncPhonebook.Success) {

            val contacts = db.contactDao().getAll()

            contacts.size shouldBeGreaterThan 0
        }else if(result is GeneralResult.SyncPhonebook.Failure){
            result.exception.shouldBeInstanceOf(Resources.NotFoundException::class.java)
        }

    }

    private fun newWorker(): SyncPhonebookWorker =

            SyncPhonebookWorker(
                    contactDao = db.contactDao(),
                    contentResolver = mActivityRule.activity.contentResolver,
                    activeAccount = activeAccount,
                    publishFn = {})

}