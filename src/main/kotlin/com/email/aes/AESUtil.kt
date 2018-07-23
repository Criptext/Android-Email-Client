package com.email.aes

import com.email.utils.Encoding
import java.security.SecureRandom
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.*


class AESUtil(keyAndIV: String) {
    private lateinit var cipherENC: Cipher
    private lateinit var cipherDEC: Cipher

    private lateinit var strKey: String
    private lateinit var strIV: String

    init {
        if (keyAndIV.length < 3 || keyAndIV.indexOf(':') == -1)
            throw IllegalArgumentException("keyAndIV string must have this pattern <key>:<iv>")
        try {
            val array = keyAndIV.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            strKey = array[0]
            strIV = array[1]
            val secret = SecretKeySpec(Encoding.stringToByteArray(strKey), "AES")

            cipherENC = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipherDEC = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val params = cipherENC.parameters
            cipherENC.init(Cipher.ENCRYPT_MODE, secret, IvParameterSpec(Encoding.stringToByteArray(strIV)))
            cipherDEC.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(Encoding.stringToByteArray(strIV)))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun encrypt(byteArray: ByteArray): ByteArray {
        return cipherENC.doFinal(byteArray)
    }

    fun decrypt(encryptedBytes: ByteArray): ByteArray {
        return cipherDEC.doFinal(encryptedBytes)
    }

    companion object {
        fun generateSalt(size: Int = 16): String {
            val random = SecureRandom()
            val bytes = ByteArray(size)
            random.nextBytes(bytes)
            return Encoding.byteArrayToString(bytes)
        }
    }
}