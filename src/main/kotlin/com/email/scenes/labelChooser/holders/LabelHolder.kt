package com.email.scenes.labelChooser.holders

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.email.R
import com.email.scenes.labelChooser.data.LabelWrapper

/**
 * Created by sebas on 2/2/18.
 */

class LabelHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
    override fun onClick(p0: View?) {
    }

    private val nameView : TextView
    private val checkBoxView : CheckBox
    private val labelColor: ImageView

    init {
        view.setOnClickListener(this)
    }

    fun bindLabel(labelThread: LabelWrapper) {
        nameView.text = labelThread.text
        checkBoxView.isChecked = labelThread.isSelected
        DrawableCompat.setTint(labelColor.drawable, Color.parseColor("#${labelThread.color}"))
    }

    init {
        nameView = view.findViewById(R.id.label_name) as TextView
        checkBoxView = view.findViewById(R.id.label_checkbox) as CheckBox
        labelColor = view.findViewById(R.id.label_color)
    }

    fun setOnCheckboxClickedListener(onCheckboxClick: () -> Unit) {
            checkBoxView.setOnClickListener {
                onCheckboxClick()
            }
    }
}
