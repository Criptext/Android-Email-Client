package com.criptext.mail.scenes.mailbox

/**
 * Created by sebas on 2/8/18.
 */

interface OnMoveThreadsListener {
    fun onMoveToInboxClicked()
    fun onMoveToSpamClicked()
    fun onMoveToTrashClicked()
}
