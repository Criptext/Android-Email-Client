package com.email.scenes.labelChooser

import com.email.scenes.SceneController
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
            else -> {

            }
        }
    }

    val createLabelEmailRelation = {
        labelId: Int, emailThreadId: Int ->
        when(sceneController) {
            is MailboxSceneController -> {
                sceneController.assignLabelToEmailThread(labelId,
                        emailThreadId)
            } else -> {

        }
        }
    }

    val getAllLabels = {
        when(sceneController) {
            is MailboxSceneController -> {
                sceneController.getAllLabels()
            } else -> {
                null
            }
        }
    }
}
