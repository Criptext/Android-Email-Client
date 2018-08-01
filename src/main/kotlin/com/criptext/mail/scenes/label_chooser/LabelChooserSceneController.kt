package com.criptext.mail.scenes.label_chooser

import com.criptext.mail.db.models.Label
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper

/**
 * Created by sebas on 2/2/18.
 */

class LabelChooserSceneController(private val scene: LabelChooserScene,
                                  private val model: LabelChooserSceneModel,
                                  private val labelDataHandler: LabelDataHandler) {

    val dialogLabelsListener : LabelChooserDialog.DialogLabelsListener =
            object : LabelChooserDialog.DialogLabelsListener {
                override fun onDialogPositiveClick() {
                    labelDataHandler.createRelationEmailLabels(model.selectedLabels)
                }

                override fun onDialogNegativeClick() {
                }
            }

    private val labelWrapperEventListener = object : LabelWrapperAdapter.OnLabelWrapperEventListener{
        override fun onToggleLabelSelection(label: LabelWrapper, position: Int) {
            if(!label.isSelected) selectLabelWrapper(label, position)
            else unselectLabelWrapper(label, position)
        }
    }

    private fun selectLabelWrapper(labelWrapper: LabelWrapper, position: Int) {
        model.selectedLabels.add(labelWrapper)
        labelWrapper.isSelected = true
        scene.notifyLabelWrapperChanged(position)
    }

    private fun unselectLabelWrapper(labelWrapper: LabelWrapper, position: Int) {
        labelWrapper.isSelected = false
        model.selectedLabels.remove(labelWrapper)
        scene.notifyLabelWrapperChanged(position)
    }

    fun start() {
        scene.attachView(labelWrapperEventListener)
    }

    fun onFetchedLabels(
            defaultSelectedLabels: List<Label>,
            labels: List<Label>) {

        model.selectedLabels.clear()
        model.labels.clear()

        val labelWrappers = labels.map {
            LabelWrapper(it)
        }

        val selectedLabelWrappers = defaultSelectedLabels.map {
            val labelWrapper = LabelWrapper(it)
            labelWrapper.isSelected = true
            labelWrapper
        }

        labelWrappers.forEach { labelWrapper ->
            defaultSelectedLabels.forEach { selectedLabel ->
                if(labelWrapper.id == selectedLabel.id){
                    labelWrapper.isSelected = true
                }
            }
        }

        model.selectedLabels.addMultipleSelected(selectedLabelWrappers)
        model.labels.addAll(labelWrappers)

        scene.onFetchedLabels()
    }
}
