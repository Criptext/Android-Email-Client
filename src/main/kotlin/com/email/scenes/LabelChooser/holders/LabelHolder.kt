package com.email.scenes.LabelChooser.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.email.R
import com.email.scenes.LabelChooser.data.LabelThread

/**
 * Created by sebas on 2/2/18.
 */

class LabelHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
    override fun onClick(p0: View?) {
    }

    private val nameView : TextView
    private val checkBoxView : CheckBox

    init {
        view.setOnClickListener(this)
    }

    fun bindLabel(labelThread: LabelThread) {
        nameView.text = labelThread.text
        checkBoxView.setChecked(labelThread.isSelected)
    }

    init {
        nameView = view.findViewById(R.id.label_name) as TextView
        checkBoxView = view.findViewById(R.id.label_checkbox) as CheckBox
    }
}
