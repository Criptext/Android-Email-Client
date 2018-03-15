package com.email.scenes.emaildetail.ui.labels

import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.email.R
import com.email.db.ColorTypes
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
        when(label.color) {
            ColorTypes.RED -> {
                val drawableBackground = ContextCompat.getDrawable(
                        context,
                        R.drawable.email_detail_label)
                val color = ContextCompat.getColor(context, R.color.red)
                drawableBackground.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                labelView.background = drawableBackground
            }

            ColorTypes.GREEN -> {
                val drawableBackground = ContextCompat.getDrawable(
                        context,
                        R.drawable.email_detail_label)
                val color = ContextCompat.getColor(context, R.color.green)
                drawableBackground.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                labelView.background = drawableBackground
            }

            ColorTypes.BLUE -> {
                val drawableBackground = ContextCompat.getDrawable(
                        context,
                        R.drawable.email_detail_label)
                val color = ContextCompat.getColor(context, R.color.azure)
                drawableBackground.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                labelView.background = drawableBackground
            }

            ColorTypes.YELLOW -> {
                val drawableBackground = ContextCompat.getDrawable(
                        context,
                        R.drawable.email_detail_label)
                val color = ContextCompat.getColor(context, R.color.yellow)
                drawableBackground.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                labelView.background = drawableBackground
            }
        }
    }

    init {
        labelView = view.findViewById(R.id.name)
        layout = view.findViewById(R.id.holder_container_label)
    }
}
