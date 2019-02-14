package com.criptext.mail.aes

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.mocks.MockLinkDeviceFile
import com.criptext.mail.scenes.emaildetail.data.DownloadAttachmentWorkerTest
import com.criptext.mail.utils.Encoding
import com.criptext.mail.utils.file.AndroidFs
import org.amshove.kluent.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.jvm.java

@RunWith(AndroidJUnit4::class)
class EncryptBinaryFileWithAESTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var aesClient: AESUtil
    private lateinit var constantFile: File

    @Before
    fun setup() {
        val key = AESUtil.generateSecureRandomBytesToString()
        val iv = AESUtil.generateSecureRandomBytesToString()
        aesClient = AESUtil(key.plus(":".plus(iv)))
    }

    @Test
    fun should_correctly_generate_aes_key() {

        val aesKey = AESUtil.generateAesKey()
        val aesTestClient = AESUtil(aesKey)

        aesTestClient.shouldBeInstanceOf(AESUtil::class.java)

        aesKey.indexOf(':') shouldNotBe -1
        val array = aesKey.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val strKey = Encoding.stringToByteArray(array[0])
        val strIV = Encoding.stringToByteArray(array[1])

        strKey.size shouldBe 16
        strIV.size shouldBe 16
    }

    @Test
    fun should_correctly_encrypt_and_decrypt_with_password() {

        val constantByteArray = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 127)
        val password = "123456"

        val encryptedData = AESUtil.encryptWithPassword(password, constantByteArray)
        val decryptedData = AESUtil.decryptWithPassword(
                password = password,
                salt = encryptedData.first, iv = encryptedData.second,
                dataToDecrypt = Encoding.stringToByteArray(encryptedData.third))
        Encoding.stringToByteArray(decryptedData) shouldEqual constantByteArray

    }

    @Test
    fun should_correctly_encrypt_and_decrypt_a_byte_array() {

        val constantByteArray = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 127)

        val encryptedBytes = aesClient.encrypt(constantByteArray)
        val decryptedBytes = aesClient.decrypt(encryptedBytes)

        decryptedBytes shouldEqual constantByteArray
    }

    @Test
    fun should_correctly_encrypt_and_decrypt_a_file_by_chunks() {

        constantFile = MockLinkDeviceFile.getMockedFile()
        val constantListOfFileEntries = constantFile.readLines()
        val decryptedList: List<String>



        val encryptedPair = AESUtil.encryptFileByChunks(constantFile)

        val encryptedFile = File(encryptedPair.second)


        val decryptedFile = AESUtil.decryptFileByChunks(encryptedFile, encryptedPair.first)

        decryptedList = File(decryptedFile).readLines()

        decryptedList shouldEqual constantListOfFileEntries


    }

    @After
    fun teardown(){
        try {
            constantFile.delete()
        }catch (ex: Exception){

        }
    }

    companion object {
        private val testTxtFileURL = "https://cdn.criptext.com/Email/images/big.txt"
        private val testBinaryFileName = "cryptext_data"
    }
}