package com.criptext.mail.scenes.signin.data

import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.SignInLocalDB
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.SignUpDao
import com.criptext.mail.scenes.signup.data.RegisterUserTestUtils
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.signal.SignalKeyGenerator
import com.gaumala.kotlinsnapshot.Camera
import io.mockk.*
import org.amshove.kluent.`should be instance of`
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException

/**
 * Created by gabriel on 5/18/18.
 */

class AuthenticateUserWorkerTest {
    private lateinit var keyGenerator: SignalKeyGenerator
    private lateinit var httpClient: HttpClient
    private lateinit var signUpDao: SignUpDao
    private lateinit var storage: KeyValueStorage
    private lateinit var accountDao: AccountDao
    private lateinit var messagingInstance: MessagingInstance
    private lateinit var db: SignInLocalDB

    private val camera = Camera()

    @Before
    fun setup() {

        keyGenerator = mockk()
        httpClient = mockk()
        signUpDao = mockk()
        storage = mockk(relaxed = true)
        accountDao = mockk()
        messagingInstance = mockk()
        db = mockk()


        every { messagingInstance.token } returns ""
    }

    private fun newWorker(username: String, password: String): AuthenticateUserWorker =
        AuthenticateUserWorker(signUpDao = signUpDao, keyValueStorage = storage, httpClient = httpClient,
                keyGenerator = keyGenerator, username = username, password = password,
                publishFn = {}, accountDao = accountDao, messagingInstance = messagingInstance, db = db)


    @Test
    fun `should send login request and store new account data without errors`() {
        val worker = newWorker("tester", "securePassword123")

        // has no previous sign in session stored
        every { storage.getString(KeyValueStorage.StringKey.SignInSession, "") } returns ""

        val authRequestSlot = CapturingSlot<JSONObject>()
        val postKeyBundleRequestSlot = CapturingSlot<JSONObject>()
        val mockedAuthResponse = SignInSession(token = "__JWTOKEN__", deviceId = 2, name = "A Tester")
                    .toJSON().toString()
        every {
            httpClient.post("/user/auth", null, capture(authRequestSlot))
        } returns mockedAuthResponse
        every {
            httpClient.post("/keybundle", "__JWTOKEN__", capture(postKeyBundleRequestSlot))
        } returns "__JWTOKEN__"

        every {
            httpClient.put("/keybundle/pushtoken", "__JWTOKEN__", any<JSONObject>())
        } returns mockedAuthResponse


        every {
            keyGenerator.register("tester", 2)
        } returns RegisterUserTestUtils.createRegistrationBundles("tester", 2)

        val extraStepsSlot = CapturingSlot<Runnable>()
        every {
            signUpDao.insertNewAccountData(account = any(), preKeyList = any(),
                    signedPreKey = any(), extraRegistrationSteps = capture(extraStepsSlot),
                    defaultLabels = any())
        } answers { extraStepsSlot.captured.run() }

        every { accountDao.updateJwt("tester", "__JWTOKEN__") } just Runs

        val result = worker.work(mockk())

        result `should be instance of` SignInResult.AuthenticateUser.Success::class.java

        // verify data got stored
        verify {
            storage.putString(KeyValueStorage.StringKey.SignInSession, mockedAuthResponse)
        }
        verify {
            storage.putString(KeyValueStorage.StringKey.ActiveAccount,
                    """{"signature":"","jwt":"__JWTOKEN__","name":"A Tester","recipientId":"tester","deviceId":2}""")
        }

        // request snapshots
        camera.matchWithSnapshot("should send login request with the right shape",
                authRequestSlot.captured.toString(4))
        camera.matchWithSnapshot("should upload keybundle with the right shape",
                postKeyBundleRequestSlot.captured.toString(4))
    }

     @Test
    fun `if post keybundle fails, should not store anything locally except for the sign in session`() {
        val worker = newWorker("tester", "securePassword123")

        // has no previous sign in session stored
        every { storage.getString(KeyValueStorage.StringKey.SignInSession, "") } returns ""

        val mockedAuthResponse = SignInSession(token = "__JWTOKEN__", deviceId = 2, name = "A Tester")
                    .toJSON().toString()
        every {
            httpClient.post("/user/auth", null, any<JSONObject>())
        } returns mockedAuthResponse
        every {
            httpClient.post("/keybundle", "__JWTOKEN__", any<JSONObject>())
        } throws SocketTimeoutException()

         every {
             httpClient.put("/keybundle/pushtoken", "__JWTOKEN__", any<JSONObject>())
         } returns mockedAuthResponse


        every {
            keyGenerator.register("tester", 2)
        } returns RegisterUserTestUtils.createRegistrationBundles("tester", 2)

        val extraStepsSlot = CapturingSlot<Runnable>()
        every {
            signUpDao.insertNewAccountData(account = any(), preKeyList = any(),
                    signedPreKey = any(), extraRegistrationSteps = capture(extraStepsSlot),
                    defaultLabels = any())
        } answers { extraStepsSlot.captured.run() }

        val result = worker.work(mockk())

        result `should be instance of` SignInResult.AuthenticateUser.Failure::class.java

        // verify data got stored
        verify {
            storage.putString(KeyValueStorage.StringKey.SignInSession, mockedAuthResponse)
        }
        verify(inverse = true) {
            storage.putString(KeyValueStorage.StringKey.ActiveAccount, any())
        }

    }

}