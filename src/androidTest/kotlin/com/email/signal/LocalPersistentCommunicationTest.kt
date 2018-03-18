package com.email.signal

import android.support.test.rule.ActivityTestRule
import com.email.db.SignUpLocalDB
import com.email.db.TestDatabase
import com.email.splash.SplashActivity
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by gabriel on 3/17/18.
 */

class LocalPersistentCommunicationTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(SplashActivity::class.java)

    private val keyGenerator = SignalKeyGenerator.Default()
    private lateinit var db:  TestDatabase
    private lateinit var signUpDB: SignUpLocalDB


    @Before
    fun setup() {
        db = TestDatabase.getAppDatabase(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        signUpDB = SignUpLocalDB.Default(db)
    }

    private fun newPersistedUser(recipientId: String, deviceId: Int): InDBUser {
        return InDBUser(db, signUpDB, keyGenerator, recipientId, deviceId)
    }

    @Test
    fun should_create_user_in_db_and_send_e2e_message_to_inmemory_user() {
        val alice = newPersistedUser("alice", 1).setup()
        val bob = InMemoryUser(keyGenerator, "bob", 1).setup()

        val keyBundleFromBob = bob.fetchAPreKeyBundle()
        alice.buildSession(keyBundleFromBob)

        val originalTextFromAlice = "Hello Bob! How are you! I'm persisting my data with Room. This is my 1st e-mail."
        val textEncryptedByAlice = alice.encrypt("bob", 1, originalTextFromAlice)

        val textDecryptedByBob = bob.decrypt("alice", 1, textEncryptedByAlice)
        textDecryptedByBob shouldEqual originalTextFromAlice

    }

    @Test
    fun should_create_user_in_db_and_receive_e2e_message_from_inmemory_user() {
        val alice = newPersistedUser("alice", 1).setup()
        val bob = InMemoryUser(keyGenerator, "bob", 1).setup()

        val keyBundleFromAlice = alice.fetchAPreKeyBundle()
        bob.buildSession(keyBundleFromAlice)

        val originalTextFromBob = "Hello Alice! How are you! I'm in ur RAM. This is my 1st e-mail."
        val textEncryptedByBob = bob.encrypt("alice", 1, originalTextFromBob)

        val textDecryptedByAlice = alice.decrypt("bob", 1, textEncryptedByBob)
        textDecryptedByAlice shouldEqual originalTextFromBob
    }

}