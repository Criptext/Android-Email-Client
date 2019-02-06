package com.criptext.mail.signal

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import android.util.Log
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.splash.SplashActivity
import com.criptext.mail.utils.DeviceUtils
import org.amshove.kluent.shouldEqual
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by gabriel on 3/16/18.
 */
@RunWith(AndroidJUnit4::class)
class LocalCommunicationTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)
    private val generator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)

    @Test
    @Throws(InterruptedException::class)
    fun should_be_able_to_receive_50_e2e_messages_in_a_row_in_memory_with_signal() {
        val alice = InMemoryUser(generator, "alice", 1).setup()
        val bob = InMemoryUser(generator, "bob", 1).setup()

        val keyBundleFromBob = bob.fetchAPreKeyBundle(1)
        alice.buildSession(keyBundleFromBob)

        for (i in 1..50) {
            val originalTextFromAlice = "Hello Bob! How are you! I'm using Criptext. This is my e-mail #$i."
            val textEncryptedByAlice = alice.encrypt("bob", 1, originalTextFromAlice)

            val textDecryptedByBob = bob.decrypt("alice", 1, textEncryptedByAlice)
            textDecryptedByBob shouldEqual originalTextFromAlice
        }

        val originalReplyFromBob = "Hello Alice! I received all your 50 messages."
        val replyEncryptedByBob = bob.encrypt("alice", 1, originalReplyFromBob)

        val replyDecryptedByAlice = alice.decrypt("bob", 1, replyEncryptedByBob)
        replyDecryptedByAlice shouldEqual originalReplyFromBob
    }


    @Test
    @Throws(InterruptedException::class)
    fun should_be_able_to_exchange_50_e2e_messages_in_memory_with_signal() {
        val alice = InMemoryUser(generator, "alice", 1).setup()
        val bob = InMemoryUser(generator, "bob", 1).setup()

        val keyBundleFromBob = bob.fetchAPreKeyBundle(1)
        alice.buildSession(keyBundleFromBob)

        for (i in 1..50) {
            val originalTextFromAlice = "Hello Bob! How are you! I'm using Criptext. This is my 1st e-mail."
            val textEncryptedByAlice = alice.encrypt("bob", 1, originalTextFromAlice)

            val textDecryptedByBob = bob.decrypt("alice", 1, textEncryptedByAlice)
            textDecryptedByBob shouldEqual originalTextFromAlice

            val originalReplyFromBob = "Hello Alice! I'm fine. I'm using Criptext too, this is my reply #$i."
            val replyEncryptedByBob = bob.encrypt("alice", 1, originalReplyFromBob)

            val replyDecryptedByAlice = alice.decrypt("bob", 1, replyEncryptedByBob)
            replyDecryptedByAlice shouldEqual originalReplyFromBob
        }

    }

}