package com.criptext.mail.utils.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.criptext.mail.R
import com.criptext.mail.utils.getColorFromAttr
import io.github.inflationx.calligraphy3.TypefaceUtils

class ItemDecorator(private val context: Context,
                    private val totalNewFeeds: Int)
    : RecyclerView.ItemDecoration(){

    private val groupSpacing = context.resources.getDimension(R.dimen.indicator_margin_top)

    private val paint = Paint()

    init {
        paint.textSize = context.resources.getDimension(R.dimen.drawer_text_item)
        paint.color = context.getColorFromAttr(R.attr.criptextPrimaryTextColor)
        paint.typeface = TypefaceUtils.load(context.resources.assets, "fonts/NunitoSans-Regular.ttf")
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(view)
            if(position == 0){
                drawText(c, view, context.resources.getString(R.string.feed_new))
            }
            else if (position % totalNewFeeds == 0 && position <= totalNewFeeds) {
                drawText(c, view, context.resources.getString(R.string.feed_older))
            }
        }
    }

    private fun drawText(c: Canvas, view: View, text: String){
        c.drawText(text, view.left.toFloat()
                + context.resources.getDimension(R.dimen.indicator_margin_left),
                view.top.toFloat() - groupSpacing/2 , paint)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position % totalNewFeeds == 0 && position <= totalNewFeeds) {
            outRect.set(0, groupSpacing.toInt() * 2, 0, 0)
        }
    }

}