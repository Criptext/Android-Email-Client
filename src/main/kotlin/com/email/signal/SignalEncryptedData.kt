package com.email.signal

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
    }
}

