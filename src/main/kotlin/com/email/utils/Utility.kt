package com.email.utils

import android.graphics.*
import com.email.utils.ui.TextDrawable
import java.util.regex.Pattern

/**
 * Created by hirobreak on 06/04/17.
 */
class Utility {

    companion object {

        fun getBitmapFromText(fullName: String, firstLetter: String, width: Int, height: Int): Bitmap {

            val drawable = TextDrawable.builder().buildRound(firstLetter, ColorUtils.colorByName(fullName))
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            return bitmap;

        }

    }
}