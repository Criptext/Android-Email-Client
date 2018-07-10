package com.email.scenes.search

import com.email.scenes.SceneModel
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchSceneModel: SceneModel {
    val threads : ArrayList<EmailThread> = ArrayList()
    var queryText: String = ""
    var hasReachedEnd = true
}
