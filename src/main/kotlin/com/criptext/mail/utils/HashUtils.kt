package com.criptext.mail.utils

import java.security.MessageDigest


fun String.sha256(digest: String = "UTF8"): String {
    return if(digest == "HEX")
        this.getSha256()
    else
        this.hashWithAlgorithm("SHA-256")
}

private fun String.hashWithAlgorithm(algorithm: String): String {
    val digest = MessageDigest.getInstance(algorithm)
    val bytes = digest.digest(this.toByteArray(Charsets.UTF_8))
    return Encoding.byteArrayToString(bytes)
}

private fun String.getSha256(): String {
    try {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(this.toByteArray())
        return bytesToHex(md.digest())
    } catch (ex: Exception) {
        throw RuntimeException(ex)
    }

}

private fun bytesToHex(bytes: ByteArray): String {
    val hexString = StringBuffer()
    for (i in 0 until bytes.size) {
        val hex = Integer.toHexString(0xff and bytes[i].toInt())
        if (hex.length == 1) hexString.append('0')
        hexString.append(hex)
    }
    return hexString.toString()
}

