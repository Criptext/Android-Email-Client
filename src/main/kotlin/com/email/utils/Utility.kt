package com.email.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.MenuItem
import com.email.R
import com.email.utils.ui.TextDrawable
import java.security.NoSuchAlgorithmException

/**
 * Created by hirobreak on 06/04/17.
 */
class Utility {
    companion object {

        fun addTintInMultiMode(context: Context, item: MenuItem) {
            var drawable : Drawable = item.icon
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.multiModeTint));
            item.setIcon(drawable)
        }
        fun getBitmapFromText(fullName: String, firstLetter: String, width: Int, height: Int): Bitmap {

            val drawable = TextDrawable.builder().buildRound(firstLetter, colorByName(fullName))
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            return bitmap;

        }

        private fun md5(s: String): String {
            try {
                // Create MD5 Hash
                val digest = java.security.MessageDigest.getInstance("MD5")
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuffer()
                for (i in messageDigest.indices)
                    hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
                return hexString.toString()

            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return ""
        }

        private fun colorByName(name: String) : Int {
            var color = "0091ff"
            val md5 = md5(name)
            if(md5.length >= 7){
                color = md5.substring(1,7)
            }
            return Color.parseColor("#"+color)
        }
    }
}