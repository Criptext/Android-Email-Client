package com.email.scenes.labelChooser

import com.email.scenes.mailbox.MailboxSceneController

/**
 * Created by sebas on 2/7/18.
 */
class LabelDataSourceHandler(mailboxSceneController: MailboxSceneController) {
    val createRelationEmailLabels = {
        selectedLabels: SelectedLabels ->
        mailboxSceneController.createRelationSelectedEmailLabels(selectedLabels)
    }

    val createLabelEmailRelation = {
        labelId: Int, emailThreadId: Int ->
        mailboxSceneController.assignLabelToEmailThread(labelId,
                emailThreadId)
    }

    val getAllLabels = {
        mailboxSceneController.getAllLabels()
    }
}
