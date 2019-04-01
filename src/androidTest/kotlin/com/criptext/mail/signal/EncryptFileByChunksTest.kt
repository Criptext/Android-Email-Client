package com.criptext.mail.signal

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.criptext.mail.androidtest.TestActivity
import com.criptext.mail.androidtest.TestDatabase
import com.criptext.mail.androidtest.TestSharedPrefs
import com.criptext.mail.db.MailboxLocalDB
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.FileDownloader
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
    private lateinit var tester: DummyUser

    private val keyGenerator = SignalKeyGenerator.Default(DeviceUtils.DeviceType.Android)

    private val bobContact = Contact(email = "bob@criptext.com", name = "Bob", id = 1,
            score = 0, isTrusted = false)
    private val joeContact = Contact(email = "joe@criptext.com", name = "Joe", id = 2,
            score = 0, isTrusted = false)
    private var activeAccount = ActiveAccount(name = "Tester", recipientId = "tester",
            deviceId = 1, jwt = "__JWTOKEN__", signature = "", refreshToken = "", id = 1)


    @Before
    fun setup() {
        db = TestDatabase.getInstance(mActivityRule.activity)
        db.resetDao().deleteAllData(1)
        db.contactDao().insertIgnoringConflicts(bobContact)
        db.contactDao().insertIgnoringConflicts(joeContact)

        mailboxLocalDB = MailboxLocalDB.Default(db, mActivityRule.activity.filesDir)
        signalClient = SignalClient.Default(store = SignalStoreCriptext(db))

        // create tester user so that signal store is initialized.
        val storage = TestSharedPrefs(mActivityRule.activity)
        tester = InDBUser(db = db, storage = storage, generator = keyGenerator,
                recipientId = "tester", deviceId = 1).setup()
        activeAccount = activeAccount.copy(id = db.accountDao().getLoggedInAccount()!!.id)
    }

    @Test
    fun should_correctly_encrypt_a_file_by_chunks_and_save_it_on_a_temp_file() {

        val constantFile = createTempFile()
        val constantListOfFileEntries: List<String>

        val decryptedList: List<String>

        try {
            FileDownloader.download(EncryptFileByChunksTest.testTxtFileURL, constantFile)
            val bob = InMemoryUser(keyGenerator, "tester", 2).setup()

            constantListOfFileEntries = constantFile.readLines()

            val keyBundleFromBob = bob.fetchAPreKeyBundle(activeAccount.id)
            tester.buildSession(keyBundleFromBob)


            val encryptedFile = signalClient.encryptFileByChunks(constantFile, "tester", 2, 512000)

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