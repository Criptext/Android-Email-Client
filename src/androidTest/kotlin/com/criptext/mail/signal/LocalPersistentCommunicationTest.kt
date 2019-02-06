package com.criptext.mail.signal

import androidx.test.rule.ActivityTestRule
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.androidtest.TestSharedPrefs
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.splash.SplashActivity
import com.criptext.mail.utils.DeviceUtils
import org.amshove.kluent.shouldEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Created by gabriel on 3/17/18.
 */

class LocalPersistentCommunicationTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)
    private lateinit var storage: KeyValueStorage
    private lateinit var db: TestDatabase
    private lateinit var signUpDao: SignUpDao


    @Before
    fun setup() {
        storage = TestSharedPrefs(mActivityRule.activity)
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        signUpDao = db.signUpDao()
    }



    private fun newPersistedUser(recipientId: String, deviceId: Int): InDBUser {
        return InDBUser(db, storage, signUpDao, keyGenerator, recipientId, deviceId)
    }

    @Test
    fun should_create_user_in_db_and_send_e2e_message_to_inmemory_user() {
        val alice = newPersistedUser("alice", 1).setup()
        val bob = InMemoryUser(keyGenerator, "bob", 1).setup()
        

        val keyBundleFromBob = bob.fetchAPreKeyBundle(1)
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

        val keyBundleFromAlice = alice.fetchAPreKeyBundle(1)
        bob.buildSession(keyBundleFromAlice)

        val originalTextFromBob = "Hello Alice! How are you! I'm in ur RAM. This is my 1st e-mail."
        val textEncryptedByBob = bob.encrypt("alice", 1, originalTextFromBob)

        val textDecryptedByAlice = alice.decrypt("bob", 1, textEncryptedByBob)
        textDecryptedByAlice shouldEqual originalTextFromBob
    }

}