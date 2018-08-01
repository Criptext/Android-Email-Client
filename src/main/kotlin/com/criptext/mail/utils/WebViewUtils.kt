package com.criptext.mail.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.scenes.WebViewActivity
import pub.devrel.easypermissions.EasyPermissions

class WebViewUtils {

    companion object {

        const val imageUri = "file:///android_asset/showmore.png"

        fun collapseScript() : String{
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

            sb.append("var replybody = document.getElementById(\"criptext_quote\") || document.getElementsByTagName(\"blockquote\")[0];")
            sb.append("var newNode = document.createElement(\"img\");")
            sb.append("newNode.src = \"$imageUri\";")
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

        fun openUrl(context: Context, url: String){
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("url", url)
            context.startActivity(intent)
            (context as BaseActivity).overridePendingTransition(R.anim.slide_in_up, R.anim.stay)
        }

        fun hasSDPermissionsWeb(ctx : Context) : Boolean{
            val readSDPermission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (EasyPermissions.hasPermissions(ctx as WebViewActivity, readSDPermission)) {
                return true
            }

            return false
        }

    }

}