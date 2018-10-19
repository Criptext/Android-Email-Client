package com.criptext.mail.utils.ui

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import com.otaliastudios.zoom.ZoomEngine
import com.otaliastudios.zoom.ZoomLayout

class MyZoomLayout: ZoomLayout {

    var mListener: ZoomUpdateListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onUpdate(helper: ZoomEngine?, matrix: Matrix?) {
        super.onUpdate(helper, matrix)
        mListener?.onUpdate(helper, matrix)
    }

    interface ZoomUpdateListener{
        fun onUpdate(helper: ZoomEngine?, matrix: Matrix?)
    }
}