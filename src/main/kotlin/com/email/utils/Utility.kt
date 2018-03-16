package com.email.utils

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.email.R
import com.email.utils.ui.TextDrawable
import com.michael.easydialog.EasyDialog
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern

/**
 * Created by hirobreak on 06/04/17.
 */
class Utility {

    companion object {

        fun getBitmapFromText(fullName: String, firstLetter: String, width: Int, height: Int): Bitmap {

            val drawable = TextDrawable.builder().buildRound(firstLetter, colorByName(fullName))
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, width, height)
            drawable.draw(canvas)

            return bitmap;

        }

        fun isEmailValid(email: String): Boolean {
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(email)
            return matcher.matches()
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

        private fun shouldPopUpRenderBottom(context: Context, dialog: EasyDialog, anchorView: View) : Boolean {
            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)
            val anchorLocationY = location[1]
            val displayMetrics = DisplayMetrics()
            val wm = context.getSystemService(Context.WINDOW_SERVICE)
                    as WindowManager
            wm.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels

            return anchorLocationY> screenHeight / 2
        }

        fun createPopUpWindow(
                context: Context,
                anchorView: View,
                contentView: View) {
            val popup = EasyDialog(context)
                    .setLayout(contentView)
                    .setBackgroundColor(
                            ContextCompat.getColor(
                                    context, R.color.white))
                    .setLocationByAttachedView(anchorView)
                    .setAnimationAlphaShow(200, 0.3f, 1.0f)
                    .setAnimationAlphaDismiss(200, 1.0f, 0.0f)
                    .setOutsideColor(R.color.azure)
                    .setTouchOutsideDismiss(true)
                    .setMatchParent(false)
                    .setMarginLeftAndRight(24, 24)

            val shouldPopUpRenderBottom = shouldPopUpRenderBottom(
                    context, popup, anchorView)

            if(shouldPopUpRenderBottom)  {
                popup.setGravity(EasyDialog.GRAVITY_TOP)
            } else {
                popup.setGravity(EasyDialog.GRAVITY_BOTTOM)
            }

            popup.show()
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