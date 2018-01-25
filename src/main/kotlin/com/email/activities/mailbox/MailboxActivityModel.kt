package com.email.activities.mailbox

import com.email.activities.ActivityModel
import com.email.activities.mailbox.data.EmailThread

/**
 * Created by sebas on 1/25/18.
 */

/**
 * Created by sebas on 1/25/18.
 */

class MailboxActivityModel(var label : String): ActivityModel {
    internal val threads : ArrayList<EmailThread> = ArrayList()
    internal val set: HashSet<String> = HashSet()

    internal var firstVisibleItem = 0
    internal var openedThread : String = ""
    internal var isInMultiSelect = false
    internal var shouldShowPartialUpdateInUI = true

}
