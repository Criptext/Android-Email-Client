package com.signaltest.crypto

import android.util.Base64


/**
 * Created by gabriel on 11/12/17.
 */

object Encoding {
    fun byteArrayToString(byteArray: ByteArray): String  = Base64.encodeToString(byteArray,
            Base64.NO_WRAP)

    fun stringToByteArray(string: String): ByteArray = Base64.decode(string, Base64.NO_WRAP)
}