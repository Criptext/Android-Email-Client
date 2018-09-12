package com.criptext.mail.utils

import android.graphics.*
import com.criptext.mail.utils.ui.TextDrawable
import java.util.regex.Pattern

/**
 * Created by hirobreak on 06/04/17.
 */
class Utility {

    companion object {

        fun getBitmapFromText(fullName: String, width: Int, height: Int): Bitmap {
            val nameInitials = getAvatarLetters(fullName)
            val drawable = TextDrawable.builder().buildRound(nameInitials, ColorUtils.colorByName(fullName))
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            return bitmap;

        }

        private fun getAvatarLetters(fullName: String): String{
            val firstLetter = fullName.toCharArray()[0].toString()
            val secondLetter = if(fullName.contains(" "))
                fullName[fullName.indexOf(" ") + 1].toString()
            else
                ""
            return firstLetter.plus(secondLetter)
        }

    }
}