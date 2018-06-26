package com.email.scenes.composer.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.api.models.EmailMetadata
import com.email.db.MailFolders
import com.email.db.MailboxLocalDB
import com.email.db.SearchLocalDB
import com.email.db.models.*
import com.email.scenes.mailbox.data.*
import com.email.scenes.search.data.SearchEmailWorker
import com.email.scenes.search.data.SearchResult
import io.mockk.mockk
import org.amshove.kluent.mock
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

    private val queryText = "mayer@jigl.com"
    private val userEmail = "gabriel@jigl.com"

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        searchLocalDB = SearchLocalDB.Default(db)

        val fromContact = Contact(1,"mayer@jigl.com", "Mayer Mizrachi")
        (1..2).forEach {
            val seconds = if (it < 10) "0$it" else it.toString()
            val metadata = EmailMetadata.DBColumns(to = "gabriel@jigl.com",  cc = "", bcc = "",
                    fromContact = fromContact, messageId = "gabriel/1/$it",
                    date = "2018-02-21 14:00:$seconds", threadId = "thread#$it",
                    subject = "Test #$it", unread = true)
            val decryptedBody = "Hello, this is message #$it"
            val labels = listOf(Label.defaultItems.inbox)
            EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                    decryptedBody = decryptedBody, labels = labels, files = emptyList())
        }

        val anotherFromContact = Contact(2,"erika@jigl.com", "Erika Perugachi")
        val metadata = EmailMetadata.DBColumns(to = "gabriel@jigl.com",  cc = "", bcc = "",
                fromContact = anotherFromContact, messageId = "gabriel/2/3",
                date = "2018-02-21 14:00:00", threadId = "thread#3",
                subject = "Another Test #3", unread = true)
        val decryptedBody = "Hello again, this is message #3"
        val labels = listOf(Label.defaultItems.inbox)
        EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                decryptedBody = decryptedBody, labels = labels, files = emptyList())
    }

    @Test
    fun test_should_found_two_emails_based_on_query_text(){

        val worker = newWorker(
                queryText = queryText,
                loadParams = LoadParams.Reset(20)
        )

        worker.work(mockk()) as SearchResult.SearchEmails.Success

        searchLocalDB.searchMailsInDB(
                queryText = queryText,
                oldestEmailThread = null,
                limit = 20,
                userEmail = userEmail
        ).size shouldBe 2

    }

    private fun newWorker(queryText: String, loadParams: LoadParams): SearchEmailWorker =
            SearchEmailWorker(
                    db = searchLocalDB,
                    queryText = queryText,
                    loadParams = loadParams,
                    userEmail = userEmail,
                    publishFn = {})

}