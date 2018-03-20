package com.email.scenes.labelChooser

import com.email.scenes.SceneController
import com.email.scenes.emaildetail.EmailDetailSceneController
import com.email.scenes.mailbox.MailboxSceneController

/**
 * Created by sebas on 2/7/18.
 */
class LabelDataSourceHandler(sceneController: SceneController) {
    val createRelationEmailLabels = {
        selectedLabels: SelectedLabels ->
        when(sceneController) {
            is MailboxSceneController -> {
                sceneController.createRelationSelectedEmailLabels(selectedLabels)
            }
            is EmailDetailSceneController -> {
                sceneController.createRelationSelectedEmailLabels(selectedLabels)
            }
            else -> {

            }
        }
    }

}
