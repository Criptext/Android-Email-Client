package com.email.scenes.mailbox.feed

import com.email.db.models.FeedItem
import com.email.mocks.MockedWorkRunner
import com.email.scenes.mailbox.feed.data.DeleteFeedItemWorker
import com.email.scenes.mailbox.feed.data.FeedDataSource
import com.email.scenes.mailbox.feed.data.LoadFeedsWorker
import com.email.scenes.mailbox.feed.data.MuteFeedItemWorker
import com.email.scenes.mailbox.feed.mocks.MockedFeedLocalDB
import com.email.scenes.mailbox.feed.mocks.MockedFeedView
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test

import java.util.*

/**
 * Created by gabriel on 2/21/18.
 */
class FeedControllerTest {

    private lateinit var model: FeedModel
    private lateinit var scene: MockedFeedView
    private lateinit var db: MockedFeedLocalDB
    private lateinit var runner: MockedWorkRunner
    private lateinit var dataSource: FeedDataSource
    private lateinit var controller: FeedController

    @Before
    fun setUp() {
        model = FeedModel()
        scene = MockedFeedView()
        runner = MockedWorkRunner()
        db = MockedFeedLocalDB()
        dataSource = FeedDataSource(runner, db)
        controller = FeedController(model, scene, dataSource)
    }

    private fun createFeedItems(size: Int): List<FeedItem> {
        return (1..size).map {
            Thread.sleep(5) // delay between dates
            FeedItem(id = it, feedType = 0, feedTitle = "notification #$it",
                    feedSubtitle = "subtitle #$it", feedDate = Date(), isNew = true, isMuted = false)
        }.reversed()
    }

    @Test
    fun `onStart should set listeners to the view and data source and onStop should clear them`() {
        controller.onStart()

        dataSource.listener `should not be` null
        scene.feedClickListener `should not be` null

        controller.onStop()

        dataSource.listener `should be` null
        scene.feedClickListener `should be` null
    }

    @Test
    fun `on a cold start, should show 'empty view' and load feed`() {
        controller.onStart()

        scene.isNoFeedsViewVisible `should be` true
        model.feedItems.`should be empty`()

        runner.assertPendingWork(listOf(LoadFeedsWorker::class.java))

        db.nextLoadedFeedItems = createFeedItems(5)
        scene.notifiedDataSetChanged = false

        runner._work()

        model.feedItems.size `should be` 5
        scene.isNoFeedsViewVisible `should be` false
        scene.notifiedDataSetChanged `should be` true
    }

    private fun muteItemAt(position: Int, exception: Exception?) {
        // init
        controller.onStart()
        runner._work()

        val mutedItem = model.feedItems[position]
        mutedItem.isMuted `should be` false

        scene.lastNotifiedChangedPosition = -1

        // fire click event
        scene.feedClickListener!!.onMuteFeedItemClicked(feedId = mutedItem.id, position = position,
                isMuted = true)

        mutedItem.isMuted `should be` true

        db.nextMuteFeedItemException = exception
        scene.lastNotifiedChangedPosition `should be` position
        scene.lastNotifiedChangedPosition = -1

        // complete task and run callbacks
        runner.assertPendingWork(listOf(MuteFeedItemWorker::class.java))
        runner._work()
    }


    @Test
    fun `when the mute button is clicked, on abscene of errors, should update the row, and update the db`() {
        db.nextLoadedFeedItems = createFeedItems(5)
        muteItemAt(3, null)

        val mutedItem = model.feedItems[3]

        mutedItem.isMuted `should be` true
        scene.lastNotifiedChangedPosition `should be` -1 // don't notify changed again
        scene.shownError `should be` null
    }

    @Test
    fun `when the mute button is clicked, if error occurs, reverts the mute update and shows error message`() {
        db.nextLoadedFeedItems = createFeedItems(5)
        muteItemAt(3, Exception("Database Error")) // throw db error

        val mutedItem = model.feedItems[3]

        mutedItem.isMuted `should be` false
        scene.lastNotifiedChangedPosition `should be` 3
        scene.shownError!! `should end with`  "Database Error"
    }

    private fun deleteItemAt(position: Int, exception: Exception?) {
        // init
        controller.onStart()
        runner._work()

        val deletedItem = model.feedItems[position]
        scene.lastNotifiedChangedPosition = -1
        val expectedCountAfterDeletion = model.feedItems.size - 1

        // fire click event
        scene.feedClickListener!!.onDeleteFeedItemClicked(feedId = deletedItem.id, position = position)

        model.feedItems.size `should be` expectedCountAfterDeletion

        db.nextDeleteFeedItemException = exception
        scene.lastNotifiedRemovedPosition `should be` position
        scene.lastNotifiedRemovedPosition = -1
        scene.lastNotifiedInsertedPosition = -1

        // complete task and run callbacks
        runner.assertPendingWork(listOf(DeleteFeedItemWorker::class.java))
        runner._work()
    }

    @Test
    fun `when the delete button is clicked, on abscene of errors, should delete the row, and update the db`() {
        db.nextLoadedFeedItems = createFeedItems(5)
        deleteItemAt(3, null)

        model.feedItems.size `should be` 4
        scene.lastNotifiedRemovedPosition `should be` -1 // don't notify removed again
        scene.shownError `should be` null
    }

    @Test
    fun `when the delete button is clicked, if error occurs, reverts the deletion and shows error message`() {
        db.nextLoadedFeedItems = createFeedItems(5)

        deleteItemAt(3, Exception("Database Error")) // throw db error

        model.feedItems.size `should be` 5
        scene.lastNotifiedInsertedPosition `should equal` 3 // should insert back item
        scene.shownError!! `should end with`  "Database Error"
    }
}