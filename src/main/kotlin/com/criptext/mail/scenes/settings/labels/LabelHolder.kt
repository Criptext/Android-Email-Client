package com.criptext.mail.scenes.settings.labels

import android.graphics.Color
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper

/**
 * Created by danieltigse on 29/6/18.
 */

class LabelHolder(val view: View) : RecyclerView.ViewHolder(view){

    private val nameView : TextView
    private val checkBoxView : CheckBox
    private val labelColor: ImageView

    fun bindLabel(label: LabelWrapper) {
        nameView.text = label.text
        checkBoxView.isChecked = label.isSelected
        if(label.type == LabelTypes.SYSTEM){
            checkBoxView.isChecked = true
            checkBoxView.isEnabled = false
        }
        else{
            checkBoxView.isEnabled = true
        }
        DrawableCompat.setTint(labelColor.drawable, Color.parseColor("#${label.color}"))
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
