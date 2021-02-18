package com.criptext.mail.scenes.mailbox.feed

import com.criptext.mail.IHostActivity
import com.criptext.mail.db.DeliveryTypes
import com.criptext.mail.db.FeedType
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.dao.ContactDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.FeedItemDao
import com.criptext.mail.db.dao.FileDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.FeedItem
import com.criptext.mail.scenes.mailbox.feed.data.*
import com.criptext.mail.scenes.mailbox.feed.ui.FeedItemHolder
import com.criptext.mail.scenes.mailbox.feed.ui.FeedScene
import io.mockk.*
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test

import java.util.*

/**
 * Created by gabriel on 2/21/18.
 */
class FeedControllerTest {

    private lateinit var model: FeedModel
    private lateinit var scene: FeedScene
    private lateinit var db: FeedItemDao
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private lateinit var emailDao: EmailDao
    private lateinit var contactDao: ContactDao
    private lateinit var fileDao: FileDao
    private lateinit var dataSource: FeedDataSource
    private lateinit var controller: FeedController
    protected lateinit var host: IHostActivity
    protected lateinit var activeAccount: ActiveAccount
    protected lateinit var storage: KeyValueStorage
    protected lateinit var sentRequests: MutableList<FeedRequest>
    protected lateinit var listenerSlot: CapturingSlot<(FeedResult) -> Unit>
    private val feedEventListenerSlot = CapturingSlot<FeedItemHolder.FeedEventListener>()

    @Before
    fun setUp() {
        model = FeedModel()
        scene = mockk(relaxed = true)
        db = mockk(relaxed = true)
        mailboxLocalDB = mockk(relaxed = true)
        emailDao = mockk(relaxed = true)
        contactDao = mockk(relaxed = true)
        fileDao = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"John","jwt":"_JWT_","recipientId":"gabriel","deviceId":1,
                    |"signature":""} """.trimMargin())
        controller = FeedController(model, scene, host, activeAccount, storage, dataSource)

        every {
            scene.setAdapter(any(), 0, 0, capture(feedEventListenerSlot))
        } just Runs

        sentRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs

        listenerSlot = CapturingSlot()
        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs
    }

    private val testEmail = Email(
            id = 1, date = Date(), subject = "This is a subject",
            unread = false, threadId = "__THREAD_ID__", content = "Bla",
            delivered = DeliveryTypes.NONE, messageId = "__THREAD_ID__",
            metadataKey = 1, preview = "__PREVIEW__", secure = false, unsentDate = Date(), trashDate = null,
            replyTo = null, fromAddress = "Mayer Mizrachi <mayer@jigl.com>",
            boundary = null, accountId = 1, isNewsletter = null)

    private fun createFeedItems(size: Int): List<ActivityFeedItem> {
        return (1..size).map {
            ActivityFeedItem(
                    feedItem = FeedItem(id = it.toLong(), feedType = FeedType.OPEN_FILE, emailId = 1,
                        contactId = 1, date = Date(), seen = false, location = "", fileId = null),
                    email = testEmail,
                    file = null,
                    contact = Contact(id = 1, email = "daniel@jigl.com", name = "Daniel", isTrusted = true,
                            score = 0))

        }.reversed()
    }

    private fun simulateLoadOfFeeds(size: Int) {
        listenerSlot.captured(FeedResult.LoadFeed.Success(createFeedItems(size), 0))
    }

    @Test
    fun `on a cold start, should try to load feeds from DB`() {

        controller.onStart()

        val request = sentRequests.first()

        request `should be instance of` FeedRequest.LoadFeed::class.java

        simulateLoadOfFeeds(2)

        model.feedItems.size `should be equal to` 2
    }

    @Test
    fun `when the delete button is clicked, on abscene of errors, should delete the row, and update the db`() {
        // init
        controller.onStart()

        simulateLoadOfFeeds(2)

        val deletedItem = model.feedItems[0]

        // fire click event
        scene.feedEventListener?.onDeleteFeedItemClicked(feedId = deletedItem.id, position = 0)

        feedEventListenerSlot.captured.onDeleteFeedItemClicked(deletedItem.id, 0)

        model.feedItems.size `should be equal to` 1

        val request = sentRequests.last()

        request `should be instance of` FeedRequest.DeleteFeedItem::class.java

    }

    @Test
    fun `when a feed item is clicked, we get the email preview and show the emailDetailScene`() {
        // init
        controller.onStart()

        simulateLoadOfFeeds(2)

        scene.feedEventListener?.onFeedItemClicked(email = testEmail)

        feedEventListenerSlot.captured.onFeedItemClicked(email = testEmail)

        val request = sentRequests.last()

        request `should be instance of` FeedRequest.GetEmailPreview::class.java

        listenerSlot.captured(FeedResult.GetEmailPreview.Success(
                emailPreview = mockk(relaxed = true),
                isTrash = false,
                isSpam = false))

        verify { host.goToScene(any(), any()) }

    }

}