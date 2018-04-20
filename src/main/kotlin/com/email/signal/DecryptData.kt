package com.email.signal

/**
 * Created by danieltigse on 4/16/18.
 */

data class DecryptData(
        val from: String,
        val deviceId: Int,
        val encryptedData: String)