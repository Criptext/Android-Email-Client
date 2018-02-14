package com.email.scenes.LabelChooser

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.email.R
import com.email.scenes.LabelChooser.data.LabelThread
import com.email.utils.VirtualList


/**
 * Created by sebas on 2/1/18.
 */

class LabelChooserDialog(private val context: Context) {
    private var labelChooserDialog : AlertDialog? = null
    private lateinit var controller: LabelChooserSceneController

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder): AlertDialog {
        val newLabelChooserDialog = dialogBuilder.create()
        newLabelChooserDialog.show()
        newLabelChooserDialog.window.setLayout(600, 800)
        assignButtonEvents(dialogView, newLabelChooserDialog, controller.dialogLabelsListener)
        return newLabelChooserDialog
    }

    private fun createController(dialogView: View, dataSource: LabelDataSourceHandler)
            : LabelChooserSceneController {
        val model = LabelChooserSceneModel()
        val scene = LabelChooserScene.LabelChooserView(dialogView ,LabelList(model.labels))
        return LabelChooserSceneController(
                scene = scene,
                model = model,
                labelDataSourceHandler = dataSource
        )
    }

    fun showDialogLabelsChooser(dataSource: LabelDataSourceHandler) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.mailbox_labels_chooser, null)
        dialogBuilder.setView(dialogView)

        controller = createController(dialogView, dataSource)
        labelChooserDialog = createDialog(dialogView, dialogBuilder)

        controller.start()
    }

    private fun assignButtonEvents(view: View, dialog: AlertDialog,
                           dialogLabelsListener: DialogLabelsListener
                           ) {
        val btnAdd = view.findViewById<Button>(R.id.label_add)
        val btnCancel = view.findViewById<Button>(R.id.label_cancel)

        btnAdd.setOnClickListener {
            dialogLabelsListener.onDialogPositiveClick()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialogLabelsListener.onDialogNegativeClick()
            dialog.dismiss()
        }
}

    interface DialogLabelsListener {
        fun onDialogPositiveClick()
        fun onDialogNegativeClick()
    }

    private class LabelList(val labels: List<LabelThread>): VirtualList<LabelThread> {
        override fun get(i: Int): LabelThread {
            return labels[i]
        }

        override val size: Int
            get() = labels.size
    }

}