package com.email.scenes.mailbox.ui

import android.provider.ContactsContract
import com.email.scenes.mailbox.data.ActivityFeed
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by danieltigse on 02/14/18.
 */

class FeedListController(private var feedItems : ArrayList<ActivityFeed>) {

    fun setFeedList(feeds : List<ActivityFeed>) {
        feedItems.clear()
        feedItems.addAll(feeds)
    }

}
