package com.email.scenes.mailbox

/**
 * Created by sebas on 2/8/18.
 */

class OnMoveThreadsListener(moveListener: MailboxSceneController.OnMoveListener) {

    val moveToSpam = {
        ->
        moveListener.moveSelectedEmailsToSpam()
    }

    val moveToTrash = {
        ->
        moveListener.moveSelectedEmailsToTrash()
    }
}
