package com.email.scenes.labelChooser

import com.email.db.models.Label
import com.email.scenes.labelChooser.data.LabelThread

/**
 * Created by sebas on 2/2/18.
 */

class LabelChooserSceneController(private val scene: LabelChooserScene,
                                  private val model: LabelChooserSceneModel,
                                  private val labelDataSourceHandler: LabelDataSourceHandler) {

    val dialogLabelsListener : LabelChooserDialog.DialogLabelsListener =
            object : LabelChooserDialog.DialogLabelsListener {
        override fun onDialogPositiveClick() {
            labelDataSourceHandler.createRelationEmailLabels(model.selectedLabels)
            clearSelectedLabels()
        }

        override fun onDialogNegativeClick() {
        }
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

    fun start() {
        scene.attachView(labelThreadEventListener)
    }

    fun onFetchedLabels(defaultSelectedLabels: List<Label>, labels: List<Label>) {
        val labelThreads = labels.map {
            LabelThread(it)
        }
        model.labels.clear()
        model.labels.addAll(labelThreads)

        model.selectedLabels.clear()

        val defaultSelectedLabelThreads = defaultSelectedLabels.map {
            LabelThread(it)
        }

        model.selectedLabels.addMultipleSelected(defaultSelectedLabelThreads)

        scene.onFetchedLabels()
    }
}
