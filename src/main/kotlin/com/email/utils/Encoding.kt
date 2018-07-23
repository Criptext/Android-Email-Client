package com.email.utils

import android.util.Base64

/**
 * Created by gabriel on 3/5/18.
 */

object Encoding {
     fun byteArrayToString(byteArray: ByteArray): String = Base64.encodeToString(byteArray,
              Base64.NO_WRAP)
     fun stringToByteArray(string: String): ByteArray = Base64.decode(string, Base64.NO_WRAP)
}