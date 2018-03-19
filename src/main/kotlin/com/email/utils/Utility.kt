package com.email.utils

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import com.email.R
import com.email.db.AttachmentTypes
import com.email.scenes.emaildetail.ui.AttachmentHistoryPopUp
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

        private fun colorByName(name: String) : Int {
            var color = "0091ff"
            val md5 = md5(name)
            if(md5.length >= 7){
                color = md5.substring(1,7)
            }
            return Color.parseColor("#"+color)
        }


        fun getDrawableAttachmentFromType(type: AttachmentTypes) = when (type) {
            AttachmentTypes.EXCEL ->
                R.drawable.attachment_excel_eliminar

            AttachmentTypes.WORD ->
                R.drawable.attachment_word_eliminar

            AttachmentTypes.PDF ->
                R.drawable.attachment_pdf_eliminar

            AttachmentTypes.PPT ->
                R.drawable.attachment_ppt_eliminar

            AttachmentTypes.IMAGE ->
                R.drawable.attachment_image_eliminar
        }
    }
}