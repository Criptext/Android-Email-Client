package com.email.scenes.LabelChooser

import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import android.widget.Button
import com.email.DB.MailboxLocalDB
import com.email.IHostActivity
import com.email.MailboxActivity
import com.email.androidui.SceneFactory
import com.email.scenes.LabelChooser.data.LabelChooserDataSource


/**
 * Created by sebas on 2/1/18.
 */

class DialogLabelsChooser(): DialogFragment(){

    interface DialogLabelsListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    val dialogLabelsListener : DialogLabelsListener = object : DialogLabelsListener{
        override fun onDialogPositiveClick(dialog: DialogFragment) {
            labelChooserSceneController.assignLabels((activity as IHostActivity).getSelectedThreads())
            labelChooserSceneController.clearSelectedLabels()
            (activity as IHostActivity).getMailboxSceneController().changeMode(multiSelectON = false, silent = false)
            dialog.dismiss()
        }


        override fun onDialogNegativeClick(dialog: DialogFragment) {
            dialog.dismiss()
        }

    }
    lateinit var recyclerView : RecyclerView
    lateinit var labelThreadEventListener : LabelThreadAdapter.OnLabelThreadEventListener
    lateinit var sceneFactory: SceneFactory
    private val labelChooserSceneModel : LabelChooserSceneModel = LabelChooserSceneModel()
    private lateinit var labelChooserSceneController: LabelChooserSceneController
    private val labelThreadListHandler : LabelThreadListHandler = LabelThreadListHandler(labelChooserSceneModel)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = initUI(inflater, container)
        initMVC(view)
        return view
    }

    fun initMVC(view: View) {
        val DB : MailboxLocalDB.Default = MailboxLocalDB.Default(activity.applicationContext)
        labelChooserSceneController = LabelChooserSceneController(
                scene = sceneFactory.createChooserDialogScene(view, labelThreadListHandler),
                model = labelChooserSceneModel,
                dataSource = LabelChooserDataSource(DB))

        labelChooserSceneController.onStart()
    }
    fun initUI(inflater: LayoutInflater?, container: ViewGroup?) : View {

        val view = inflater!!.inflate(R.layout.mailbox_labels_chooser, container)
        recyclerView = view.findViewById(R.id.label_recycler) as RecyclerView
        val btn_add = view.findViewById(R.id.label_add) as Button
        val btn_cancel = view.findViewById(R.id.label_cancel) as Button
        assignButtonEvents(btn_add, btn_cancel)
        return view
    }
    fun assignButtonEvents(btn_add: Button, btn_cancel: Button) {
        btn_add.setOnClickListener {
            dialogLabelsListener.onDialogPositiveClick(this)
        }

        btn_cancel.setOnClickListener {
            dialogLabelsListener.onDialogNegativeClick(this)
        }
    }

    class Builder {

        fun build(sceneFactory: SceneFactory): DialogLabelsChooser {
            val dialog = DialogLabelsChooser()
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

