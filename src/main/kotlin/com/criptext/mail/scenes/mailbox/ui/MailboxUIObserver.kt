package com.criptext.mail.scenes.mailbox.ui

import com.criptext.mail.utils.uiobserver.UIObserver

/**
 * Created by gabriel on 2/28/18.
 */

interface MailboxUIObserver: UIObserver {
    fun onOpenComposerButtonClicked()
    fun onRefreshMails()
    fun onBackButtonPressed()
    fun onFeedDrawerClosed()
    fun onEmptyTrashPressed()
}