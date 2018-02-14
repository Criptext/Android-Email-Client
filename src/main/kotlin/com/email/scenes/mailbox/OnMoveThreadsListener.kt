package com.email.scenes.mailbox

/**
 * Created by sebas on 2/8/18.
 */

class OnMoveThreadsListener(mailboxSceneController: MailboxSceneController) {

    val moveToSpam = {
        ->
        mailboxSceneController.moveSelectedEmailsToSpam()
    }

    val moveToTrash = {
        ->
        mailboxSceneController.moveSelectedEmailsToTrash()
    }
}
