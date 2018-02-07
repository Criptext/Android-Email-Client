package com.email.scenes.LabelChooser

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.widget.Button
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.scenes.SceneController

/**
 * Created by sebas on 2/2/18.
 */

class LabelChooserSceneController(private val scene: LabelChooserScene,
                                  private val model: LabelChooserSceneModel,
                                  private val labelDataSourceHandler: LabelDataSourceHandler)
    : SceneController() {

    val dialogLabelsListener : LabelChooserDialog.DialogLabelsListener = object : LabelChooserDialog.DialogLabelsListener {
        override fun onDialogPositiveClick(dialog: AlertDialog) {
            labelDataSourceHandler.createRelationEmailLabels(model.selectedLabels)
            clearSelectedLabels()
            dialog.dismiss()
        }


        override fun onDialogNegativeClick(dialog: AlertDialog) {
            dialog.dismiss()
        }

    }
    override fun onBackPressed(activity: Activity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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

    fun clearSelectedLabels() {
        model.selectedLabels.clear()
    }

    override fun onStart() {
        val labelThreads = labelDataSourceHandler.getAllLabels()
        model.labels.clear()
        model.labels.addAll(labelThreads)
        scene.attachView(labelThreadEventListener)
    }

    override fun onStop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun assignButtonEvents(dialog: AlertDialog, btn_add: Button, btn_cancel: Button) {
        btn_add.setOnClickListener {
            dialogLabelsListener.onDialogPositiveClick(dialog)
        }

        btn_cancel.setOnClickListener {
            dialogLabelsListener.onDialogNegativeClick(dialog)
        }
    }

}
