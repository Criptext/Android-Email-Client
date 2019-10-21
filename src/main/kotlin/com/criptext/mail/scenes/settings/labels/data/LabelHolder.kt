package com.criptext.mail.scenes.settings.labels.data

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
    private val trashImage: ImageView

    fun bindLabel(label: LabelWrapper) {
        nameView.text = label.text
        checkBoxView.isChecked = label.isSelected
        if(label.type == LabelTypes.SYSTEM){
            checkBoxView.isChecked = true
            checkBoxView.isEnabled = false
            trashImage.visibility = View.GONE
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
        trashImage = view.findViewById(R.id.label_trash) as ImageView
    }

    fun setOnCheckboxClickedListener(onCheckboxClick: () -> Unit) {
        checkBoxView.setOnClickListener {
            onCheckboxClick()
        }
    }

    fun setOnTrashClickedListener(onTrashClicked: () -> Unit){
        trashImage.setOnClickListener {
            onTrashClicked()
        }
    }
}
