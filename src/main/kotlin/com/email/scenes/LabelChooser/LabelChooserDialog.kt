package com.email.scenes.LabelChooser

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import com.email.R
import com.email.MailboxActivity


/**
 * Created by sebas on 2/1/18.
 */

class LabelChooserDialog(val context: Context) {
    private var labelChooserDialog : AlertDialog? = null
    private val labelChooserSceneModel : LabelChooserSceneModel = LabelChooserSceneModel()
    private lateinit var labelChooserSceneController: LabelChooserSceneController
    private val labelThreadListHandler : LabelThreadListHandler = LabelThreadListHandler(labelChooserSceneModel)
    private lateinit var btn_add: Button
    private lateinit var btn_cancel: Button

    fun showdialogLabelsChooser(labelDataSourceHandler: LabelDataSourceHandler) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = (context as MailboxActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.mailbox_labels_chooser, null)
        dialogBuilder.setView(dialogView)

        val labelChooserScene = LabelChooserScene.LabelChooserView(context,dialogView ,labelThreadListHandler)
        labelChooserSceneController = LabelChooserSceneController(
                scene = labelChooserScene,
                model = labelChooserSceneModel,
                labelDataSourceHandler = labelDataSourceHandler

        )
        labelChooserDialog = dialogBuilder.create()
        labelChooserDialog?.show()
        labelChooserDialog?.getWindow()?.setLayout(500, 700)
        val drawableBackground = context.resources.getDrawable(R.drawable.dialog_label_chooser_shape)
        labelChooserDialog?.window?.setBackgroundDrawable(drawableBackground)
        startController()
        initButtons(dialogView)
        assignButtonEvents(labelChooserDialog as AlertDialog,
                labelChooserSceneController.dialogLabelsListener)
    }
    fun startController() {
        labelChooserSceneController.onStart()
    }

    fun initButtons(view: View){
        btn_add = view.findViewById(R.id.label_add) as Button
        btn_cancel = view.findViewById(R.id.label_cancel) as Button
    }
    fun assignButtonEvents(dialog: AlertDialog,
                           dialogLabelsListener: DialogLabelsListener
                           ) {
        btn_add.setOnClickListener {
            dialogLabelsListener.onDialogPositiveClick(dialog)
        }

        btn_cancel.setOnClickListener {
            dialogLabelsListener.onDialogNegativeClick(dialog)
        }
}

    interface DialogLabelsListener {
        fun onDialogPositiveClick(dialog: AlertDialog)
        fun onDialogNegativeClick(dialog: AlertDialog)
    }

    inner class LabelThreadListHandler(val model: LabelChooserSceneModel) {
        val getLabelThreadFromIndex = {
            i: Int ->
            model.labels[i]
        }
        val getLabelThreadsCount = {
            model.labels.size
        }
    }

}
