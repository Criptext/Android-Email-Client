package com.email.scenes.signup.data

import com.email.api.HttpClient
import com.email.db.KeyValueStorage
import com.email.db.dao.SignUpDao
import com.email.scenes.signup.IncompleteAccount
import com.email.signal.PreKeyBundleShareData
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

    private fun createRegistrationBundles(recipientId: String): SignalKeyGenerator.RegistrationBundles {
        val preKeys = mapOf(Pair(1, "__PK_1__"), Pair(2, "__PK_2__"), Pair(3, "__PK_3__"))
        val registrationId = 54378
        return SignalKeyGenerator.RegistrationBundles(
                privateBundle = SignalKeyGenerator.PrivateBundle(identityKeyPair = "__IDENTITY_KEY_PAIR__",
                        signedPreKeyId = 1, signedPreKey = "__SIGNED_PRE_KEY__",
                        registrationId = registrationId, preKeys = preKeys),
                uploadBundle = PreKeyBundleShareData.UploadBundle(
                        shareData = PreKeyBundleShareData(recipientId = recipientId, deviceId = 1,
                                signedPreKeyId = 1, signedPreKeyPublic = "__SIGNED_PRE_KEY_PUBLIC__",
                                signedPreKeySignature = "__SIGNED_PRE_KEY_SIGNATURE__",
                                identityPublicKey = "__IDENTITY_PUBLIC_KEY__",
                                registrationId = registrationId), preKeys = preKeys)
        )
    }

    @Test
    fun `should post a new user to the server`() {
        val bodySlot = CapturingSlot<JSONObject>()

        every {
            keyGenerator.register("tester", 1)
        } returns createRegistrationBundles("tester")
        every { httpClient.post("/user", null, capture(bodySlot)) } returns "OK"
        every { signUpDao.insertNewAccountData(any(), any(), any(), any(), any()) } just Runs

        val newAccount = IncompleteAccount(username ="tester", name = "A Tester",
                password = "secretPassword", recoveryEmail = "tester@gmail.com")
        val worker = newWorker(newAccount)

        worker.work() `should be instance of` SignUpResult.RegisterUser.Success::class.java

        val uploadedJSONString = bodySlot.captured.toString(2)
        camera.matchWithSnapshot("uploads new user data with correct shape", uploadedJSONString)
    }
}