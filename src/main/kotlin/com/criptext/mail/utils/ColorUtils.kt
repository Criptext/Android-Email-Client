package com.criptext.mail.utils

import android.graphics.Color
import java.security.NoSuchAlgorithmException

class ColorUtils{
    companion object {

        fun colorStringByName(name: String) : String {
            var color = "0091ff"
            val md5 = md5(name)
            if(md5.length >= 7){
                color = md5.substring(1,7)
            }
            return color
        }

        fun colorByName(name: String) : Int {
            var color = "0091ff"
            val md5 = md5(name)
            if(md5.length >= 7){
                color = md5.substring(1,7)
            }
            return Color.parseColor("#"+color)
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


    }
}