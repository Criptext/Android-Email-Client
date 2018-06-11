package com.email.scenes.signup.data

import com.email.api.HttpClient
import com.email.db.KeyValueStorage
import com.email.db.dao.SignUpDao
import com.email.scenes.signup.IncompleteAccount
import com.email.signal.SignalClient
import com.email.signal.SignalKeyGenerator
import com.gaumala.kotlinsnapshot.Camera
import io.mockk.*
import org.amshove.kluent.`should be instance of`
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

/**
 * Created by gabriel on 5/16/18.
 */

class RegisterUserWorkerTest {
    private lateinit var signal: SignalClient
    private lateinit var keyGenerator: SignalKeyGenerator
    private lateinit var httpClient: HttpClient
    private lateinit var signUpDao: SignUpDao
    private lateinit var storage: KeyValueStorage

    private val camera = Camera()

    @Before
    fun setup() {

        signal = mockk()
        keyGenerator = mockk()
        httpClient = mockk()
        signUpDao = mockk()
        storage = mockk()
    }

    private fun newWorker(incompleteAccount: IncompleteAccount): RegisterUserWorker =
        RegisterUserWorker(db = signUpDao, keyValueStorage = storage, httpClient = httpClient,
                signalKeyGenerator = keyGenerator, incompleteAccount = incompleteAccount,
                publishFn = {})


    @Test
    fun `should post a new user to the server`() {
        val bodySlot = CapturingSlot<JSONObject>()

        every {
            keyGenerator.register("tester", 1)
        } returns RegisterUserTestUtils.createRegistrationBundles("tester", 1)
        every { httpClient.post("/user", null, capture(bodySlot)) } returns "OK"
        every { signUpDao.insertNewAccountData(any(), any(), any(), any(), any()) } just Runs

        val newAccount = IncompleteAccount(username ="tester", name = "A Tester", deviceId = 1,
                password = "secretPassword", recoveryEmail = "tester@gmail.com")
        val worker = newWorker(newAccount)

        worker.work(mockk()) `should be instance of` SignUpResult.RegisterUser.Success::class.java

        val uploadedJSONString = bodySlot.captured.toString(2)
        camera.matchWithSnapshot("uploads new user data with correct shape", uploadedJSONString)
    }
}