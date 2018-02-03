package com.email.scenes.LabelChooser

import android.app.AlertDialog
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.app.Dialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.email.R
import android.widget.Button
import com.email.MailboxActivity
import com.email.androidui.labelthread.LabelThreadRecyclerView
import android.view.WindowManager
import android.widget.EditText




/**
 * Created by sebas on 2/1/18.
 */

class DialogLabelsChooser(): DialogFragment(){

    interface DialogLabelsListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    lateinit var dialogLabelsListener : DialogLabelsListener
    lateinit var labelChooserView : LabelChooserScene.LabelChooserView
    lateinit var btn_add : Button
    lateinit var btn_cancel : Button
    lateinit var recyclerView : RecyclerView
    lateinit var labelThreadEventListener : LabelThreadAdapter.OnLabelThreadEventListener

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.mailbox_labels_chooser, container)

        recyclerView = view.findViewById(R.id.label_recycler) as RecyclerView
        btn_add = view.findViewById(R.id.label_add) as Button
        btn_cancel = view.findViewById(R.id.label_cancel) as Button
        labelChooserView.initRecyclerView(labelThreadEventListener)
        assignButtonEvents()
        return view
    }

    fun assignButtonEvents() {
        btn_add.setOnClickListener {
            dialogLabelsListener.onDialogPositiveClick(this)
        }

        btn_cancel.setOnClickListener {
            dialogLabelsListener.onDialogNegativeClick(this)
        }
    }
}

