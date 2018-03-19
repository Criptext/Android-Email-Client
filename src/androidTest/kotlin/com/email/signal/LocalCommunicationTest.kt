package com.email.signal

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.splash.SplashActivity
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
    val mActivityRule = ActivityTestRule(SplashActivity::class.java)
    private val generator = SignalKeyGenerator.Default()

    @Test
    @Throws(InterruptedException::class)
    fun should_be_able_to_exchange_e2e_messages_in_memory_with_signal() {
        val alice = InMemoryUser(generator, "alice", 1).setup()
        val bob = InMemoryUser(generator, "bob", 1).setup()

        val keyBundleFromBob = bob.fetchAPreKeyBundle()
        alice.buildSession(keyBundleFromBob)

        val originalTextFromAlice = "Hello Bob! How are you! I'm using Criptext. This is my 1st e-mail."
        val textEncryptedByAlice = alice.encrypt("bob", 1, originalTextFromAlice)

        val textDecryptedByBob = bob.decrypt("alice", 1, textEncryptedByAlice)
        textDecryptedByBob shouldEqual originalTextFromAlice
    }




}