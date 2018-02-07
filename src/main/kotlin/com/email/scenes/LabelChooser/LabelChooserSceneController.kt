package com.email.scenes.LabelChooser

import android.app.Activity
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.email.MailboxActivity
import com.email.R
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.scenes.SceneController
import com.email.scenes.mailbox.data.EmailThread

/**
 * Created by sebas on 2/2/18.
 */

class LabelChooserSceneController(private val scene: LabelChooserScene,
                                  private val model: LabelChooserSceneModel,
                                  private val labelDataSourceHandler: MailboxActivity.LabelDataSourceHandler) : SceneController() {

    val dialogLabelsListener : DialogLabelsChooser.DialogLabelsListener = object : DialogLabelsChooser.DialogLabelsListener {
        override fun onDialogPositiveClick(dialog: DialogFragment) {
            labelDataSourceHandler.createRelationEmailLabels(model.selectedLabels)
            clearSelectedLabels()
            dialog.dismiss()
        }


        override fun onDialogNegativeClick(dialog: DialogFragment) {
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

    fun initUI(dialog: DialogFragment, view:View,  inflater: LayoutInflater?){
        val btn_add = view.findViewById(R.id.label_add) as Button
        val btn_cancel = view.findViewById(R.id.label_cancel) as Button
        assignButtonEvents(dialog, btn_add, btn_cancel)
    }
    fun assignButtonEvents(dialog: DialogFragment, btn_add: Button, btn_cancel: Button) {
        btn_add.setOnClickListener {
            dialogLabelsListener.onDialogPositiveClick(dialog)
        }

        btn_cancel.setOnClickListener {
            dialogLabelsListener.onDialogNegativeClick(dialog)
        }
    }

}
