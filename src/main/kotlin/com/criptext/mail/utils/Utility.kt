package com.criptext.mail.utils

import android.graphics.*
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.utils.ui.TextDrawable
import java.util.regex.Pattern

/**
 * Created by hirobreak on 06/04/17.
 */
class Utility {

    companion object {

        fun getBitmapFromText(fullName: String, width: Int, height: Int): Bitmap {
            val nameInitials = getAvatarLetters(fullName)
            val drawable = TextDrawable.builder(Color.WHITE).buildRound(nameInitials, ColorUtils.colorByName(fullName))
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            return bitmap

        }

        fun getBitmapFromNumber(number: Int, width: Int, height: Int, bgColor: Int, textColor: Int): Bitmap {
            val nameInitials = number.toString()
            val drawable = TextDrawable.builder(textColor)
                    .buildRound(nameInitials, bgColor)
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            return bitmap

        }

        private fun getAvatarLetters(fullName: String): String{
            val cleanedName = fullName.trim()
            val firstLetter = cleanedName.toCharArray()[0].toString().toUpperCase()
            val secondLetter = if(cleanedName.contains(" "))
                cleanedName[cleanedName.lastIndexOf(" ") + 1].toString().toUpperCase()
            else
                ""
            return firstLetter.plus(secondLetter)
        }

    }
}