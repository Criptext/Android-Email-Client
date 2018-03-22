package com.email.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.*
import com.email.BaseActivity
import com.email.R
import com.email.db.AttachmentTypes
import com.email.scenes.WebViewActivity
import com.email.utils.ui.TextDrawable
import pub.devrel.easypermissions.EasyPermissions
import java.security.NoSuchAlgorithmException
import java.util.regex.Pattern

/**
 * Created by hirobreak on 06/04/17.
 */
class Utility {

    companion object {

        fun hasSDPermissionsWeb(ctx : Context) : Boolean{
            val readSDPermission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (EasyPermissions.hasPermissions(ctx as WebViewActivity, readSDPermission)) {
                return true
            }

            return false
        }

        fun openUrl(context: Context, url: String){
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
            (context as BaseActivity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }

        fun changedHeaderHtml(htmlText: String): String {

            val head = "<head><meta name=\"viewport\" content=\"width=device-width\"></head>"
            val closedTag = "</body></html>"
            val changeFontHtml = head + htmlText + collapseScript() + closedTag
            return changeFontHtml
        }

        fun collapseScript() : String{
            val imageUri = "file:///android_asset/showmore.png"
            val sb = StringBuilder()
            sb.append("<script>")

            sb.append("var tds = document.getElementsByTagName(\"td\");")
            sb.append("var tables = document.getElementsByTagName(\"table\");")

            sb.append("for (i = 0; i < tds.length; i++) {")
            sb.append("var td = tds[i];")
            sb.append("if(td.width > window.innerWidth){")
            sb.append("td.width = window.innerWidth;")
            sb.append("};")
            sb.append("};")

            sb.append("for (i = 0; i < tables.length; i++) {")
            sb.append("var table = tables[i];")
            sb.append("if(table.width > window.innerWidth){")
            sb.append("table.width = \"100%\";")
            sb.append("};")
            sb.append("};")

            sb.append("var urlRegex = /(=\")?(http(s)?:\\/\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)(<\\/a>)?/g;")
            sb.append("document.documentElement.outerHTML.replace(urlRegex, function(url) {")
            sb.append("if(url.indexOf('=\"') > -1 || url.indexOf('.length') > -1 || url.indexOf('.push') > -1 || url.indexOf('.slice.call') > -1){")
            sb.append("return url;")
            sb.append("};")
            sb.append("var trueUrl = url;")
            sb.append("if(trueUrl.indexOf('http') == -1){")
            sb.append("trueUrl = \"https://\"+url;")
            sb.append("};")
            sb.append("return '<a href=\"' + trueUrl + '\" target=\"_blank\">' + url + '</a>';")
            sb.append("});")

            sb.append("var replybody = document.getElementsByTagName(\"blockquote\")[0];")
            sb.append("var newNode = document.createElement(\"img\");")
            sb.append("newNode.src = \"${imageUri}\";")
            sb.append("newNode.width = 30;")
            sb.append("newNode.style.paddingTop = \"10px\";")
            sb.append("newNode.style.paddingBottom = \"10px\";")
            sb.append("replybody.style.display = \"none\";")
            sb.append("replybody.parentElement.insertBefore(newNode, replybody);")
            sb.append("newNode.addEventListener(\"click\", function(){ if(replybody.style.display == \"block\"){ " +
                    "replybody.style.display = \"none\";} else {" +
                    "replybody.style.display = \"block\";} CriptextSecureEmail.toggleButton();});")
            sb.append("</script>")

            return sb.toString()
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