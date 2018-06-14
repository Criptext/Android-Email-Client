package com.email.scenes.emaildetail.ui.labels

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.db.models.Label
import com.email.db.typeConverters.LabelTextConverter

/**
 * Created by sebas on 3/14/18.
 */

class LabelHolder(val view: View): RecyclerView.ViewHolder(view) {
    private val context = view.context

    private val layout: LinearLayout
    private val labelView: TextView

    fun bindLabel(label: Label){
        labelView.text = LabelTextConverter().
                parseLabelTextType(label.text)
        setDrawableBackground(Color.parseColor("#${label.color}"))
    }

    private fun setDrawableBackground(color: Int){
        val drawableBackground = ContextCompat.getDrawable(
                context,
                R.drawable.email_detail_label)
        drawableBackground?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        labelView.background = drawableBackground
    }

    init {
        labelView = view.findViewById(R.id.name)
        layout = view.findViewById(R.id.holder_container_label)
    }
}
