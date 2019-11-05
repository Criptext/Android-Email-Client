package com.criptext.mail.scenes.settings.labels.data

import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.Gravity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.db.LabelTypes
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.scenes.settings.labels.LabelsUIObserver

/**
 * Created by danieltigse on 29/6/18.
 */

class LabelHolder(val view: View) : RecyclerView.ViewHolder(view){

    private val nameView : TextView
    private val checkBoxView : CheckBox
    private val labelColor: ImageView
    private val threeDots: ImageView

    fun bindLabel(label: LabelWrapper) {
        nameView.text = label.text
        checkBoxView.isChecked = label.isSelected
        if(label.type == LabelTypes.SYSTEM){
            checkBoxView.isChecked = true
            checkBoxView.isEnabled = false
            threeDots.visibility = View.GONE
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
        threeDots = view.findViewById(R.id.more) as ImageView
    }

    fun setOnCheckboxClickedListener(onCheckboxClick: () -> Unit) {
        checkBoxView.setOnClickListener {
            onCheckboxClick()
        }
    }

    fun setOnMoreClickedListener(labelsUIObserver: LabelsUIObserver?, label: LabelWrapper){
        threeDots.setOnClickListener { val wrapper = ContextThemeWrapper(view.context, R.style.email_detail_popup_menu)
            val popupMenu = PopupMenu(wrapper, threeDots)
            val popUpLayout = R.menu.label_options_menu
            popupMenu.inflate(popUpLayout)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.delete -> {
                        labelsUIObserver?.onDeleteLabelClicked(label)
                        true
                    }
                    R.id.edit -> {
                        labelsUIObserver?.onEditLabelClicked(label)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}
