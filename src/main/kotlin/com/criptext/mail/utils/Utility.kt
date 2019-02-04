package com.criptext.mail.utils

import android.graphics.*
import androidx.appcompat.app.AppCompatDelegate
import com.criptext.mail.utils.ui.TextDrawable
import java.io.File
import java.util.regex.Pattern
import android.graphics.PorterDuffXfermode
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getHeight
import android.graphics.Rect
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Bitmap



/**
 * Created by hirobreak on 06/04/17.
 */
class Utility {

    companion object {

        fun getCroppedBitmap(bitmap: Bitmap?): Bitmap? {
            if(bitmap == null) return null
            val output = Bitmap.createBitmap(bitmap.width,
                    bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(),
                    (bitmap.width / 2).toFloat(), paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
            //return _bmp;
            return output
        }

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

        fun getBitmapFromFile(file: File): Bitmap{
            val filePath = file.path
            val options = BitmapFactory.Options()
            val bitmap = BitmapFactory.decodeFile(filePath, options)
            return Bitmap.createScaledBitmap(bitmap, 256, 256, false)
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