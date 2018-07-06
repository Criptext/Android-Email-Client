package com.email.websocket.data

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.api.models.TrackingUpdate
import com.email.db.DeliveryTypes
import com.email.db.FeedType
import com.email.db.models.Contact
import com.email.db.models.Label
import com.email.mocks.MockEmailData
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldNotBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by gabriel on 6/29/18.
 */

@RunWith(AndroidJUnit4::class)
class UpdateDeliveryStatusWorkerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.labelDao().insertAll(Label.DefaultItems().toList())
    }

    private fun newWorker(trackingUpdate: TrackingUpdate): UpdateDeliveryStatusWorker =
            UpdateDeliveryStatusWorker(dao = db.emailDao(), feedDao = db.feedDao(),
                    contactDao = db.contactDao(), publishFn = {},
                    trackingUpdate = trackingUpdate)


    @Test
    fun should_mark_email_as_read_and_return_the_tracking_update_if_email_was_not_already_read() {
        // insert email that has not been read already
        val existingEmail = MockEmailData.createNewEmail(1)
        existingEmail.metadataKey = 121
        existingEmail.delivered = DeliveryTypes.SENT
        db.emailDao().insert(existingEmail)
        db.contactDao().insertIgnoringConflicts(Contact(1, "mayer@jigl.com", "Mayer"))

        val worker = newWorker(TrackingUpdate(metadataKey = 121, type = DeliveryTypes.READ,
                from = "mayer"))

        val result = worker.work(mockk(relaxed = true)) as EventResult.UpdateDeliveryStatus.Success

        result.update `shouldNotBe` null

        // assert that email got updated in DB
        val updatedEmail = db.emailDao().findEmailByMetadataKey(121)
        updatedEmail?.delivered `shouldBe` DeliveryTypes.READ
    }

    @Test
    fun if_email_is_already_read_should_do_nothing_and_return_null() {
        // insert email that has been read already
        val existingEmail = MockEmailData.createNewEmail(1)
        existingEmail.metadataKey = 121
        existingEmail.delivered = DeliveryTypes.READ
        db.emailDao().insert(existingEmail)

        val worker = newWorker(TrackingUpdate(metadataKey = 121, type = DeliveryTypes.READ,
                from = "mayer@jigl.com"))

        val result = worker.work(mockk(relaxed = true)) as EventResult.UpdateDeliveryStatus.Success

        result.update `shouldBe` null
    }

    @Test
    fun should_create_feed_and_return_the_tracking_update_if_email_was_not_already_read() {
        // insert email that has not been read already
        val existingEmail = MockEmailData.createNewEmail(1)
        existingEmail.metadataKey = 121
        existingEmail.delivered = DeliveryTypes.SENT
        db.emailDao().insert(existingEmail)
        db.contactDao().insertIgnoringConflicts(Contact(1, "mayer@jigl.com", "Mayer"))

        var feeds = db.feedDao().getAllFeedItems()
        feeds.size shouldBe 0

        val worker = newWorker(TrackingUpdate(metadataKey = 121, type = DeliveryTypes.READ,
                from = "mayer"))

        worker.work(mockk(relaxed = true)) as EventResult.UpdateDeliveryStatus.Success

        feeds = db.feedDao().getAllFeedItems()
        feeds.size shouldBe 1

        feeds[0].feedType shouldBe FeedType.OPEN_EMAIL
    }

    @Test
    fun if_email_is_already_read_should_not_create_feed_and_return_null() {
        // insert email that has been read already
        val existingEmail = MockEmailData.createNewEmail(1)
        existingEmail.metadataKey = 121
        existingEmail.delivered = DeliveryTypes.READ
        db.emailDao().insert(existingEmail)

        val worker = newWorker(TrackingUpdate(metadataKey = 121, type = DeliveryTypes.READ,
                from = "mayer@jigl.com"))

        val result = worker.work(mockk(relaxed = true)) as EventResult.UpdateDeliveryStatus.Success

        db.feedDao().getAllFeedItems().size shouldBe 0

        result.update shouldBe null
    }

}