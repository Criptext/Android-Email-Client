package com.criptext.mail.utils.ui

import android.content.Context
import android.support.v4.widget.DrawerLayout
import android.util.AttributeSet

/**
 * Created by danieltigse on 1/31/18.
 */

class MyDrawerLayout(context: Context, attrs: AttributeSet?) : DrawerLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMeasureSpec2 = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY)

        val heightMeasureSpec2 = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec2, heightMeasureSpec2)
    }
}