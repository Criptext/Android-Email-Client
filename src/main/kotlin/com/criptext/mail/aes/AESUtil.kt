package com.criptext.mail.aes

import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.file.ChunkFileReader
import org.spongycastle.crypto.PBEParametersGenerator
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter
import java.io.File
import java.security.SecureRandom
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.Cipher
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.OutputStream
import java.io.InputStream








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

        fun generateAesKey():String{
            val aesKey = AESUtil.generateSecureRandomBytesToString()
            val aesIV = AESUtil.generateSecureRandomBytesToString()

            return aesKey.plus(":".plus(aesIV))
        }

        fun generateSecureRandomBytesToString(size: Int = 16): String {
            val random = SecureRandom()
            val bytes = ByteArray(size)
            random.nextBytes(bytes)
            return Encoding.byteArrayToString(bytes)
        }

        fun generateSecureRandomBytes(size: Int = 16): ByteArray {
            val random = SecureRandom()
            val bytes = ByteArray(size)
            random.nextBytes(bytes)
            return bytes
        }

        fun encryptWithPassword(password: String, dataToEncrypt: ByteArray): Triple<String, String, String>{
            val salt = ByteArray(8)
            val srandom = SecureRandom()
            srandom.nextBytes(salt)

            val generator = PKCS5S2ParametersGenerator(SHA256Digest())
            generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password.toCharArray()), salt, 10000)
            val key = generator.generateDerivedMacParameters(128) as KeyParameter

            val skey = SecretKeySpec(key.key, "AES")

            val iv = ByteArray(128 / 8)
            srandom.nextBytes(iv)
            val ivspec = IvParameterSpec(iv)

            val ci = Cipher.getInstance("AES/CBC/PKCS5Padding")
            ci.init(Cipher.ENCRYPT_MODE, skey, ivspec)

            return Triple(Encoding.byteArrayToString(salt), Encoding.byteArrayToString(iv),
                    Encoding.byteArrayToString(ci.doFinal(dataToEncrypt)))
        }

        fun decryptWithPassword(password: String, salt: String, iv: String, dataToDecrypt: ByteArray): String{

            val generator = PKCS5S2ParametersGenerator(SHA256Digest())
            generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password.toCharArray()), Encoding.stringToByteArray(salt), 10000)
            val key = generator.generateDerivedMacParameters(128) as KeyParameter

            val skey = SecretKeySpec(key.key, "AES")

            val ivspec = IvParameterSpec(Encoding.stringToByteArray(iv))

            val ci = Cipher.getInstance("AES/CBC/PKCS5Padding")
            ci.init(Cipher.DECRYPT_MODE, skey, ivspec)

            return Encoding.byteArrayToString(ci.doFinal(dataToDecrypt))
        }

        fun encryptFileByChunks(fileToEncrypt: File): Pair<ByteArray, String> {

            val strKey = generateSecureRandomBytes()
            val skey = SecretKeySpec(strKey, "AES")


            val iv = generateSecureRandomBytes(128 / 8)
            val ivspec = IvParameterSpec(iv)

            val outFile = createTempFile(suffix = ".enc")

            val out = FileOutputStream(outFile)
            out.write(iv)

            val ci = Cipher.getInstance("AES/CBC/PKCS5Padding")
            ci.init(Cipher.ENCRYPT_MODE, skey, ivspec)

            FileInputStream(fileToEncrypt).use { `in` -> processFile(ci, `in`, out) }

            return Pair(strKey, outFile.absolutePath)
        }

        fun decryptFileByChunks(fileToDecrypt: File, strKey: ByteArray): String {
            val `in` = FileInputStream(fileToDecrypt)
            val iv = ByteArray(128 / 8)
            `in`.read(iv)

            val skey = SecretKeySpec(strKey, "AES")
            val ci = Cipher.getInstance("AES/CBC/PKCS5Padding")
            ci.init(Cipher.DECRYPT_MODE, skey, IvParameterSpec(iv))

            val outputFile = createTempFile(suffix = ".ver")

            FileOutputStream(outputFile).use { out -> processFile(ci, `in`, out) }

            return outputFile.absolutePath
        }

        @Throws(javax.crypto.IllegalBlockSizeException::class, javax.crypto.BadPaddingException::class, java.io.IOException::class)
        private fun processFile(ci: Cipher, `in`: InputStream, out: OutputStream) {
            val ibuf = ByteArray(STREAM_BUFFER_SIZE)
            var len = `in`.read(ibuf)
            while (len != -1) {
                val obuf = ci.update(ibuf, 0, len)
                if (obuf != null) out.write(obuf)
                len = `in`.read(ibuf)
            }
            val obuf = ci.doFinal()
            if (obuf != null) out.write(obuf)
        }

        const val STREAM_BUFFER_SIZE = 512
    }
}