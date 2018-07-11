package com.email.scenes.composer.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.api.models.EmailMetadata
import com.email.db.DeliveryTypes
import com.email.db.SearchLocalDB
import com.email.db.models.*
import com.email.scenes.mailbox.data.*
import com.email.scenes.search.data.SearchEmailWorker
import com.email.scenes.search.data.SearchResult
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchEmailWorkerTest{

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var searchLocalDB: SearchLocalDB

    private val userEmail = "gabriel@jigl.com"

    private fun createMetadataColumns(id: Int, fromContact: Contact): EmailMetadata.DBColumns {
        val seconds = if (id < 10) "0$id" else id.toString()
       return EmailMetadata.DBColumns(to = "gabriel@jigl.com",  cc = "", bcc = "",
                    fromContact = fromContact, messageId = "gabriel/1/$id",
                    date = "2018-02-21 14:00:$seconds", threadId = "thread#$id",
                    subject = "Test #$id", unread = true, metadataKey = id + 100L,
                    status = DeliveryTypes.NONE)
    }
    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        searchLocalDB = SearchLocalDB.Default(db)

        (1..2).forEach {
            val fromContact = Contact(1,"mayer@jigl.com", "Mayer Mizrachi")
            val metadata = createMetadataColumns(it, fromContact)
            val decryptedBody = "Hello, this is message #$it"
            val labels = listOf(Label.defaultItems.inbox)
            EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                    decryptedBody = decryptedBody, labels = labels, files = emptyList())
        }

        val anotherFromContact = Contact(2,"erika@jigl.com", "Erika Perugachi")
        val metadata = createMetadataColumns(3, anotherFromContact)
        val decryptedBody = "Hello again, this is message #3"
        val labels = listOf(Label.defaultItems.inbox)
        EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                decryptedBody = decryptedBody, labels = labels, files = emptyList())
    }

    @Test
    fun test_should_found_two_emails_based_on_sender_email_address(){

        val worker = newWorker(
                queryText = "mayer@jigl.com",
                loadParams = LoadParams.Reset(20)
        )

        val result = worker.work(mockk()) as SearchResult.SearchEmails.Success

        result.emailThreads.size shouldBe 2
    }

    private fun newWorker(queryText: String, loadParams: LoadParams): SearchEmailWorker =
            SearchEmailWorker(
                    db = searchLocalDB,
                    queryText = queryText,
                    loadParams = loadParams,
                    userEmail = userEmail,
                    publishFn = {})

}