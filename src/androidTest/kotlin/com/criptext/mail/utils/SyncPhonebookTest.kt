package com.criptext.mail.utils

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.db.models.Label
import com.criptext.mail.mocks.MockEmailData
import com.criptext.mail.utils.generaldatasource.data.GeneralResult
import com.criptext.mail.utils.generaldatasource.workers.SyncPhonebookWorker
import io.mockk.mockk
import org.amshove.kluent.shouldBeGreaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncPhonebookTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())

        MockEmailData.insertEmailsNeededForTests(db, listOf(Label.defaultItems.inbox))
    }

    //This test assumes the phone has at least 1 contact with an email registered in it's phonebook.
    @Test
    fun should_load_contacts_from_phone_and_add_them_to_db(){


        val worker = newWorker()

        worker.work(mockk()) as GeneralResult.SyncPhonebook.Success

        val contacts = db.contactDao().getAll()

         contacts.size shouldBeGreaterThan 0

    }

    private fun newWorker(): SyncPhonebookWorker =

            SyncPhonebookWorker(
                    contactDao = db.contactDao(),
                    contentResolver = mActivityRule.activity.contentResolver,
                    publishFn = {})

}