package com.criptext.mail.scenes.mailbox.feed

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.criptext.mail.IHostActivity
import com.criptext.mail.R
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Email
import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.mailbox.feed.data.FeedDataSource
import com.criptext.mail.scenes.mailbox.feed.data.FeedRequest
import com.criptext.mail.scenes.mailbox.feed.data.FeedResult
import com.criptext.mail.scenes.mailbox.feed.ui.FeedItemHolder
import com.criptext.mail.scenes.mailbox.feed.ui.FeedListController
import com.criptext.mail.scenes.mailbox.feed.ui.FeedScene
import com.criptext.mail.scenes.params.EmailDetailParams

/**
 * Created by danieltigse on 2/15/18.
 */

open class FeedController(private val model: FeedModel,
                          private val scene: FeedScene,
                          private val host: IHostActivity,
                          private val activeAccount: ActiveAccount,
                          private val storage: KeyValueStorage,
                          private val feedDataSource: FeedDataSource){

    var lastTimeFeedOpened: Long
        get() = storage.getLong(KeyValueStorage.StringKey.LastTimeFeedOpened, 0)
        set(value) {
            storage.putLong(KeyValueStorage.StringKey.LastTimeFeedOpened, value)
        }

    private val feedListController = FeedListController(model, scene.virtualListView)

    private val feedEventListener = object : FeedItemHolder.FeedEventListener{

        override fun onFeedItemClicked(email: Email) {
            feedDataSource.submitRequest(FeedRequest.GetEmailPreview(
                    email = email,
                    userEmail = activeAccount.userEmail
            ))
        }

        override fun onApproachingEnd() {

        }

        override fun onMuteFeedItemClicked(feedId: Long, position: Int, isMuted: Boolean) {
            feedListController.toggleMutedFeedItem(id = feedId,
                    lastPosition = position)
            feedDataSource.submitRequest(FeedRequest.MuteFeedItem(id = feedId,
                    position = position,
                    isMuted = isMuted))
        }

        override fun onDeleteFeedItemClicked(feedId: Long, position: Int) {
            val deleted = feedListController.deleteFeedItem(id = feedId, lastPosition = position)
            if (deleted != null) {
                val req = FeedRequest.DeleteFeedItem(item = deleted, position = position)
                feedDataSource.submitRequest(req)
            }
        }

        override fun showStartGuideNotification(view: View) {
            host.showStartGuideView(
                    view,
                    R.string.start_guide_notification,
                    R.dimen.focal_padding_inverse
            )
        }
    }

    private val dataSourceListener = { result: FeedResult ->
        when (result) {
            is FeedResult.LoadFeed -> onFeedItemsLoaded(result)
            is FeedResult.DeleteFeedItem -> onFeedItemDeleted(result)
            is FeedResult.MuteFeedItem -> onFeedItemMuted(result)
            is FeedResult.GetEmailPreview -> onGetEmailPreview(result)
        }
    }

    private fun onGetEmailPreview(result: FeedResult.GetEmailPreview){
        when(result){
            is FeedResult.GetEmailPreview.Success -> {
                val currentLabel = when {
                    result.isTrash -> Label.defaultItems.trash
                    result.isSpam -> Label.defaultItems.spam
                    else -> Label.defaultItems.inbox
                }
                host.goToScene(EmailDetailParams(threadId = result.emailPreview.threadId,
                      currentLabel = currentLabel, threadPreview = result.emailPreview), true)
            }
            is FeedResult.GetEmailPreview.Failure -> {
                scene.showError(result.message)
            }
        }
    }

    private fun onFeedItemsLoaded(result: FeedResult.LoadFeed) {
        when (result) {
            is FeedResult.LoadFeed.Success -> {
                feedListController.populateFeeds(result.feedItems)
                scene.setAdapter(
                        virtualFeedList = VirtualFeedList(model),
                        lastTimeFeedOpened = lastTimeFeedOpened,
                        feedEventListener = feedEventListener,
                        totalNewFeeds = result.totalNewFeeds)
                scene.updateFeedBadge(result.totalNewFeeds)
                showStartGuideNotifications(result.totalNewFeeds)
            }
            is FeedResult.LoadFeed.Failure -> scene.showError(result.message)
        }
    }

    private fun showStartGuideNotifications(totalNewFeeds: Int) {
        if(storage.getBool(KeyValueStorage.StringKey.StartGuideShowNotification, true) && totalNewFeeds > 0){
            scene.showStartGuideNotification()
            storage.putBool(KeyValueStorage.StringKey.StartGuideShowNotification, false)
        }
    }

    private fun onFeedItemDeleted(result: FeedResult.DeleteFeedItem) {
        when (result) {
            is FeedResult.DeleteFeedItem.Success -> {
                reloadFeeds()
            }
            is FeedResult.DeleteFeedItem.Failure -> {
                feedListController.insertFeedItem(result.item)
                scene.showError(result.message)
            }
        }
    }

    private fun onFeedItemMuted(result: FeedResult.MuteFeedItem) {
        when (result) {
            is FeedResult.MuteFeedItem.Success -> {/* NoOp */}
            is FeedResult.MuteFeedItem.Failure -> {
                feedListController.toggleMutedFeedItem(id = result.id,
                        lastPosition = result.lastKnownPosition)
                scene.showError(result.message)
            }
        }
    }

    fun onMenuChanged(menu: IHostActivity.IActivityMenu){
        scene.initializeMenu(menu)
    }

    fun onStart() {
        if (model.feedItems.isEmpty())
            reloadFeeds()
        feedDataSource.listener = dataSourceListener
    }

    fun onStop() {
        feedDataSource.listener = null
    }

    fun reloadFeeds(){
        feedDataSource.submitRequest(FeedRequest.LoadFeed(lastTimeFeedOpened))
    }
}