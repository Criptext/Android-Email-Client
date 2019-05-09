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
import android.R.attr.maxWidth
import android.R.attr.maxHeight
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth





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
            var width = bitmap.width
            var height = bitmap.height

            when {
                width > height -> {
                    // landscape
                    val ratio = width.toFloat() / 256
                    width = 256
                    height = (height / ratio).toInt()
                }
                height > width -> {
                    // portrait
                    val ratio = height.toFloat() / 256
                    height = 256
                    width = (width / ratio).toInt()
                }
                else -> {
                    // square
                    height = 256
                    width = 256
                }
            }

            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }

        private fun getAvatarLetters(fullName: String): String{
            val cleanedName = fullName.trim()
            if(cleanedName.isEmpty()) return ""
            val firstLetter = cleanedName.toCharArray()[0].toString().toUpperCase()
            val secondLetter = if(cleanedName.contains(" "))
                cleanedName[cleanedName.lastIndexOf(" ") + 1].toString().toUpperCase()
            else
                ""
            return firstLetter.plus(secondLetter)
        }

        fun humanReadableByteCount(bytes: Long, si: Boolean): String {
            val unit = if (si) 1000 else 1024
            if (bytes < unit) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
            val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
            return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }

    }
}