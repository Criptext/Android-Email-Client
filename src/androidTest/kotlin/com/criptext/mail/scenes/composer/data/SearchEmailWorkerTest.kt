package com.criptext.mail.scenes.composer.data

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.api.models.EmailMetadata
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.SearchLocalDB
import com.criptext.mail.db.models.*
import com.criptext.mail.scenes.mailbox.data.*
import com.criptext.mail.scenes.search.data.SearchEmailWorker
import com.criptext.mail.scenes.search.data.SearchResult
import com.criptext.mail.utils.EmailUtils
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

    private val activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "")

    private fun createMetadataColumns(id: Int, fromContact: Contact): EmailMetadata.DBColumns {
        val seconds = if (id < 10) "0$id" else id.toString()
       return EmailMetadata.DBColumns(to = listOf("gabriel@criptext.com"),  cc = emptyList(), bcc = emptyList(),
                    fromContact = fromContact, messageId = "gabriel/1/$id",
                    date = "2018-02-21 14:00:$seconds", threadId = "thread#$id",
                    subject = "Test #$id", unread = true, metadataKey = id + 100L,
                    status = DeliveryTypes.NONE, unsentDate = "2018-02-21 14:00:$seconds", secure = true,
                    trashDate = "2018-02-21 14:00:$seconds", replyTo = null, boundary = null)
    }
    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
        db.accountDao().insert(Account(activeAccount.recipientId, activeAccount.deviceId,
                activeAccount.name, activeAccount.jwt, activeAccount.refreshToken,
                "_KEY_PAIR_", 0, ""))
        searchLocalDB = SearchLocalDB.Default(db, mActivityRule.activity.filesDir)

        (1..2).forEach {
            val fromContact = Contact(1,"mayer@criptext.com", "Mayer Mizrachi", isTrusted = false, score = 0)
            val metadata = createMetadataColumns(it, fromContact)
            val decryptedBody = "Hello, this is message #$it"
            val labels = listOf(Label.defaultItems.inbox)
            EmailUtils.saveEmailInFileSystem(
                    filesDir = mActivityRule.activity.filesDir,
                    recipientId = activeAccount.recipientId,
                    metadataKey = metadata.metadataKey,
                    content = decryptedBody,
                    headers = null)
            EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                    preview = decryptedBody, labels = labels, files = emptyList(), fileKey = null)
        }

        val anotherFromContact = Contact(2,"erika@criptext.com", "Erika Perugachi", isTrusted = false, score = 0)
        val metadata = createMetadataColumns(3, anotherFromContact)
        val decryptedBody = "Hello again, this is message #3"
        val labels = listOf(Label.defaultItems.inbox)
        EmailUtils.saveEmailInFileSystem(
                filesDir = mActivityRule.activity.filesDir,
                recipientId = activeAccount.recipientId,
                metadataKey = metadata.metadataKey,
                content = decryptedBody,
                headers = null)
        EmailInsertionSetup.exec(dao = db.emailInsertionDao(), metadataColumns = metadata,
                preview = decryptedBody, labels = labels, files = emptyList(), fileKey = null)
    }

    @Test
    fun test_should_found_two_emails_based_on_sender_email_address(){

        val worker = newWorker(
                queryText = "mayer@criptext.com",
                loadParams = LoadParams.Reset(20)
        )

        val result = worker.work(mockk()) as SearchResult.SearchEmails.Success

        result.emailThreads.size shouldBe 2
    }

    @Test
    fun test_should_found_one_email_based_on_subject(){

        val worker = newWorker(
                queryText = "Test #1",
                loadParams = LoadParams.Reset(20)
        )

        val result = worker.work(mockk()) as SearchResult.SearchEmails.Success

        result.emailThreads.size shouldBe 1
    }

    @Test
    fun test_should_found_one_email_based_on_preview(){

        val worker = newWorker(
                queryText = "Hello, this is message #1",
                loadParams = LoadParams.Reset(20)
        )

        val result = worker.work(mockk()) as SearchResult.SearchEmails.Success

        result.emailThreads.size shouldBe 1
    }

    @Test
    fun test_should_found_three_emails_based_on_to_contact(){

        val worker = newWorker(
                queryText = "gabriel@criptext.com",
                loadParams = LoadParams.Reset(20)
        )

        val result = worker.work(mockk()) as SearchResult.SearchEmails.Success

        result.emailThreads.size shouldBe 3
    }

    private fun newWorker(queryText: String, loadParams: LoadParams): SearchEmailWorker =
            SearchEmailWorker(
                    db = searchLocalDB,
                    queryText = queryText,
                    loadParams = loadParams,
                    userEmail = activeAccount.userEmail,
                    publishFn = {})

}