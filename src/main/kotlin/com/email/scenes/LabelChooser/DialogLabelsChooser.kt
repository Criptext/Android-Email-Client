package com.email.scenes.LabelChooser

import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import com.email.MailboxActivity
import com.email.androidui.SceneFactory


/**
 * Created by sebas on 2/1/18.
 */

class DialogLabelsChooser(): DialogFragment(){

    interface DialogLabelsListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }
    lateinit var sceneFactory: SceneFactory
    lateinit var labelDataSourceHandler: MailboxActivity.LabelDataSourceHandler
    private val labelChooserSceneModel : LabelChooserSceneModel = LabelChooserSceneModel()
    private lateinit var labelChooserSceneController: LabelChooserSceneController
    private val labelThreadListHandler : LabelThreadListHandler = LabelThreadListHandler(labelChooserSceneModel)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.mailbox_labels_chooser, container)
        initMVC(view)
        labelChooserSceneController.initUI(this, view, inflater)
        return view
    }

    fun initMVC(view: View) {
        labelChooserSceneController = LabelChooserSceneController(
                scene = sceneFactory.createChooserDialogScene(view, labelThreadListHandler),
                model = labelChooserSceneModel,
                labelDataSourceHandler = labelDataSourceHandler)
        labelChooserSceneController.onStart()
    }
    class Builder {

        fun build(sceneFactory: SceneFactory,
                  labelDataSourceHandler: MailboxActivity.LabelDataSourceHandler): DialogLabelsChooser {
            val dialog = DialogLabelsChooser()
            dialog.labelDataSourceHandler = labelDataSourceHandler
            dialog.sceneFactory = sceneFactory

            return dialog
        }
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

