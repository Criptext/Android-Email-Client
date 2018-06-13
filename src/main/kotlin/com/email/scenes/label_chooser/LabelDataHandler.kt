package com.email.scenes.label_chooser

import com.email.scenes.SceneController
import com.email.scenes.emaildetail.EmailDetailSceneController
import com.email.scenes.mailbox.MailboxSceneController

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
