package com.criptext.mail.signal

/**
 * Created by danieltigse on 4/16/18.
 */

data class SignalEncryptedData(
        val encryptedB64: String,
        val type: Type) {

    enum class Type { normal, preKey;
        fun toInt(): Int =
            when (this) {
                normal -> 1
                preKey -> 3
        }

        companion object {
            fun fromInt(i: Int): Type? =
                when (i) {
                    1 -> normal
                    3 -> preKey
                    else -> null
                }
        }
    }

}

