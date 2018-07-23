package com.email.scenes.label_chooser

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.email.R
import com.email.db.models.Label
import com.email.scenes.label_chooser.data.LabelWrapper
import com.email.utils.virtuallist.VirtualList


/**
 * Created by sebas on 2/1/18.
 */

class LabelChooserDialog(private val context: Context, private val rootView: View) {
    private var labelChooserDialog : AlertDialog? = null
    private lateinit var controller: LabelChooserSceneController

    private fun createDialog(dialogView: View, dialogBuilder: AlertDialog.Builder): AlertDialog {
        val width = context.resources.getDimension(R.dimen.alert_dialog_label_chooser_width).toInt()
        val newLabelChooserDialog = dialogBuilder.create()
        newLabelChooserDialog.show()

        val window = newLabelChooserDialog.window
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER_VERTICAL)

        val drawableBackground = ContextCompat.getDrawable(context, R.drawable.dialog_label_chooser_shape)
        newLabelChooserDialog.window.setBackgroundDrawable(drawableBackground)
        assignButtonEvents(dialogView, newLabelChooserDialog, controller.dialogLabelsListener)
        return newLabelChooserDialog
    }

    private fun createController(dialogView: View, dataSource: LabelDataHandler)
            : LabelChooserSceneController {
        val model = LabelChooserSceneModel()
        val scene = LabelChooserScene.LabelChooserView(
                dialogView ,LabelList(model.labels))

        return LabelChooserSceneController(
                scene = scene,
                model = model,
                labelDataHandler = dataSource
        )
    }

    fun showDialogLabelsChooser(dataHandler: LabelDataHandler) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.mailbox_labels_chooser, rootView as ViewGroup, false)
        dialogBuilder.setView(dialogView)

        controller = createController(dialogView, dataHandler)
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
            labelChooserDialog?.dismiss()
        }

        btnCancel.setOnClickListener {
            dialogLabelsListener.onDialogNegativeClick()
            labelChooserDialog?.dismiss()
        }
}

    interface DialogLabelsListener {
        fun onDialogPositiveClick()
        fun onDialogNegativeClick()
    }

    private class LabelList(val labels: List<LabelWrapper>): VirtualList<LabelWrapper> {
        override val hasReachedEnd = true

        override fun get(i: Int): LabelWrapper {
            return labels[i]
        }

        override val size: Int
            get() = labels.size
    }

    fun onFetchedLabels(
            defaultSelectedLabels: List<Label>,
            allLabels: List<Label> ) {
        controller.onFetchedLabels(
                defaultSelectedLabels, allLabels)
    }
}
