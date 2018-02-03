package com.email.scenes.LabelChooser

import android.util.Log
import com.email.scenes.LabelChooser.data.LabelChooserDataSource
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.scenes.SceneController
import com.email.scenes.mailbox.SelectedThreads
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 2/2/18.
 */

class LabelChooserSceneController(private val scene: LabelChooserScene,
                                  private val model: LabelChooserSceneModel,
                                  private val dataSource: LabelChooserDataSource) : SceneController() {

    private val labelThreadEventListener = object : LabelThreadAdapter.OnLabelThreadEventListener{
        override fun onToggleLabelSelection(label: LabelThread, position: Int) {
            if(!label.isSelected) selectLabelThread(label, position)
            else unselectLabelThread(label, position)
        }
    }

    private fun selectLabelThread(labelThread: LabelThread, position: Int) {
        model.selectedLabels.add(labelThread)
        labelThread.isSelected = true
        scene.notifyLabelThreadChanged(position)
    }

    private fun unselectLabelThread(labelThread: LabelThread, position: Int) {
        labelThread.isSelected = false
        model.selectedLabels.remove(labelThread)
        scene.notifyLabelThreadChanged(position)
    }

    fun assignLabels(selectedThreads : SelectedThreads) {
        selectedThreads.toList().forEach {
            val emailThread : EmailThread = it
            model.selectedLabels.toIDs().forEach {
                try {
                    assignLabelToEmailThread(emailThread, it)
                } catch (e: android.database.sqlite.SQLiteConstraintException) {
                    Log.d("error", e.toString())
                    e.printStackTrace()
                }
            }
        }
    }

    fun clearSelectedLabels() {
        model.selectedLabels.clear()
    }
    fun assignLabelToEmailThread(emailThread: EmailThread, labelId: Int) {
        dataSource.createLabelEmailRelation(labelId = labelId, emailId = emailThread.id)
    }
    override fun onStart() {
        val labelThreads : List<LabelThread> = dataSource.getAllLabels()
        model.labels = labelThreads as ArrayList<LabelThread>
        scene.attachView(labelThreadEventListener)
    }

    override fun onStop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
