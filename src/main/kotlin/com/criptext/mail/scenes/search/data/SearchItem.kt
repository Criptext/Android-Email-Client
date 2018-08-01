package com.criptext.mail.scenes.search.data

/**
 * Created by danieltigse on 2/6/18.
 */

class SearchItem(val id: Int, val subject: String, val recipients: String) {

    var threadId: String? = null
    var timestampCreated: Long? = null

}