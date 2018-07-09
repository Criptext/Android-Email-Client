package com.email.scenes.mailbox.ui

import android.graphics.Color
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.email.R
import com.email.scenes.label_chooser.data.LabelWrapper

/**
 * Created by danieltigse on 29/6/18.
 */

class LabelHolder(val view: View) : RecyclerView.ViewHolder(view){

    private val nameView : TextView
    private val checkBoxView : CheckBox
    private val labelColor: ImageView
    private val rootView: View
    private val viewSeparator: View

    fun bindLabel(label: LabelWrapper) {
        nameView.text = label.text
        DrawableCompat.setTint(labelColor.drawable, Color.parseColor("#${label.color}"))
    }

    init {
        nameView = view.findViewById(R.id.label_name) as TextView
        checkBoxView = view.findViewById(R.id.label_checkbox) as CheckBox
        labelColor = view.findViewById(R.id.label_color)
        rootView = view.findViewById(R.id.rootView)
        viewSeparator = view.findViewById(R.id.viewSeparator)
        checkBoxView.visibility = View.INVISIBLE
        viewSeparator.visibility = View.GONE
    }

    fun setOnClickedListener(onClick: () -> Unit) {
        rootView.setOnClickListener {
            onClick()
        }
    }
}
