package com.email.views.mailbox

import com.email.views.ActivityModel
import com.email.views.mailbox.data.EmailThread

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
