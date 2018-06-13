package com.email.utils

import android.graphics.*
import android.webkit.MimeTypeMap
import com.email.R
import com.email.db.AttachmentTypes
import com.email.utils.ui.TextDrawable
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
                R.drawable.xls

            AttachmentTypes.WORD ->
                R.drawable.word

            AttachmentTypes.PDF ->
                R.drawable.pdf

            AttachmentTypes.PPT ->
                R.drawable.ppt

            AttachmentTypes.IMAGE ->
                R.drawable.img

            AttachmentTypes.DEFAULT ->
                R.drawable.generic
        }

        fun getAttachmentTypeFromPath(filepath: String): AttachmentTypes {
            var type = AttachmentTypes.DEFAULT
            val extension = filepath.split(".").last()
            val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            type = when {
                mimetype.contains("image") -> AttachmentTypes.IMAGE
                mimetype.contains("word") -> AttachmentTypes.WORD
                mimetype.contains("powerpoint") || mimetype.contains("presentation") -> AttachmentTypes.PPT
                mimetype.contains("excel") || mimetype.contains("sheet") -> AttachmentTypes.EXCEL
                mimetype.contains("pdf") -> AttachmentTypes.PDF
                else -> AttachmentTypes.DEFAULT
            }
            return type
        }
    }
}