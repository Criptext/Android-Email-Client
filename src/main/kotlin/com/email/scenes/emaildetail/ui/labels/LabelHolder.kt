package com.email.scenes.emaildetail.ui.labels

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.db.models.Label

/**
 * Created by sebas on 3/14/18.
 */

class LabelHolder(val view: View): RecyclerView.ViewHolder(view) {
    private val context = view.context

    private val layout: LinearLayout
    private val labelView: TextView

    fun bindLabel(label: Label){
        labelView.text = label.text
    }

    init {
        labelView = view.findViewById(R.id.name)
        layout = view.findViewById(R.id.holder_container_label)
    }
}
