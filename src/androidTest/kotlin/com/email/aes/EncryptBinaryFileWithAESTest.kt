package com.email.aes

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptBinaryFileWithAESTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var aesClient: AESUtil

    @Before
    fun setup() {
        val key = AESUtil.generateSalt()
        val iv = AESUtil.generateSalt()
        aesClient = AESUtil(key.plus(":".plus(iv)))
    }

    @Test
    fun should_correctly_encrypt_and_decrypt_a_byte_array() {

        val constantByteArray = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 127)

        val encryptedBytes = aesClient.encrypt(constantByteArray)
        val decryptedBytes = aesClient.decrypt(encryptedBytes)

        decryptedBytes `shouldEqual` constantByteArray
    }
}