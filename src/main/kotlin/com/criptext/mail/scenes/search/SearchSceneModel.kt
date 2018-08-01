package com.criptext.mail.scenes.search

import com.criptext.mail.scenes.SceneModel
import com.criptext.mail.scenes.mailbox.data.EmailThread

/**
 * Created by danieltigse on 2/2/18.
 */

class SearchSceneModel: SceneModel {
    val threads : ArrayList<EmailThread> = ArrayList()
    var queryText: String = ""
    var hasReachedEnd = true
}
