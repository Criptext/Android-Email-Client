package com.email.scenes.search

import com.email.scenes.SceneModel
import com.email.scenes.mailbox.MailboxSceneModel
import com.email.scenes.mailbox.data.EmailThread
import com.email.scenes.search.data.SearchItem

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchSceneModel: SceneModel {
    val threads : ArrayList<EmailThread> = ArrayList()
    val searchItems : ArrayList<SearchItem> = ArrayList()
    var queryText: String = ""
    var hasReachedEnd = true
}
