package com.criptext.mail.scenes.label_chooser

import com.criptext.mail.scenes.SceneController
import com.criptext.mail.scenes.emaildetail.EmailDetailSceneController
import com.criptext.mail.scenes.mailbox.MailboxSceneController

/**
 * Created by sebas on 2/7/18.
 */
class LabelDataHandler(sceneController: SceneController) {
    val createRelationEmailLabels = {
        selectedLabels: SelectedLabels ->
        when(sceneController) {
            is MailboxSceneController -> {
                sceneController.updateEmailThreadsLabelsRelations(selectedLabels)
            }
            is EmailDetailSceneController -> {
                sceneController.updateThreadLabelsRelation(selectedLabels)
            }
            else -> {

            }
        }
    }

}
