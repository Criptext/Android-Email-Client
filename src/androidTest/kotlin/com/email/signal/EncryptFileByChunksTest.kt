package com.email.signal

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.email.androidtest.TestActivity
import com.email.androidtest.TestDatabase
import com.email.androidtest.TestSharedPrefs
import com.email.db.MailboxLocalDB
import com.email.db.models.Contact
import com.email.utils.FileDownloader
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EncryptFileByChunksTest {

    @get:Rule
    val mActivityRule = ActivityTestRule(TestActivity::class.java)

    private lateinit var db: TestDatabase
    private lateinit var mailboxLocalDB: MailboxLocalDB
    private lateinit var signalClient: SignalClient
    private lateinit var tester: TestUser

    private val keyGenerator = SignalKeyGenerator.Default()
    private val bobContact = Contact(email = "bob@jigl.com", name = "Bob", id = 1)
    private val joeContact = Contact(email = "joe@jigl.com", name = "Joe", id = 2)

    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.contactDao().insertIgnoringConflicts(bobContact)
        db.contactDao().insertIgnoringConflicts(joeContact)

        mailboxLocalDB = MailboxLocalDB.Default(db)
        signalClient = SignalClient.Default(store = SignalStoreCriptext(db))

        // create tester user so that signal store is initialized.
        val storage = TestSharedPrefs(mActivityRule.activity)
        tester = InDBUser(db = db, storage = storage, generator = keyGenerator,
                recipientId = "tester", deviceId = 1).setup()
    }

    @Test
    fun should_correctly_encrypt_a_file_by_chunks_and_save_it_on_a_temp_file() {

        val constantFile = createTempFile()
        val constantListOfFileEntries: List<String>

        val decryptedList: List<String>

        try {
            FileDownloader.download(EncryptFileByChunksTest.testTxtFileURL, constantFile)
            val bob = InMemoryUser(keyGenerator, "bob", 1).setup()

            constantListOfFileEntries = constantFile.readLines()

            val keyBundleFromBob = bob.fetchAPreKeyBundle()
            tester.buildSession(keyBundleFromBob)


            val encryptedFile = signalClient.encryptFileByChunks(constantFile, "bob", 1, 512000)

            val unencryptedFile = File(bob.decryptFileByChunks(File(encryptedFile),"tester", 1))
            
            decryptedList = unencryptedFile.readLines()


        } finally {
            constantFile.delete()
        }

        decryptedList `shouldEqual` constantListOfFileEntries
    }

    companion object {
        private val testTxtFileURL = "https://cdn.criptext.com/Email/images/big.txt"
    }
}